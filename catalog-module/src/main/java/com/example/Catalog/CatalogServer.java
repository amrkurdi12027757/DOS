package com.example.Catalog;

import com.example.Catalog.handler.InfoHandler;
import com.example.Catalog.handler.SearchHandler;
import com.example.Catalog.handler.UpdateStockHandler;
import com.example.Catalog.pojo.Book;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static spark.Spark.*;

public class CatalogServer {

    public static final String BOOKS_FILE_PATH = "./books.csv";
    public static final AtomicReference<List<Book>> BOOKS = new AtomicReference<>(Book.readBooksFromFile(BOOKS_FILE_PATH));
    public static final Gson GSON = new Gson();

    public static void main(String[] args) {

        port(4575);
        get("/info/:id", new InfoHandler());

        get("/search/:topic", new SearchHandler());

        put("/updateStock/:bookID", new UpdateStockHandler());
    }
}

