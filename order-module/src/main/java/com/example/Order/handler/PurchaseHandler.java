package com.example.Order.handler;

import com.example.ReplicaDiscovery;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.concurrent.atomic.AtomicInteger;

public class PurchaseHandler implements Route {
    private static final AtomicInteger CATALOG_SERVER_INDEX = new AtomicInteger(0);

    @Override
    public Object handle(Request request, Response response) {
        String itemId = request.params(":itemId");
        boolean purchaseResult = purchase(itemId);

        if (purchaseResult) {
            HttpClient.newHttpClient().
                    sendAsync(
                            HttpRequest.newBuilder().uri(
                                    URI.create("http://" +
                                            ReplicaDiscovery.getInstance().
                                                    getAll("gateway")
                                                    .get(0) +
                                                    ":4567" +
                                            "/invalidateCache/" + itemId)
                            ).build(),
                            java.net.http.HttpResponse.BodyHandlers.discarding()
                    );
            return "Purchase successful for item ID: " + itemId;
        } else {
            response.status(400);
            return "Purchase failed for item ID: " + itemId + ". Item may be out of stock or not found.";
        }
    }

    private static boolean purchase(String itemId) {
        try {
            HttpResponse<String> infoResponse = Unirest.get(getCatalogServerUrl() + "/info/" + itemId).asString();
            if (infoResponse.getStatus() == 200) {
                JsonObject bookInfo = JsonParser.parseString(infoResponse.getBody()).getAsJsonObject();
                int stock = bookInfo.get("stock").getAsInt();
                System.out.println(stock);

                if (stock > 0) {
                    HttpResponse<String> updateResponse = Unirest.put(getCatalogServerUrl() + "/updateStock/" + itemId).asString();
                    return updateResponse.getStatus() == 200;
                }
            }
        } catch (UnirestException e) {
            System.err.println("Error communicating with catalog server: " + e.getMessage());
        }
        return false;
    }

    private static String getCatalogServerUrl() {
        int index = CATALOG_SERVER_INDEX.getAndIncrement() %
                ReplicaDiscovery.getInstance().
                        getOthers("catalog").
                        size();
        return "http://" + ReplicaDiscovery.getInstance().getOthers("catalog").get(index) + ":4575";
    }
}
