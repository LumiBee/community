package com.lumibee.hive.controller;

import com.lumibee.hive.service.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SitemapController {

    @Autowired private SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<String> getSitemap() {
        String sitemap = sitemapService.generateSitemapXml();
        return ResponseEntity.ok(sitemap);
    }
}