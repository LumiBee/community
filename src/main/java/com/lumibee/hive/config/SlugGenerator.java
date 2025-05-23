package com.lumibee.hive.config;

import com.github.promeg.pinyinhelper.Pinyin;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SlugGenerator {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]"); // 移除非单词字符和连字符
    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); // 匹配空白符
    private static final Pattern CONSECUTIVE_HYPHENS = Pattern.compile("-{2,}"); // 匹配连续的连字符
    private static final Pattern EDGE_HYPHENS = Pattern.compile("^-|-$"); // 匹配开头或结尾的连字符

    public static String generateSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String title = input.toLowerCase(Locale.ENGLISH);

        // 1. 对中文进行拼音转换
        title = Pinyin.toPinyin(title, "-").toLowerCase(Locale.ENGLISH);


        // 2. 标准化（例如，将带音调的字符转为基本字符）
         title = Normalizer.normalize(title, Normalizer.Form.NFD);
         title = title.replaceAll("\\p{InCombiningDiacriticalMarks}", "");

        title = title.replace('đ', 'd');
        title = title.replace('ħ', 'h');
        title = title.replace('ı', 'i');
        title = title.replace('ĸ', 'k');
        title = title.replace('ŀ', 'l');
        title = title.replace('ł', 'l');
        title = title.replace('ß', 's');
        title = title.replace('ŧ', 't');
        title = title.replace("n̈", "n");
        title = title.replace('ä', 'a');
        title = title.replace('æ', 'a');
        title = title.replace('ö', 'o');
        title = title.replace('ø', 'o');
        title = title.replace('ü', 'u');

        // 3. 替换空格为连字符
        title = WHITESPACE.matcher(title).replaceAll("-");

        // 4. 移除非字母数字字符 (除了连字符)
        title = NONLATIN.matcher(title).replaceAll("");

        // 5. 将多个连续的连字符替换为单个连字符
        title = CONSECUTIVE_HYPHENS.matcher(title).replaceAll("-");

        // 6. 移除开头和结尾的连字符
        title = EDGE_HYPHENS.matcher(title).replaceAll("");


         if (title.length() > 100) {
             title = title.substring(0, 100);
             title = EDGE_HYPHENS.matcher(title).replaceAll("");
         }

        return title;
    }

}