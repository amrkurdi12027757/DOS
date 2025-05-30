package com.example;

import com.example.handler.ForwardingHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static spark.Spark.*;

public class Frontend {
    public static final AtomicInteger CATALOG_SERVER_INDEX = new AtomicInteger(0);
    public static final AtomicInteger ORDER_SERVER_INDEX = new AtomicInteger(0);

    public static void main(String[] args) {
        port(4567);

        get("/search/:topic",
                new ForwardingHandler(() ->
                        "http://" +
                                getBaseUrl("catalog", CATALOG_SERVER_INDEX) +
                                ":4575",
                        "/search/:topic",
                        ForwardingHandler.HttpMethod.GET));

        post("/purchase/:itemId",
                new ForwardingHandler(() ->
                        "http://" +
                                getBaseUrl("order", ORDER_SERVER_INDEX) +
                                ":3300",
                        "/purchase/:itemId",
                        ForwardingHandler.HttpMethod.POST));

        get("/info/:id",
                new ForwardingHandler(() ->
                        "http://" +
                                getBaseUrl("catalog", CATALOG_SERVER_INDEX) +
                                ":4575",
                        "/info/:id",
                        ForwardingHandler.HttpMethod.GET));

        get("/invalidateCache/:id",
                (request, response) -> {
                    ForwardingHandler.invalidateById(request.params(":id"));
                    response.status(200);
                    return "OK";
                }
        );
    }

    private static String getBaseUrl(String serviceName, AtomicInteger serverIndex) {
        return ReplicaDiscovery.getInstance().
                getOthers(serviceName).
                get(serverIndex.
                        getAndIncrement() %
                        ReplicaDiscovery.getInstance()
                                .getOthers(serviceName).
                                size());
    }
}