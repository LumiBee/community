package com.lumibee.hive.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.constant.CacheNames;
import com.lumibee.hive.dto.ArticleDetailsDTO;
import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.ArticlePublishRequestDTO;
import com.lumibee.hive.dto.LikeResponse;
import com.lumibee.hive.dto.PortfolioDTO;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.ArticleFavoritesMapper;
import com.lumibee.hive.mapper.ArticleLikesMapper;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.mapper.UserMapper;
import com.lumibee.hive.model.Article;
import com.lumibee.hive.model.ArticleDocument;
import com.lumibee.hive.model.Portfolio;
import com.lumibee.hive.model.Tag;
import com.lumibee.hive.model.User;


/**
 * 文章服务实现类
 * 负责文章相关的业务逻辑处理，包括文章的增删改查、发布、草稿保存等操作
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired private ArticleMapper articleMapper;
    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private com.lumibee.hive.service.UserService userService;
    @Autowired private UserMapper userMapper;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private ArticleLikesMapper articleLikesMapper;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private ArticleFavoritesMapper favoriteMapper;
    @Autowired private CacheManager cacheManager;
    @Autowired private RedisCacheService redisCacheService;
    @Autowired private CacheMonitoringService cacheMonitoringService;


    /**
     * 获取首页文章列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.HOMEPAGE_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).homepageArticles(#pageNum, #pageSize)")
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize) {
        Page<Article> articlePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.ArticleStatus.published)
                    .eq(Article::getDeleted, 0)
                    .orderByDesc(Article::getGmtModified);

        return getArticleExcerptDTOPage(pageNum, pageSize, articlePageRequest, queryWrapper);
    }

    /**
     * 获取所有已发布的文章详情
     * @return 文章详情列表
     */
    @Override
    @Cacheable(value = CacheNames.ALL_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).allArticles()")
    @Transactional(readOnly = true)
    public List<ArticleDetailsDTO> selectAll() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                    .eq("status", Article.ArticleStatus.published)
                    .orderByDesc("gmt_modified");
        List<Article> articles = articleMapper.selectList(queryWrapper);
        
        if (articles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询用户信息，避免N+1查询问题
        List<Long> userIds = articles.stream()
                .map(Article::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, User> userMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        List<ArticleDetailsDTO> articleDetailsDTOs = new ArrayList<>();
        for (Article article : articles) {
            ArticleDetailsDTO articleDetailsDTO = new ArticleDetailsDTO();
            BeanUtils.copyProperties(article, articleDetailsDTO);
            User user = userMap.get(article.getUserId());
            if (user != null) {
                articleDetailsDTO.setUserName(user.getName());
            }
            articleDetailsDTOs.add(articleDetailsDTO);
        }
        return articleDetailsDTOs;
    }

    /**
     * 获取热门文章列表
     * @param limit 限制数量
     * @return 热门文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.POPULAR_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).popularArticles(#limit)")
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getPopularArticles(int limit) {
        return articleMapper.getPopularArticles(limit);
    }

    /**
     * 获取精选文章列表
     * @return 精选文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.FEATURED_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).featuredArticles()")
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getFeaturedArticles() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                .eq("status", Article.ArticleStatus.published)
                .eq("is_featured", true)
                .orderByDesc("gmt_modified");

        List<Article> featuredArticles = articleMapper.selectList(queryWrapper);
        if (featuredArticles.isEmpty()) {
            return new ArrayList<>(); // 如果没有精选文章，返回空列表
        }

        // 获取所有用户ID
        List<Long> userIds = featuredArticles.stream()
                .map(Article::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<Long, User> userMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<ArticleExcerptDTO> articleExcerptDTOS = new ArrayList<>();
        for (Article article : featuredArticles) {
            ArticleExcerptDTO articleDTO = new ArticleExcerptDTO();
            BeanUtils.copyProperties(article, articleDTO);

            // 设置用户信息
            User user = userMap.get(article.getUserId());
            if (user != null) {
                articleDTO.setUserName(user.getName());
                articleDTO.setAvatarUrl(user.getAvatarUrl());
            }

            articleExcerptDTOS.add(articleDTO);
        }

        return articleExcerptDTOS;
    }

    /**
     * 根据标签slug获取文章列表
     * @param tagSlug 标签slug
     * @return 该标签下的文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.TAG_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).tagArticles(#tagSlug)")
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getArticlesByTagSlug(String tagSlug) {
        TagDTO tag = tagService.selectTagBySlug(tagSlug);
        if (tag == null) {
            return new ArrayList<>();
        }
        return articleMapper.getArticlesByTagId(tag.getTagId());
    }

    /**
     * 根据作品集ID获取文章列表
     * @param id 作品集ID
     * @return 该作品集下的文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.PORTFOLIO_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).portfolioArticles(#id)")
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getArticlesByPortfolioId(Integer id) {
        return articleMapper.getArticlesByPortfolioId(id);
    }

    /**
     * 获取用户个人页面的文章列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的文章摘要列表
     */
    @Override
    @Cacheable(value = CacheNames.USER_ARTICLES, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).userArticles(#userId)")
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getProfilePageArticle(long userId, long pageNum, long pageSize) {
        Page<Article> articleProfilePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.ArticleStatus.published)
                    .eq(Article::getDeleted, 0)
                    .eq(Article::getUserId, userId)
                    .orderByDesc(Article::getGmtModified);

        return getArticleExcerptDTOPage(pageNum, pageSize, articleProfilePageRequest, queryWrapper);
    }

    /**
     * 根据slug获取文章详情
     * @param slug 文章slug
     * @return 文章详情
     */
    @Override
    @Cacheable(value = CacheNames.ARTICLE_DETAIL, key = "T(com.lumibee.hive.utils.CacheKeyBuilder).articleDetail(#slug)")
    @Transactional
    public ArticleDetailsDTO getArticleBySlug(String slug) {
        QueryWrapper<Article> wrapper = new QueryWrapper<> ();
        wrapper.eq("slug", slug).eq("deleted", 0);
        Article article = articleMapper.selectOne(wrapper);

        if (article == null) {
            return null; // 如果没有找到文章，返回null
        }

        try {
            articleMapper.incrementViewCount(article.getArticleId());
            User user = userService.selectById(article.getUserId());
            Portfolio portfolio = portfolioMapper.selectById(article.getPortfolioId());
            List<Tag> tags = tagService.selectTagsByArticleId(article.getArticleId());

            ArticleDetailsDTO articleDetailsDTO = new ArticleDetailsDTO();
            BeanUtils.copyProperties(article, articleDetailsDTO);
            if (user != null) {
                articleDetailsDTO.setUserId(user.getId());
                articleDetailsDTO.setUserName(user.getName());
                articleDetailsDTO.setAvatarUrl(user.getAvatarUrl());
                
                // 获取用户的统计数据
                Integer userArticleCount = articleMapper.countArticlesByUserId(user.getId());
                Integer userFollowersCount = userService.countFansByUserId(user.getId());
                Integer userFollowingCount = userService.countFollowingByUserId(user.getId());

                
                articleDetailsDTO.setUserArticleCount(userArticleCount);
                articleDetailsDTO.setUserFollowersCount(userFollowersCount);
                articleDetailsDTO.setUserFollowingCount(userFollowingCount);
                articleDetailsDTO.setUserBio(user.getBio());
            }
            if (portfolio != null) {
                articleDetailsDTO.setPortfolio(convertToPortfolioDTO(portfolio));
            }
            if (tags != null && !tags.isEmpty()) {
                List<TagDTO> tagDTOs = new ArrayList<>();
                for (Tag tag : tags) {
                    tagDTOs.add(convertToTagDTO(tag));
                }
                articleDetailsDTO.setTags(tagDTOs);
            }

            // 确保文章存在于Elasticsearch中
            try {
                if (!articleRepository.existsById(article.getArticleId())) {
                    // 如果文章不存在于Elasticsearch，尝试重新索引
                    ArticleDocument articleDocument = new ArticleDocument();
                    articleDocument.setId(article.getArticleId());
                    articleDocument.setContent(article.getContent());
                    articleDocument.setUserName(user != null ? user.getName() : "");
                    articleDocument.setTitle(article.getTitle());
                    articleDocument.setSlug(article.getSlug());
                    articleDocument.setLikes(article.getLikes());
                    articleDocument.setViewCount(article.getViewCount());
                    articleDocument.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
                    articleRepository.save(articleDocument);
                }
            } catch (Exception e) {
                // 如果Elasticsearch操作失败，仍然返回文章详情
                System.err.println("Elasticsearch索引文章失败: " + e.getMessage());
            }

            return articleDetailsDTO;
        } catch (Exception e) {
            System.err.println("获取文章详情失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public LikeResponse toggleLike(long userId, int articleId) {
        //获取当前文章的点赞状态
        boolean isCurrentlyLiked = articleLikesMapper.toggleLike(userId, articleId) == null;
        //用来储存用户的点赞状态
        boolean newLikedStatus;

        if (isCurrentlyLiked) {
            //如果当前用户没有点赞，则插入点赞记录并更新文章的点赞状态
            articleLikesMapper.insertLike(userId, articleId);
            articleMapper.updateArticleLikes(articleId, 1);
            newLikedStatus = true;
        }else {
            //如果当前用户已经点赞，则删除点赞记录并更新文章的点赞状态
            articleLikesMapper.deleteLike(userId, articleId);
            articleMapper.updateArticleLikes(articleId, -1);
            newLikedStatus = false;
        }

        // --- 手动清除缓存 ---
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            Objects.requireNonNull(cacheManager.getCache("articleDetails")).evict(article.getSlug());
        }

        //获取当前文章的点赞数量
        Integer likesCount = articleMapper.countLikes(articleId);

        return new LikeResponse(true, newLikedStatus, likesCount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserLiked(long userId, int articleId) {
        return articleLikesMapper.toggleLike(userId, articleId) != null;
    }

    @Override
    @Transactional
    public void incrementViewCount(Integer articleId) {
        articleMapper.incrementViewCount(articleId);
    }

    /**
     * 发布文章
     * @param requestDTO 文章发布请求数据
     * @param userId 用户ID
     * @return 文章详情
     */
    @Override
    @Transactional
    public ArticleDetailsDTO publishArticle(ArticlePublishRequestDTO requestDTO, Long userId) {
        if (requestDTO.getArticleId() != null) {
            return updateArticle(requestDTO.getArticleId(), requestDTO, userId);
        }

        Article article = new Article();
        article.setTitle(requestDTO.getTitle());
        article.setContent(requestDTO.getContent());
        article.setExcerpt(requestDTO.getExcerpt());

        article.setUserId(userId);
        article.setGmtCreate(LocalDateTime.now());
        article.setGmtModified(LocalDateTime.now());
        article.setStatus(Article.ArticleStatus.published);
        article.setSlug(createUniqueSlug(requestDTO.getTitle()));

        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            Integer portfolioId = portfolioService.selectOrCreatePortfolio(requestDTO.getPortfolioName(), userId).getId();
            article.setPortfolioId(portfolioId);
            portfolioService.updatePortfolioGmt(portfolioId, userId);
        }

        articleMapper.insert(article);

        Integer articleId = article.getArticleId();
        List<String> tagsName = requestDTO.getTagsName();
        if (tagsName != null && !tagsName.isEmpty()) {
            Set<Tag> tags = tagService.selectOrCreateTags(tagsName);
            for (Tag tag : tags) {
                if (tag != null) {
                    tagService.insertTagArticleRelation(articleId, tag.getTagId());
                    tagService.incrementArticleCount(tag.getTagId());
                }
            }
            // 清除标签列表缓存
            redisCacheService.clearAllTagListCaches();
        }

        // 将文章保存到 Elasticsearch
        User user = userMapper.selectById(userId);
        ArticleDocument articleDocument = new ArticleDocument();
        articleDocument.setId(articleId);
        articleDocument.setContent(requestDTO.getContent());
        articleDocument.setUserName(user.getName());  // 修正：使用getName()而不是getUsername()
        articleDocument.setTitle(requestDTO.getTitle());
        articleDocument.setSlug(article.getSlug());
        articleDocument.setLikes(article.getLikes());
        articleDocument.setViewCount(article.getViewCount());
        articleDocument.setAvatarUrl(user.getAvatarUrl());  // 确保头像URL被正确设置
        
        // 保存到Elasticsearch
        articleRepository.save(articleDocument);

        // 发布文章后，清除相关缓存
        redisCacheService.clearArticleRelatedCaches(article);

        return this.getArticleBySlug(article.getSlug());
    }

    /**
     * 更新文章
     * @param articleId 文章ID
     * @param requestDTO 文章更新请求数据
     * @param userId 用户ID
     * @return 文章详情
     */
    @Override
    @Transactional
    public ArticleDetailsDTO updateArticle(Integer articleId, ArticlePublishRequestDTO requestDTO, Long userId) {
        Article existingArticle = articleMapper.selectById(articleId);
        if (existingArticle == null || !existingArticle.getUserId().equals(userId)) {
            throw new  RuntimeException("文章不存在或不属于当前用户");
        }

        existingArticle.setTitle(requestDTO.getTitle());
        existingArticle.setContent(requestDTO.getContent());
        existingArticle.setExcerpt(requestDTO.getExcerpt());
        existingArticle.setGmtModified(LocalDateTime.now());

        // 更新作品集
        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            Portfolio portfolio = portfolioService.selectOrCreatePortfolio(requestDTO.getPortfolioName(), userId);
            existingArticle.setPortfolioId(portfolio.getId());
            portfolioService.updatePortfolioGmt(portfolio.getId(), userId);
        }else {
            existingArticle.setPortfolioId(null);
        }

        // 更新标签
        articleMapper.deleteArticleTagByArticleId(articleId);
        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            Set<Tag> tags = tagService.selectOrCreateTags(requestDTO.getTagsName());
            for (Tag tag : tags) {
                if (tag != null) {
                    tagService.insertTagArticleRelation(articleId, tag.getTagId());
                    tagService.incrementArticleCount(tag.getTagId());
                }
            }
            // 清除标签列表缓存
            redisCacheService.clearAllTagListCaches();
        }

        existingArticle.setStatus(Article.ArticleStatus.published);
        articleMapper.updateById(existingArticle);

        // 更新Elasticsearch中的文章数据
        User user = userMapper.selectById(userId);
        ArticleDocument articleDocument = new ArticleDocument();
        articleDocument.setId(articleId);
        articleDocument.setContent(requestDTO.getContent());
        articleDocument.setUserName(user.getName());
        articleDocument.setTitle(requestDTO.getTitle());
        articleDocument.setSlug(existingArticle.getSlug());
        articleDocument.setLikes(existingArticle.getLikes());
        articleDocument.setViewCount(existingArticle.getViewCount());
        articleDocument.setAvatarUrl(user.getAvatarUrl());

        // 保存/更新到Elasticsearch
        articleRepository.save(articleDocument);

        // 更新文章后，清除相关缓存
        redisCacheService.clearArticleRelatedCaches(existingArticle);

        return getArticleBySlug(existingArticle.getSlug());
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 文章详情
     */
    @Override
    @Transactional
    public ArticleDetailsDTO deleteArticleById(Integer articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 验证用户权限：只有文章作者才能删除文章
        if (!article.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此文章");
        }

        if (article.getSlug() != null) {
            Objects.requireNonNull(cacheManager.getCache("articleDetails")).evict(article.getSlug());
        }

        int result = articleMapper.deleteById(articleId);
        if (result == 0) {
            throw new RuntimeException("删除文章失败");
        }
        articleRepository.deleteById(articleId);

        // 删除文章后，清除相关缓存
        redisCacheService.clearArticleRelatedCaches(article);
        redisCacheService.clearAllTagListCaches();

        return convertToArticleDetailsDTO(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countArticlesByUserId(Long id) {
        return articleMapper.countArticlesByUserId(id);
    }

    // TODO : 完善权限控制，确保只有管理员可以设置精选文章
    @Transactional
    public void setArticleFeatured(Integer articleId, boolean isFeatured, User.UserRole role) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        
        article.setFeatured(isFeatured);
        article.setGmtModified(LocalDateTime.now());
        articleMapper.updateById(article);
        
        // 清除相关缓存
        if (article.getSlug() != null) {
            Objects.requireNonNull(cacheManager.getCache("articleDetails")).evict(article.getSlug());
        }
    }

    @Override
    @Transactional
    public ArticleDetailsDTO saveDraft(Integer articleId, ArticlePublishRequestDTO requestDTO, Long userId) {
        Article article = null;
        boolean isNewArticle = false;
        
        if (articleId == null) {
            // 创建新草稿
            article = new Article();
            isNewArticle = true;
        } else {
            // 更新现有草稿
            article = articleMapper.selectById(articleId);
            if (article == null || !article.getUserId().equals(userId)) {
                throw new RuntimeException("草稿不存在或不属于当前用户");
            }
        }

        article.setTitle(requestDTO.getTitle());
        article.setContent(requestDTO.getContent());
        article.setExcerpt(requestDTO.getExcerpt());
        article.setUserId(userId);
        article.setGmtModified(LocalDateTime.now());
        
        // 只有新文章才设置创建时间
        if (isNewArticle) {
            article.setGmtCreate(LocalDateTime.now());
        }

        article.setStatus(Article.ArticleStatus.draft);
        article.setSlug(createUniqueSlug(requestDTO.getTitle()));

        // 根据是否为新文章选择插入或更新
        if (isNewArticle) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }

        return getArticleBySlug(article.getSlug());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailsDTO selectDraftById(Integer articleId) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId)
               .eq("deleted", 0);
        Article article = articleMapper.selectOne(wrapper);

        if (article == null) {
            return null; // 如果没有找到草稿文章，返回null
        }

        ArticleDetailsDTO detailsDTO = new ArticleDetailsDTO();
        BeanUtils.copyProperties(article, detailsDTO);

        return detailsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public int getFavoriteCount(Integer articleId) {
        if (articleId == null) {
            return 0; // 如果文章ID无效，返回0
        }
        return favoriteMapper.countArticlesFavorited(articleId);
    }

    private ArticleDetailsDTO convertToArticleDetailsDTO(Article article) {
        if (article == null) return null;
        ArticleDetailsDTO dto = new ArticleDetailsDTO();
        BeanUtils.copyProperties(article, dto);

        // 获取用户信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName());
            dto.setAvatarUrl(user.getAvatarUrl());
        }

        // 获取作品集信息
        Portfolio portfolio = portfolioMapper.selectById(article.getPortfolioId());
        dto.setPortfolio(convertToPortfolioDTO(portfolio));

        // 获取标签信息
        List<Tag> tags = tagService.selectTagsByArticleId(article.getArticleId());
        if (tags != null && !tags.isEmpty()) {
            List<TagDTO> tagDTOs = tags.stream()
                    .map(this::convertToTagDTO)
                    .collect(Collectors.toList());
            dto.setTags(tagDTOs);
        }

        return dto;
    }

    @NotNull
    private Page<ArticleExcerptDTO> getArticleExcerptDTOPage(long pageNum, long pageSize, Page<Article> articlePageRequest, LambdaQueryWrapper<Article> queryWrapper) {
        // 使用MyBatis-Plus的分页查询
        Page<Article> articlePage = articleMapper.selectPage(articlePageRequest, queryWrapper);

        // 转换为DTO
        List<ArticleExcerptDTO> articleDTOList = articlePage.getRecords().stream()
                .map(article -> {
                    ArticleExcerptDTO dto = new ArticleExcerptDTO();
                    BeanUtils.copyProperties(article, dto);

                    // 获取用户信息
                    User user = userService.selectById(article.getUserId());
                    if (user != null) {
                        dto.setUserName(user.getName());
                        dto.setAvatarUrl(user.getAvatarUrl());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        // 创建返回的分页对象
        Page<ArticleExcerptDTO> articleDTOPage = new Page<>(pageNum, pageSize);
        articleDTOPage.setRecords(articleDTOList);
        articleDTOPage.setTotal(articlePage.getTotal());
        articleDTOPage.setCurrent(articlePage.getCurrent());
        articleDTOPage.setSize(articlePage.getSize());

        return articleDTOPage;
    }

    private PortfolioDTO convertToPortfolioDTO(Portfolio portfolio) {
        if (portfolio == null) return null;
        PortfolioDTO dto = new PortfolioDTO(); // 假设您已创建 PortfolioDTO 类
        BeanUtils.copyProperties(portfolio, dto);
        return dto;
    }

    private TagDTO convertToTagDTO(Tag tag) {
        if (tag == null) return null;
        TagDTO dto = new TagDTO(); // 假设您已创建 TagDTO 类
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }

    public String createUniqueSlug(String title) {
        String baseSlug = SlugGenerator.generateSlug(title);
        if (baseSlug.isEmpty()) {
            baseSlug = "article-" + System.currentTimeMillis();
        }

        String potentialSlug = baseSlug;
        int count = 0;
        while (articleMapper.selectBySlug(potentialSlug)) {
            potentialSlug = potentialSlug + "-" + count++;
        }

        return potentialSlug;
    }
}
