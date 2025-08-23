package com.lumibee.hive.tools;

import cn.hutool.core.io.FileUtil;
import com.lumibee.hive.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(String fileName) {
        String filePath = FILE_DIR + "/" + fileName;

        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(
            @ToolParam(description = "File name to write to") String fileName,
            @ToolParam(description = "Content to write") String content) {

        String filePath = FILE_DIR + "/" + fileName;

        try {
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully: " + filePath;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }
    }

}
