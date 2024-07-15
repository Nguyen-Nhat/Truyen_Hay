package com.ckxnhat.truyenhay.services.export_file;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExportEpubService implements ExportFileService {
    @Override
    public ByteArrayInputStream exportFile(String content) {
        Book book = new Book();
        Metadata metadata = book.getMetadata();
        metadata.addTitle("Sample Book");
        metadata.addAuthor(new Author("First Name", "Last Name"));
        book.addSection("Content", new Resource(content.getBytes(), "content.html"));

        // Tạo và ghi file EPUB
        EpubWriter epubWriter = new EpubWriter();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            epubWriter.write(book, outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            System.out.println("Error exporting EPUB file: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getName() {
        return "epub";
    }
}
