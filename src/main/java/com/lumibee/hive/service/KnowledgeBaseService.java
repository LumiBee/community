package com.lumibee.hive.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class KnowledgeBaseService {

    private final VectorStore vectorStore;

    public KnowledgeBaseService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void processAndStorePdf(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("pdf", ".pdf");
        file.transferTo(tempFile);

        // 加载与解析 PDF
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(tempFile.toURI().toString(),
                PdfDocumentReaderConfig.builder().build());
        List<Document> documents = pdfReader.get();

        // 文本切片
        TokenTextSplitter textSplitter = new TokenTextSplitter(800, 200, 5, 10000, true);
        List<Document> chunkedDocuments = textSplitter.split(documents);

        // 元数据打标
        String originalFilename = file.getOriginalFilename();
        chunkedDocuments.forEach(doc -> {
            doc.getMetadata().put("source_filename", originalFilename);
            doc.getMetadata().put("doc_type", "pdf");
        });

        // 向量化并写入 ES
        vectorStore.accept(chunkedDocuments);

        tempFile.delete();
    }
}
