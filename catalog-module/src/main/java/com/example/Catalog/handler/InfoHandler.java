package com.example.Catalog.handler;

import com.example.Catalog.pojo.Book;
import com.example.Catalog.pojo.BookInfoResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.logging.Logger;

import static com.example.Catalog.CatalogServer.BOOKS;
import static com.example.Catalog.CatalogServer.GSON;

public class InfoHandler implements Route {
    private static final Logger LOGGER = Logger.getLogger(InfoHandler.class.getName());

    @Override
    public Object handle(Request request, Response response) {
        LOGGER.info("Handling info request");
        int id = Integer.parseInt(request.params(":id"));

        Book resultBook = BOOKS.get().stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElse(null);

        if (resultBook != null) {

            BookInfoResponse bookInfo = new BookInfoResponse(resultBook.getTitle(), resultBook.getPrice(), resultBook.getStock());
            response.type("application/json");
            return GSON.toJson(bookInfo);
        } else {
            response.status(404);
            return GSON.toJson("Book not found");
        }
    }
}
