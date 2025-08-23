package com.lumibee.hive.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class TimeTool {

    @Tool(description = "Get the current time in a specified timezone")
    public String getCurrentTimeZone() {
        // 获取当前时间，使用ZonedDateTime获取时区信息
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        // 根据 ZonedDateTime获取当前时区
        ZoneId zoneId = zonedDateTime.getZone();
        // 格式化当前时间为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.CHINA); // 可根据需要更改区域设置
        String formattedTime = zonedDateTime.format(formatter);

        return "当前的时区是: " + zoneId + ", 当前时间是: " + formattedTime;
    }
}
