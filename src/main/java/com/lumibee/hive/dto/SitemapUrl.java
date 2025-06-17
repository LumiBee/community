package com.lumibee.hive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SitemapUrl {
    private String loc;
    private LocalDate lastMod;
    private String changeFreq = "weekly";
    private double priority = 0.8;

    public SitemapUrl(String loc, LocalDate lastMod) {
        this.loc = loc;
        this.lastMod = LocalDate.now();
    }
}
