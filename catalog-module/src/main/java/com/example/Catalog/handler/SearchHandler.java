package com.example.Catalog.handler;

import com.example.Catalog.pojo.Book;
import com.example.Catalog.pojo.BookIdNameResponse;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.example.Catalog.CatalogServer.BOOKS;
import static com.example.Catalog.CatalogServer.GSON;

public class SearchHandler implements Route {
    private static final Logger LOGGER = Logger.getLogger(InfoHandler.class.getName());
    @Override
    public Object handle(Request request, Response response) {
        LOGGER.info("Handling search request");
        String topic = request.params(":topic");

        List<Book> filteredBooks = BOOKS.get().stream()
                .filter(book -> book.getTopic().equalsIgnoreCase(topic))
                .toList();


        List<BookIdNameResponse> idNameResponses = filteredBooks.stream()
                .map(book -> new BookIdNameResponse(book.getId(), book.getTitle()))
                .collect(Collectors.toList());

        response.type("application/json");
        return GSON.toJson(idNameResponses);
    }
}
