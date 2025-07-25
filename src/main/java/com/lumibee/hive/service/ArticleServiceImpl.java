package com.lumibee.hive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumibee.hive.config.SlugGenerator;
import com.lumibee.hive.dto.*;
import com.lumibee.hive.mapper.*;
import com.lumibee.hive.model.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired private ArticleMapper articleMapper;
    @Autowired private PortfolioMapper portfolioMapper;
    @Autowired private UserService userService;
    @Autowired private UserMapper userMapper;
    @Autowired private TagService tagService;
    @Autowired private PortfolioService portfolioService;
    @Autowired private ArticleLikesMapper articleLikesMapper;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private ArticleFavoritesMapper favoriteMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getHomepageArticle(long pageNum, long pageSize) {
        Page<Article> articlePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.ArticleStatus.published)
                    .eq(Article::getDeleted, 0)
                    .orderByDesc(Article::getGmtModified);

        return getArticleExcerptDTOPage(pageNum, pageSize, articlePageRequest, queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDetailsDTO> selectAll() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                    .eq("status", Article.ArticleStatus.published)
                    .orderByDesc("gmt_modified");
        List<Article> articles = articleMapper.selectList(queryWrapper);
        List<ArticleDetailsDTO> articleDetailsDTOs = new ArrayList<>();
        for (Article article : articles) {
            ArticleDetailsDTO articleDetailsDTO = new ArticleDetailsDTO();
            BeanUtils.copyProperties(article, articleDetailsDTO);
            User user = userMapper.selectById(article.getUserId());
            if (user != null) {
                articleDetailsDTO.setUserName(user.getName());
            }
            articleDetailsDTOs.add(articleDetailsDTO);
        }
        return articleDetailsDTOs;
    }


    @Override
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

    @Override
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

    @Override
    @Transactional
    public ArticleDetailsDTO getArticleBySlug(String slug) {
        QueryWrapper<Article> wrapper = new QueryWrapper<> ();
        wrapper.eq("slug", slug).eq("deleted", 0);
        Article article = articleMapper.selectOne(wrapper);

        if (article == null) {
            return null; // 如果没有找到文章，返回null
        }

        articleMapper.incrementViewCount(article.getArticleId());
        User user = userService.selectById(article.getUserId());
        Portfolio portfolio = portfolioMapper.selectById(article.getPortfolioId());
        List<Tag> tags = tagService.selectTagsByArticleId(article.getArticleId());

        ArticleDetailsDTO articleDetailsDTO = new ArticleDetailsDTO();
        BeanUtils.copyProperties(article, articleDetailsDTO);
        if (user != null) {
            articleDetailsDTO.setUserName(user.getName());
            articleDetailsDTO.setAvatarUrl(user.getAvatarUrl());
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

        return articleDetailsDTO;
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

    @Override
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> selectArticleSummaries(int limit) {
        return articleMapper.selectArticleSummaries(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getArticlesByTagId(int tagId) {
        return articleMapper.getArticlesByTagId(tagId);
    }

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
        
        // 保存到Elasticsearch - 这是关键的缺失步骤
        articleRepository.save(articleDocument);

        return this.getArticleBySlug(article.getSlug());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> selectArticlesByPortfolioId(Integer id) {
        return articleMapper.selectArticlesByPortfolioId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countArticlesByUserId(Long id) {
        return articleMapper.countArticlesByUserId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> getArticlesByUserId(Long id) {
        return articleMapper.getArticlesByUserId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleExcerptDTO> selectFeaturedArticles() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                    .eq("status", Article.ArticleStatus.published)
                    .eq("is_featured", true)
                    .orderByDesc("gmt_modified");

        List<Article> featuredArticles = articleMapper.selectList(queryWrapper);
        if (featuredArticles.isEmpty()) {
            return new ArrayList<>(); // 如果没有精选文章，返回空列表
        }
        List<ArticleExcerptDTO> articleExcerptDTOS = new ArrayList<>();
        for (Article article : featuredArticles) {
            ArticleExcerptDTO articleDTO = new ArticleExcerptDTO();
            BeanUtils.copyProperties(article, articleDTO);
            articleExcerptDTOS.add(articleDTO);
        }

        return articleExcerptDTOS;
    }

    @Override
    @Transactional
    public ArticleDetailsDTO saveDraft(ArticlePublishRequestDTO requestDTO, Long userId) {
        Article article = new Article();
        article.setTitle(requestDTO.getTitle());
        article.setContent(requestDTO.getContent());
        article.setExcerpt(requestDTO.getExcerpt());
        article.setUserId(userId);
        article.setGmtCreate(LocalDateTime.now());
        article.setGmtModified(LocalDateTime.now());

        article.setStatus(Article.ArticleStatus.draft);
        article.setSlug(createUniqueSlug(requestDTO.getTitle()));

        articleMapper.insert(article);

        return getArticleBySlug(article.getSlug());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleExcerptDTO> getArticlesByUserId(Long userId, long pageNum, long pageSize) {
        Page<Article> articlePageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq(Article::getUserId, userId)
                    .eq(Article::getDeleted, 0)
                    .eq(Article::getStatus, Article.ArticleStatus.draft)
                    .orderByDesc(Article::getGmtModified);

        return getArticleExcerptDTOPage(pageNum, pageSize, articlePageRequest, queryWrapper);
    }

    @NotNull
    private Page<ArticleExcerptDTO> getArticleExcerptDTOPage(long pageNum, long pageSize, Page<Article> articlePageRequest, LambdaQueryWrapper<Article> queryWrapper) {
        Page<Article> articlePage = articleMapper.selectPage(articlePageRequest, queryWrapper);
        List<Article> articleList = articlePage.getRecords();

        if (articleList.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        List<Long> userIds = articleList.stream()
                .map(Article::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<ArticleExcerptDTO> articleDTOList = articleList.stream().map(article -> {
            ArticleExcerptDTO articleDTO = new ArticleExcerptDTO();
            BeanUtils.copyProperties(article, articleDTO);
            User user = userMap.get(article.getUserId());
            if (user != null) {
                articleDTO.setUserName(user.getName());
                articleDTO.setAvatarUrl(user.getAvatarUrl());
            }
            return articleDTO;
        }).collect(Collectors.toList());

        Page<ArticleExcerptDTO> articleDTOPage = new Page<>(
                articlePage.getCurrent(),
                articlePage.getSize(),
                articlePage.getTotal()
        );

        articleDTOPage.setRecords(articleDTOList);

        return articleDTOPage;
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
    @Transactional
    public ArticleDetailsDTO updateDraft(ArticlePublishRequestDTO requestDTO, Long userId){
        Article article;

        if (requestDTO.getArticleId() != null) {
            article = articleMapper.selectById(requestDTO.getArticleId());
            if (!article.getUserId().equals(userId)) {
                throw new RuntimeException("草稿文章不存在或不属于当前用户");
            }
        }else {
            article = new Article();
            article.setUserId(userId);
            article.setStatus(Article.ArticleStatus.draft);
        }

        article.setExcerpt(requestDTO.getExcerpt());
        article.setTitle(requestDTO.getTitle());
        article.setContent(requestDTO.getContent());
        article.setGmtModified(LocalDateTime.now());
        article.setSlug(createUniqueSlug(requestDTO.getTitle()));
        articleMapper.updateById(article);

        return getArticleBySlug(article.getSlug());
    }

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

        return getArticleBySlug(existingArticle.getSlug());
    }

    @Override
    @Transactional
    public ArticleDetailsDTO deleteArticleById(Integer articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        int result = articleMapper.deleteById(articleId);
        if (result == 0) {
            throw new RuntimeException("删除文章失败");
        }
        articleRepository.deleteById(articleId);

        return convertToArticleDetailsDTO(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDocument> selectArticles(String query) {
        return articleRepository.findByTitleOrContentWithPrefix(query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDocument> selectRelatedArticles(ArticleDetailsDTO currentArticle, int limit) {
        if (currentArticle == null || currentArticle.getArticleId() == null) {
            return new ArrayList<>();
        }

        List<ArticleDocument> relatedArticles = articleRepository.selectRelatedArticles(
                currentArticle.getTitle(),
                currentArticle.getContent(),
                currentArticle.getArticleId());

        if (relatedArticles.size() > limit) {
            return relatedArticles.subList(0, limit);
        }

        return relatedArticles;
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
}
