package com.example.Order;


import com.example.Order.handler.PurchaseHandler;
import com.example.ReplicaDiscovery;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static spark.Spark.port;
import static spark.Spark.post;

public class OrderServer {

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(OrderServer::flushOthersDNS));

        Thread.sleep(1000);
        flushOthersDNS();
        Thread.sleep(1000);
        port(3300);
        post("/purchase/:itemId", new PurchaseHandler());
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

