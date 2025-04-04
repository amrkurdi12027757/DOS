package com.example.Order.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.Request;
import spark.Response;
import spark.Route;

public class PurchaseHandler implements Route {
    private static final String CATALOG_SERVER_URL = "http://catalog:4575";

    @Override
    public Object handle(Request request, Response response) {
        String itemId = request.params(":itemId");
        boolean purchaseResult = purchase(itemId);

        if (purchaseResult) {
            return "Purchase successful for item ID: " + itemId;
        } else {
            response.status(400);
            return "Purchase failed for item ID: " + itemId + ". Item may be out of stock or not found.";
        }
    }

    private static boolean purchase(String itemId) {
        try {
            HttpResponse<String> infoResponse = Unirest.get(CATALOG_SERVER_URL + "/info/" + itemId).asString();
            if (infoResponse.getStatus() == 200) {
                JsonObject bookInfo = JsonParser.parseString(infoResponse.getBody()).getAsJsonObject();
                int stock = bookInfo.get("stock").getAsInt();
                System.out.println(stock);

                if (stock > 0) {
                    HttpResponse<String> updateResponse = Unirest.put(CATALOG_SERVER_URL + "/updateStock/" + itemId).asString();
                    return updateResponse.getStatus() == 200;
                }
            }
        } catch (UnirestException e) {
            System.err.println("Error communicating with catalog server: " + e.getMessage());
        }
        return false;
    }
}
