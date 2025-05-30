package com.example.Catalog.handler;

import com.example.ReplicaDiscovery;
import spark.Request;
import spark.Response;
import spark.Route;

public class FlushDNS implements Route {
    @Override
    public Object handle(Request request, Response response) {
        System.out.println("Flushing DNS cache");
        ReplicaDiscovery.getInstance().refreshAll();
        System.out.println("CATALOG_REPLICAS = " + ReplicaDiscovery.getInstance().getAll("catalog"));
        response.status(200);
        return "OK";
    }
}
