package com.lumibee.hive.tools;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;


public class WeatherQueryTool {

    private String apiKey;
    private String weatherApiBaseUrl;
    private String geoApiBaseUrl;
    private final HttpClient httpClient;

    public WeatherQueryTool(String apiKey, String weatherApiBaseUrl, String geoApiBaseUrl, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.weatherApiBaseUrl = weatherApiBaseUrl;
        this.geoApiBaseUrl = geoApiBaseUrl;
        this.httpClient = httpClient;
    }

    /**
     * 根据城市名称获取Location ID。
     * @param cityName The name of the city to search for (e.g., "北京", "london").
     * @return The location ID string if found, otherwise null.
     */
    private String getLocationId(String cityName) {
        try {
            String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String requestUrl = String.format("%slookup?location=%s", geoApiBaseUrl, encodedCityName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header("X-QW-Api-Key", apiKey)
                    .build();

            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                String jsonText;
                // --- 使用GZIPInputStream进行解压 ---
                try (InputStream stream = response.body();
                     GZIPInputStream gzipStream = new GZIPInputStream(stream);
                     InputStreamReader reader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                     BufferedReader bufferedReader = new BufferedReader(reader)) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    jsonText = sb.toString();
                }

                // 现在 jsonText 是干净的JSON字符串了
                JSONObject responseObject = new JSONObject(jsonText);
                if ("200".equals(responseObject.optString("code"))) {
                    JSONArray locations = responseObject.optJSONArray("location");
                    if (locations != null) {
                        return locations.getJSONObject(0).getString("id");
                    }
                }
            }
        } catch (Exception e) {
            // 打印详细错误，方便调试
            e.printStackTrace();
        }
        return null;
    }

    @Tool(description = "Get the daily weather forecast for a specific city for the next few days (e.g., 3d, 7d).")
    public String getDailyForecast(
            @ToolParam(description = "The name of the city to get the weather forecast for, e.g., '北京', 'Shanghai', 'London'.") String cityName,
            @ToolParam(description = "The forecast duration. Supported values are '3d', '7d', '10d', '15d'.") String forecastDays) {
        String locationId = getLocationId(cityName);
        if (locationId == null) {
            return "错误：未能找到城市 '" + cityName + "' 的ID。请检查城市名称。";
        }

        String requestUrl = String.format("%s%s?location=%s", weatherApiBaseUrl, forecastDays, locationId);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .header("X-QW-Api-Key", apiKey)
                    .build();

            HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                String jsonText;
                // --- 同样使用GZIPInputStream进行解压 ---
                try (InputStream stream = response.body();
                     GZIPInputStream gzipStream = new GZIPInputStream(stream);
                     InputStreamReader reader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
                     BufferedReader bufferedReader = new BufferedReader(reader)) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    jsonText = sb.toString();
                }

                return parseAndFormatWeatherResponse(jsonText);
            } else {
                return "错误：从天气API获取数据失败。状态码: " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "错误：API调用期间发生异常: " + e.getMessage();
        }
    }

    private String parseAndFormatWeatherResponse(String jsonBody) {
        try {
            JSONObject responseObject = new JSONObject(jsonBody);
            String code = responseObject.optString("code");

            if (!"200".equals(code)) {
                return "API Error: QWeather API returned status code " + code + ". Please check your parameters.";
            }

            JSONArray dailyForecasts = responseObject.getJSONArray("daily");
            StringBuilder formattedForecast = new StringBuilder("Weather Forecast:\n");

            for (int i = 0; i < dailyForecasts.length(); i++) {
                JSONObject day = dailyForecasts.getJSONObject(i);
                formattedForecast.append(String.format("- 日期: %s\n", day.optString("fxDate")));
                formattedForecast.append(String.format("  温度: %s°C (Min) to %s°C (Max)\n", day.optString("tempMin"), day.optString("tempMax")));
                formattedForecast.append(String.format("  白天天气: %s, 夜晚天气: %s\n", day.optString("textDay"), day.optString("textNight")));
                formattedForecast.append(String.format("  风力: %s at %s km/h\n", day.optString("windDirDay"), day.optString("windSpeedDay")));
                formattedForecast.append(String.format("  紫外线: %s\n", day.optString("uvIndex")));
                formattedForecast.append("\n");
            }

            return formattedForecast.toString();
        } catch (Exception e) {
            return "Error parsing weather data response: " + e.getMessage();
        }
    }
}