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

import lombok.extern.log4j.Log4j2;

/**
 * 文章服务实现类
 * 负责文章相关的业务逻辑处理，包括文章的增删改查、发布、草稿保存等操作
 */
@Service
@Log4j2
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
    @Autowired private RedisClearCacheService redisClearCacheService;
    @Autowired private RedisCounterService redisCounterService;
    @Autowired private RedisPopularArticleService redisPopularArticleService;
    @Autowired private CacheBreakdownProtectionService cacheBreakdownProtectionService;


    /**
     * 获取首页文章列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的文章摘要列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize) {
        return getHomepageArticleWithBreakdownProtection(pageNum, pageSize);
    }

    /**
     * 使用分布式锁防止缓存击穿的首页文章获取方法
     */
    private Page<ArticleExcerptDTO> getHomepageArticleWithBreakdownProtection(long pageNum, long pageSize) {
        String cacheKey = "page:" + pageNum + "::size:" + pageSize;
        return cacheBreakdownProtectionService.getWithBreakdownProtection(
            "articles::list::homepage", 
            cacheKey, 
            () -> loadHomepageArticleFromDatabase(pageNum, pageSize)
        );
    }

    /**
     * 从数据库加载首页文章
     */
    private Page<ArticleExcerptDTO> loadHomepageArticleFromDatabase(long pageNum, long pageSize) {
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
    @Cacheable(value = "articles::list::all", key = "''")
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
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getPopularArticles(int limit) {
        try {
            // 从 Redis Sorted Set 获取热门文章ID列表
            List<String> popularArticleIds = redisPopularArticleService.getPopularArticleIds(limit);
            
            if (!popularArticleIds.isEmpty()) {
                log.info("从 Redis 获取到热门文章ID: {}", popularArticleIds);
                
                // 使用 HMGET 批量获取文章摘要信息
                List<ArticleExcerptDTO> cachedArticles = redisPopularArticleService.getArticleExcerpts(popularArticleIds);
                
                if (!cachedArticles.isEmpty()) {
                    log.info("从 Redis 缓存获取热门文章成功: limit={}, count={}", limit, cachedArticles.size());
                    return cachedArticles;
                } else {
                    log.warn("Redis 缓存中没有文章摘要数据，回退到数据库查询");
                }
            }
            
            // 如果 Redis 中没有数据，回退到数据库查询
            log.debug("Redis 中没有热门文章数据，回退到数据库查询");
            List<ArticleExcerptDTO> dbResult = articleMapper.getPopularArticles(limit);
            
            // 将数据库结果同步到 Redis
            for (ArticleExcerptDTO article : dbResult) {
                try {
                    // 获取文章的统计数据
                    Integer viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
                    Integer likeCount = redisCounterService.getArticleLikeCount(article.getArticleId());
                    Integer commentCount = 0; // 这里可以添加评论数查询
                    
                    // 获取文章发布时间
                    Article fullArticle = articleMapper.selectById(article.getArticleId());
                    java.time.LocalDateTime publishTime = fullArticle != null ? fullArticle.getGmtCreate() : null;
                    
                    // 更新到 Redis Sorted Set（包含时间衰减）
                    redisPopularArticleService.updateArticlePopularity(
                        article.getArticleId(), viewCount, likeCount, commentCount, publishTime
                    );
                    
                    // 缓存文章摘要信息到 Redis Hash
                    redisPopularArticleService.cacheArticleExcerpt(article);
                    
                } catch (Exception e) {
                    log.warn("同步文章到 Redis 失败: articleId={}", article.getArticleId(), e);
                }
            }
            
            return dbResult;
            
        } catch (Exception e) {
            log.error("获取热门文章失败: limit={}", limit, e);
            // 发生异常时回退到数据库查询
            return articleMapper.getPopularArticles(limit);
        }
    }

    /**
     * 获取精选文章列表
     * @return 精选文章摘要列表
     */
    @Override
    @Cacheable(value = "articles::list::featured", key = "''")
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
     * 将 Article 转换为 ArticleExcerptDTO
     * 
     * @param article 文章对象
     * @return 文章摘要DTO
     */
    private ArticleExcerptDTO convertToExcerptDTO(Article article) {
        ArticleExcerptDTO dto = new ArticleExcerptDTO();
        BeanUtils.copyProperties(article, dto);
        
        // 设置用户信息
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            dto.setUserName(user.getName());
            dto.setAvatarUrl(user.getAvatarUrl());
        }
        
        return dto;
    }

    /**
     * 根据标签slug获取文章列表
     * @param tagSlug 标签slug
     * @return 该标签下的文章摘要列表
     */
    @Override
    @Cacheable(value = "articles::list::tag", key = "#tagSlug")
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
    @Cacheable(value = "articles::list::portfolio", key = "#id")
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
    @Cacheable(value = "articles::list::user", key = "#userId + '::' + #pageNum + '::' + #pageSize")
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getProfilePageArticle(long userId, long pageNum, long pageSize, boolean isDraft) {
        Page<Article> articleProfilePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (isDraft) {
            queryWrapper.eq(Article::getStatus, Article.ArticleStatus.draft)
                    .eq(Article::getDeleted, 0)
                    .eq(Article::getUserId, userId)
                    .orderByDesc(Article::getGmtModified);
        }else {
            queryWrapper.eq(Article::getStatus, Article.ArticleStatus.published)
                    .eq(Article::getDeleted, 0)
                    .eq(Article::getUserId, userId)
                    .orderByDesc(Article::getGmtModified);
        }

        return getArticleExcerptDTOPage(pageNum, pageSize, articleProfilePageRequest, queryWrapper);
    }

    /**
     * 根据slug获取文章详情
     * @param slug 文章slug
     * @return 文章详情
     */
    @Override
    @Transactional
    public ArticleDetailsDTO getArticleBySlug(String slug) {
        return getArticleBySlugWithBreakdownProtection(slug, null);
    }

    @Override
    @Transactional
    public ArticleDetailsDTO getArticleBySlug(String slug, Long userId) {
        return getArticleBySlugWithBreakdownProtection(slug, userId);
    }

    /**
     * 使用分布式锁防止缓存击穿的文章获取方法
     */
    private ArticleDetailsDTO getArticleBySlugWithBreakdownProtection(String slug, Long userId) {
        return cacheBreakdownProtectionService.getWithBreakdownProtection(
            "articles::detail", 
            slug, 
            () -> loadArticleFromDatabase(slug, userId)
        );
    }

    /**
     * 从数据库加载文章详情（原有逻辑）
     */
    private ArticleDetailsDTO loadArticleFromDatabase(String slug, Long userId) {
        QueryWrapper<Article> wrapper = new QueryWrapper<> ();
        wrapper.eq("slug", slug).eq("deleted", 0);
        Article article = articleMapper.selectOne(wrapper);

        if (article == null) {
            return null; // 如果没有找到文章，返回null
        }

        try {
            // 使用 Redis 计数器记录阅读量
            int viewCount;
            if (!redisCounterService.existsArticleViewCount(article.getArticleId())) {
                // 如果 Redis 中不存在，从数据库加载并增加1
                viewCount = article.getViewCount() != null ? article.getViewCount().intValue() + 1 : 1;
                redisCounterService.setArticleViewCount(article.getArticleId(), viewCount);
            } else {
                // 如果 Redis 中存在，直接增加1
                viewCount = redisCounterService.incrementArticleView(article.getArticleId());
            }
            article.setViewCount(viewCount);
            
            // 从 Redis 获取点赞数，如果 Redis 中没有则从数据库加载
            int likeCount;
            if (!redisCounterService.existsArticleLikeCount(article.getArticleId())) {
                // 如果 Redis 中不存在，从数据库加载
                likeCount = article.getLikes() != null ? article.getLikes() : 0;
                redisCounterService.setArticleLikeCount(article.getArticleId(), likeCount);
            } else {
                // 如果 Redis 中存在，直接获取
                likeCount = redisCounterService.getArticleLikeCount(article.getArticleId());
            }
            article.setLikes(likeCount);
            
            // 更新文章热度分数到 Redis
            try {
                redisPopularArticleService.incrementArticleView(article.getArticleId());
            } catch (Exception e) {
                log.warn("更新文章热度分数失败: articleId={}", article.getArticleId(), e);
            }
            
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
                Integer userFollowersCount = userService.countFansByUserId(user.getId());
                Integer userFollowingCount = userService.countFollowingByUserId(user.getId());
                
                // 从 Redis 获取用户文章数量
                int userArticleCount = redisCounterService.getUserArticleCount(user.getId());
                if (!redisCounterService.existsUserArticleCount(user.getId())) {
                    // 如果 Redis 中没有数据，从数据库获取并设置到 Redis
                    Integer dbCount = articleMapper.countArticlesByUserId(user.getId());
                    userArticleCount = dbCount != null ? dbCount : 0;
                    redisCounterService.setUserArticleCount(user.getId(), userArticleCount);
                }

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
                    articleDocument.setExcerpt(article.getExcerpt());
                    articleDocument.setUserName(user != null ? user.getName() : "");
                    articleDocument.setTitle(article.getTitle());
                    articleDocument.setSlug(article.getSlug());
                    articleDocument.setLikes(article.getLikes());
                    articleDocument.setViewCount(article.getViewCount());
                    articleDocument.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
                    articleDocument.setGmtModified(article.getGmtModified() != null ? article.getGmtModified().toString() : null);
                    articleDocument.setBackgroundUrl(article.getBackgroundUrl());
                    articleDocument.setUserId(article.getUserId());
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
            // Redis 计数器操作
            redisCounterService.incrementArticleLike(articleId);
            // 更新文章热度分数
            redisPopularArticleService.incrementArticleLike(articleId);
            newLikedStatus = true;
        }else {
            //如果当前用户已经点赞，则删除点赞记录并更新文章的点赞状态
            articleLikesMapper.deleteLike(userId, articleId);
            articleMapper.updateArticleLikes(articleId, -1);
            // Redis 计数器操作
            redisCounterService.decrementArticleLike(articleId);
            // 更新文章热度分数
            redisPopularArticleService.decrementArticleLike(articleId);
            newLikedStatus = false;
        }

        // 清除缓存
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            redisClearCacheService.clearArticleRelatedCaches(article);
        }

        //获取当前文章的点赞数量
        int likesCount;
        if (!redisCounterService.existsArticleLikeCount(articleId)) {
            // 如果 Redis 中不存在，从数据库获取
            likesCount = articleMapper.countLikes(articleId);
            redisCounterService.setArticleLikeCount(articleId, likesCount);
        } else {
            // 如果 Redis 中存在，直接获取
            likesCount = redisCounterService.getArticleLikeCount(articleId);
        }

        return new LikeResponse(true, newLikedStatus, likesCount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserLiked(long userId, int articleId) {
        return articleLikesMapper.toggleLike(userId, articleId) != null;
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

        // 检查是否存在相同标题的草稿
        QueryWrapper<Article> draftQuery = new QueryWrapper<>();
        draftQuery.eq("title", requestDTO.getTitle())
                  .eq("user_id", userId) // 确保是当前用户的草稿，这样即使是相同标题也不会冲突
                  .eq("status", Article.ArticleStatus.draft)
                  .eq("deleted", 0)
                  .orderByDesc("gmt_modified")
                  .last("LIMIT 1");
        
        Article existingArticle = articleMapper.selectOne(draftQuery);

        Article article;
        boolean isNewArticle = false;

        // 如果草稿不为空，则更新该草稿为已发布状态
        if (existingArticle != null && existingArticle.getStatus() == Article.ArticleStatus.draft) {
            // 将现有草稿转换为已发布状态
            article = existingArticle;
            article.setContent(requestDTO.getContent());
            article.setExcerpt(requestDTO.getExcerpt());
            article.setGmtModified(LocalDateTime.now());
            article.setStatus(Article.ArticleStatus.published);
            // 草稿转发布，不需要增加文章计数
            isNewArticle = false;
        } else {
            // 创建新文章（保留作为备用，防止万分之一的可能）
            article = new Article();
            article.setTitle(requestDTO.getTitle());
            article.setContent(requestDTO.getContent());
            article.setExcerpt(requestDTO.getExcerpt());
            article.setUserId(userId);
            article.setGmtCreate(LocalDateTime.now());
            article.setGmtModified(LocalDateTime.now());
            article.setStatus(Article.ArticleStatus.published);
            article.setSlug(createUniqueSlug(requestDTO.getTitle()));
            isNewArticle = true;
        }

        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            Integer portfolioId = portfolioService.selectOrCreatePortfolio(requestDTO.getPortfolioName(), userId).getId();
            article.setPortfolioId(portfolioId);
            portfolioService.updatePortfolioGmt(portfolioId, userId);
            
            // 更新 Redis 作品集文章计数
            try {
                redisCounterService.incrementPortfolioArticle(portfolioId);
            } catch (Exception e) {
                log.error("更新 Redis 作品集文章计数时出错: {}", e.getMessage());
            }
        }

        // 根据是否为新文章选择插入或更新
        if (isNewArticle) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }
        
        // 同步处理后续操作
        try {
            processArticlePublishOperations(article, requestDTO, userId, isNewArticle);
            log.info("文章发布后续操作处理成功: articleId={}", article.getArticleId());
        } catch (Exception e) {
            log.error("文章发布后续操作处理失败: articleId={}", article.getArticleId(), e);
            // 后续操作失败不影响核心业务，继续执行
        }

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
        Integer oldPortfolioId = existingArticle.getPortfolioId();
        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            Portfolio portfolio = portfolioService.selectOrCreatePortfolio(requestDTO.getPortfolioName(), userId);
            existingArticle.setPortfolioId(portfolio.getId());
            portfolioService.updatePortfolioGmt(portfolio.getId(), userId);
            
            // 更新 Redis 作品集文章计数
            try {
                // 如果原来有作品集，先减少计数
                if (oldPortfolioId != null && !oldPortfolioId.equals(portfolio.getId())) {
                    redisCounterService.decrementPortfolioArticle(oldPortfolioId);
                }
                // 增加新作品集计数
                redisCounterService.incrementPortfolioArticle(portfolio.getId());
            } catch (Exception e) {
                log.error("更新 Redis 作品集文章计数时出错: {}", e.getMessage());
            }
        } else {
            existingArticle.setPortfolioId(null);
            
            // 如果原来有作品集，减少计数
            if (oldPortfolioId != null) {
                try {
                    redisCounterService.decrementPortfolioArticle(oldPortfolioId);
                } catch (Exception e) {
                    log.error("更新 Redis 作品集文章计数时出错: {}", e.getMessage());
                }
            }
        }

        existingArticle.setStatus(Article.ArticleStatus.published);
        articleMapper.updateById(existingArticle);

        // 同步处理后续操作
        try {
            processArticlePublishOperations(existingArticle, requestDTO, userId, false);
            log.info("文章更新后续操作处理成功: articleId={}", articleId);
        } catch (Exception e) {
            log.error("文章更新后续操作处理失败: articleId={}", articleId, e);
            // 后续操作失败不影响核心业务，继续执行
        }

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

        // 删除文章摘要缓存
        try {
            redisPopularArticleService.removeArticleExcerpt(articleId);
            log.debug("删除文章摘要缓存成功: articleId={}", articleId);
        } catch (Exception e) {
            log.error("删除文章摘要缓存失败: articleId={}", articleId, e);
        }

        if (article.getSlug() != null) {
            Objects.requireNonNull(cacheManager.getCache("articleDetails")).evict(article.getSlug());
        }

        int result = articleMapper.deleteById(articleId);
        if (result == 0) {
            throw new RuntimeException("删除文章失败");
        }
        articleRepository.deleteById(articleId);

        // 更新 Redis 计数器
        try {
            // 用户文章计数 -1
            redisCounterService.decrementUserArticle(userId);
            // 如果文章有作品集，作品集文章计数 -1
            if (article.getPortfolioId() != null) {
                redisCounterService.decrementPortfolioArticle(article.getPortfolioId());
            }
        } catch (Exception e) {
            log.error("更新 Redis 计数器时出错: {}", e.getMessage());
        }

        // 删除文章后，清除相关缓存
        redisClearCacheService.clearArticleRelatedCaches(article);
        redisClearCacheService.clearAllTagListCaches();

        return convertToArticleDetailsDTO(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countArticlesByUserId(Long id) {
        // 从 Redis 获取用户文章数量
        try {
            int articleCount = redisCounterService.getUserArticleCount(id);
            if (!redisCounterService.existsUserArticleCount(id)) {
                // 如果 Redis 中没有数据，从数据库获取并设置到 Redis
                Integer dbCount = articleMapper.countArticlesByUserId(id);
                articleCount = dbCount != null ? dbCount : 0;
                redisCounterService.setUserArticleCount(id, articleCount);
            }
            return articleCount;
        } catch (Exception e) {
            log.error("获取用户 {} 文章数量时出错: {}", id, e.getMessage());
            // 如果 Redis 出错，从数据库获取
            return articleMapper.countArticlesByUserId(id);
        }
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
        
        // 从 Redis 获取文章收藏数
        try {
            int favoriteCount = redisCounterService.getArticleFavoriteCount(articleId);
            if (!redisCounterService.existsArticleFavoriteCount(articleId)) {
                // 如果 Redis 中没有数据，从数据库获取并设置到 Redis
                int dbCount = favoriteMapper.countArticlesFavorited(articleId);
                redisCounterService.setArticleFavoriteCount(articleId, dbCount);
                return dbCount;
            }
            return favoriteCount;
        } catch (Exception e) {
            log.error("获取文章 {} 收藏数时出错: {}", articleId, e.getMessage());
            // 如果 Redis 出错，从数据库获取
            return favoriteMapper.countArticlesFavorited(articleId);
        }
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


    /**
     * 处理文章发布相关的后续操作
     */
    private void processArticlePublishOperations(Article article, ArticlePublishRequestDTO requestDTO, Long userId, boolean isNewArticle) {
        Integer articleId = article.getArticleId();
        
        // 1. 处理缓存更新操作
        processCacheUpdateOperations(article, requestDTO, userId, isNewArticle);
        
        // 2. 处理搜索同步操作
        processSearchSyncOperations(article, requestDTO, userId);
        
        // 3. 处理标签同步操作
        if (requestDTO.getTagsName() != null && !requestDTO.getTagsName().isEmpty()) {
            processTagSyncOperations(articleId, requestDTO.getTagsName());
        }

        // 4. 增加用户积分
        userService.changePoints(userId, 5, "用户新发布了一篇文章");
    }

    /**
     * 处理缓存更新操作
     */
    private void processCacheUpdateOperations(Article article, ArticlePublishRequestDTO requestDTO, Long userId, boolean isNewArticle) {
        Integer articleId = article.getArticleId();
        
        // 更新用户文章计数
        if (isNewArticle) {
            redisCounterService.incrementUserArticle(userId);
        }
        
        // 更新作品集文章计数
        if (requestDTO.getPortfolioName() != null && !requestDTO.getPortfolioName().isEmpty()) {
            redisCounterService.incrementPortfolioArticle(article.getPortfolioId());
        }
        
        // 更新文章热度分数
        redisPopularArticleService.updateArticlePopularity(articleId, 0, 0, 0, article.getGmtCreate());

        // 同步热门文章摘要缓存（只缓存真正的热门文章）
        try {
            ArticleExcerptDTO articleExcerpt = convertToExcerptDTO(article);
            redisPopularArticleService.syncPopularArticleCache(articleExcerpt);
            log.debug("同步文章摘要缓存成功: articleId={}", articleId);
        } catch (Exception e) {
            log.error("同步文章摘要缓存失败: articleId={}", articleId, e);
        }
        
        // 清除相关缓存
        redisClearCacheService.clearHomepageArticleCaches();
        redisClearCacheService.clearUserArticleCaches(userId);
    }

    /**
     * 处理搜索同步操作
     */
    private void processSearchSyncOperations(Article article, ArticlePublishRequestDTO requestDTO, Long userId) {
        try {
            // 获取用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，跳过搜索同步: userId={}", userId);
                return;
            }
            
            // 创建搜索文档
            ArticleDocument searchDoc = new ArticleDocument();
            searchDoc.setId(article.getArticleId());
            searchDoc.setTitle(requestDTO.getTitle());
            searchDoc.setContent(requestDTO.getContent());
            searchDoc.setExcerpt(requestDTO.getExcerpt());
            searchDoc.setSlug(article.getSlug());
            searchDoc.setLikes(article.getLikes());
            searchDoc.setViewCount(article.getViewCount());
            searchDoc.setUserId(userId);
            searchDoc.setUserName(user.getName());
            searchDoc.setAvatarUrl(user.getAvatarUrl());
            searchDoc.setGmtModified(article.getGmtModified() != null ? article.getGmtModified().toString() : null);
            searchDoc.setBackgroundUrl(article.getBackgroundUrl());
            
            // 保存到 Elasticsearch
            articleRepository.save(searchDoc);
            
            log.info("搜索同步操作处理完成: articleId={}, title={}", article.getArticleId(), requestDTO.getTitle());
        } catch (Exception e) {
            log.error("搜索同步操作处理失败: articleId={}", article.getArticleId(), e);
        }
    }

    /**
     * 处理标签同步操作
     */
    private void processTagSyncOperations(Integer articleId, java.util.List<String> tagNames) {
        try {
            if (tagNames == null || tagNames.isEmpty()) {
                log.info("标签列表为空，跳过标签同步: articleId={}", articleId);
                return;
            }
            
            // 先删除文章的所有标签关系
            articleMapper.deleteArticleTagByArticleId(articleId);
            
            // 使用 TagService 的批量处理方法
            Set<Tag> tags = tagService.selectOrCreateTags(tagNames);
            
            // 为每个标签创建文章标签关系
            for (Tag tag : tags) {
                if (tag != null) {
                    tagService.insertTagArticleRelation(articleId, tag.getTagId());
                    log.debug("文章标签关系创建成功: articleId={}, tagId={}, tagName={}", 
                        articleId, tag.getTagId(), tag.getName());
                }
            }
            
            log.info("标签同步操作处理完成: articleId={}, tagNames={}", articleId, tagNames);
            // 清除标签列表缓存
            redisClearCacheService.clearAllTagListCaches();
        } catch (Exception e) {
            log.error("标签同步操作处理失败: articleId={}, tagNames={}", articleId, tagNames, e);
        }
    }
}
