package com.lumibee.hive.service;

import com.lumibee.hive.dto.ArticleExcerptDTO;
import com.lumibee.hive.dto.SitemapUrl;
import com.lumibee.hive.dto.TagDTO;
import com.lumibee.hive.mapper.ArticleMapper;
import com.lumibee.hive.mapper.PortfolioMapper;
import com.lumibee.hive.mapper.TagMapper;
import com.lumibee.hive.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SitemapService{
    private final String BASE_URL = "https://www.hivelumi.com";

    @Autowired private ArticleMapper articleMapper;
    @Autowired private TagMapper tagMapper;
    @Autowired private PortfolioMapper portfolioMapper;

    public String generateSitemapXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        List<SitemapUrl> urls = new ArrayList<>();

        // 添加静态资源
        urls.add(new SitemapUrl(BASE_URL + "/", LocalDate.now()));
        urls.add(new SitemapUrl(BASE_URL + "/portfolio", LocalDate.now()));
        urls.add(new SitemapUrl(BASE_URL + "/tags", LocalDate.now()));

        // 添加文章资源
        List<ArticleExcerptDTO> articles = articleMapper.selectSitemapDetails();
        for (ArticleExcerptDTO article : articles) {
            urls.add(new SitemapUrl(BASE_URL + "/articles/" + article.getSlug(), article.getGmtModified().toLocalDate()));
        }

        // 添加标签资源
        List<TagDTO> tags = tagMapper.selectSitemapDetails();
        for (TagDTO tag : tags) {
            urls.add(new SitemapUrl(BASE_URL + "/tags/" + tag.getSlug(), LocalDate.now()));
        }

        // 添加作品集资源
        List<String> portfolios = portfolioMapper.selectSitemapDetails();
        for (String portfolio : portfolios) {
            urls.add(new SitemapUrl(BASE_URL + "/portfolio/" + portfolio, LocalDate.now()));
        }

        // 生成XML
        for (SitemapUrl url : urls) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(url.getLoc()).append("</loc>\n");
            xml.append("    <lastmod>").append(url.getLastMod()).append("</lastmod>\n");
            xml.append("    <changefreq>").append(url.getChangeFreq()).append("</changefreq>\n");
            xml.append("    <priority>").append(url.getPriority()).append("</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }
}
