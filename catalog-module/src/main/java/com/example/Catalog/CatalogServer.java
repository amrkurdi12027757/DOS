package com.example.Catalog;

import com.example.Catalog.handler.FlushDNS;
import com.example.Catalog.handler.InfoHandler;
import com.example.Catalog.handler.SearchHandler;
import com.example.Catalog.handler.UpdateStockHandler;
import com.example.Catalog.pojo.Book;
import com.example.ReplicaDiscovery;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static spark.Spark.*;

public class CatalogServer {
    public static final String BOOKS_FILE_PATH = "./books.csv";
    public static final AtomicReference<List<Book>> BOOKS = new AtomicReference<>(Book.readBooksFromFile(BOOKS_FILE_PATH));
    public static final Gson GSON = new Gson();

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(CatalogServer::flushOthersDNS));

        Thread.sleep(1000);
        flushOthersDNS();

        port(4575);
        get("/info/:id", new InfoHandler());

        get("/search/:topic", new SearchHandler());

        put("/updateStock/:bookID", new UpdateStockHandler());
        put("/internalUpdateStock/:bookID", new UpdateStockHandler.Internal());

        get("/flushDNS", new FlushDNS());
    }

    private static void flushOthersDNS() {
        for (String replica : ReplicaDiscovery.getInstance().getOthers("catalog")) {
            HttpClient.newHttpClient().sendAsync(
                    HttpRequest.newBuilder().uri(
                            URI.create("http://" + replica + ":4575" + "/flushDNS")
                    ).build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        }
    }
}

