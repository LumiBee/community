package com.lumibee.hive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lumibee.hive.service.SitemapService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "站点地图生成", description = "站点地图生成相关的 API 接口")
public class SitemapController {

    @Autowired private SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    @Operation(summary = "生成站点地图", description = "生成XML格式的站点地图")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功", 
                    content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/xml"))
    })
    public ResponseEntity<String> getSitemap() {
        String sitemap = sitemapService.generateSitemapXml();
        return ResponseEntity.ok(sitemap);
    }
}