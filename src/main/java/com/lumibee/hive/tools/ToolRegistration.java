package com.lumibee.hive.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;
    @Value("${qweather.api.key}")
    private String qweatherApiKey;
    @Value("${qweather.api.weather-base-url}")
    private String qweatherApiBaseUrl;
    @Value("${qweather.api.geo-base-url}")
    private String geoApiBaseUrl;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TimeTool timeTool = new TimeTool();
        WeatherQueryTool weatherQueryTool = new WeatherQueryTool(qweatherApiKey, qweatherApiBaseUrl, geoApiBaseUrl, HttpClient.newHttpClient());
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
            fileOperationTool,
            webSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool,
            timeTool,
            weatherQueryTool,
            terminateTool
        );
    }
}
