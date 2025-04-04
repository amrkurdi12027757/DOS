package com.example;

import com.example.handler.ForwardingHandler;

import static spark.Spark.*;

public class Frontend {
    public static final String CATALOG_URL = "http://catalog:4575";
    public static final String ORDER_URL = "http://order:3300";

    public static void main(String[] args) {
        port(4567);

        get("/search/:topic",
                new ForwardingHandler(CATALOG_URL,
                        "/search/:topic",
                        ForwardingHandler.HttpMethod.GET));

        post("/purchase/:itemId",
                new ForwardingHandler(ORDER_URL,
                        "/purchase/:itemId",
                        ForwardingHandler.HttpMethod.POST));

        get("/info/:id",
                new ForwardingHandler(CATALOG_URL,
                        "/info/:id",
                        ForwardingHandler.HttpMethod.GET));
    }
}