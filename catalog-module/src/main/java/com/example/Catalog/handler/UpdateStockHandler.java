package com.example.Catalog.handler;

import com.example.Catalog.pojo.Book;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.example.Catalog.CatalogServer.BOOKS;
import static com.example.Catalog.CatalogServer.BOOKS_FILE_PATH;

public class UpdateStockHandler implements Route {
    @Override
    public Object handle(Request request, Response response) throws IOException {
        String bookID = request.params(":bookID");
        Path path = Paths.get(BOOKS_FILE_PATH);
        List<String> lines = Files.readAllLines(path);
        List<String> updatedLines = new ArrayList<>();

        boolean found = false;
        for (String line : lines) {
            if (line.startsWith(bookID + ",")) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int stock = Integer.parseInt(parts[3]);
                    if (stock > 0) {
                        stock--;
                        parts[3] = String.valueOf(stock);
                        line = String.join(",", parts);
                        found = true;
                    }
                }
            }
            updatedLines.add(line);
        }
        if (found) {
            Files.write(path, updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            BOOKS.set(Book.readBooksFromFile(BOOKS_FILE_PATH));
            return "Stock updated successfully.";
        } else {
            response.status(404);
            return "Book not found or stock is 0.";
        }
    }
}
