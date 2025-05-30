package com.example.Catalog.handler;

import com.example.Catalog.pojo.Book;
import com.example.ReplicaDiscovery;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.example.Catalog.CatalogServer.BOOKS;
import static com.example.Catalog.CatalogServer.BOOKS_FILE_PATH;

public class UpdateStockHandler implements Route {
    private static final Logger LOGGER = Logger.getLogger(InfoHandler.class.getName());

    public static final class Internal implements Route {
        @Override
        public Object handle(Request request, Response response) throws Exception {
            LOGGER.info("Handling internal update stock request");
            return new UpdateStockHandler().handle(request, response, true);
        }
    }

    @Override
    public Object handle(Request request, Response response) throws IOException {
        LOGGER.info("Handling update stock request");
        return handle(request, response, false);
    }

    private Object handle(Request request, Response response, boolean internal) throws IOException {
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
            if (!internal)
                ReplicaDiscovery.getInstance().getOthers("catalog").
                        forEach(replica -> {
                                    HttpClient.newHttpClient().sendAsync(
                                            HttpRequest.newBuilder().
                                                    PUT(HttpRequest.
                                                            BodyPublishers.
                                                            noBody())
                                                    .uri(
                                                            URI.create("http://" + replica + ":4575" + "/internalUpdateStock/" + bookID)
                                                    ).build(),
                                            HttpResponse.BodyHandlers.discarding()
                                    );
                                }
                        );
            return "Stock updated successfully.";
        } else {
            response.status(404);
            return "Book not found or stock is 0.";
        }
    }
}
