package com.example.Order;


import com.example.Order.handler.PurchaseHandler;

import static spark.Spark.port;
import static spark.Spark.post;

public class OrderServer {

    public static void main(String[] args) {
        port(3300);
        post("/purchase/:itemId", new PurchaseHandler());
    }
}

