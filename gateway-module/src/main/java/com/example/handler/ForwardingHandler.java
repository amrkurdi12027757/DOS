package com.example.handler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForwardingHandler implements Route {
    private static final Logger LOGGER = Logger.getLogger(ForwardingHandler.class.getName());
    private final String baseUrl;
    private final String pathTemplate;
    private final HttpMethod method;

    public enum HttpMethod {//Only for now as Amr only used POST,GET
        GET, POST
    }

    public ForwardingHandler(String baseUrl, String pathTemplate, HttpMethod method) {
        this.baseUrl = baseUrl;
        this.pathTemplate = pathTemplate;
        this.method = method;
    }

    @Override
    public Object handle(Request request, Response response) {
        String path = resolvePath(pathTemplate, request);
        try {

            HttpResponse<String> forwardResponse = sendRequest(baseUrl + path);
            LOGGER.info("\nReceived response from forwarded server\nStatus: " + forwardResponse.getStatus() + "\nBody: " + forwardResponse.getBody());
            response.status(forwardResponse.getStatus());

            if (forwardResponse.getStatus() == 200) {
                return forwardResponse.getBody();
            } else {
                return "Forwarding error: " + forwardResponse.getBody();
            }
        } catch (UnirestException e) {
            response.status(500);
            return "Internal server error";
        }
    }

    private HttpResponse<String> sendRequest(String url) throws UnirestException {
        LOGGER.info("\nForwarding request to: " + url);
        return switch (method) {
            case GET -> Unirest.get(url).asString();
            case POST -> Unirest.post(url).asString();
            default -> throw new IllegalArgumentException("Unsupported HTTP method");
        };
    }

    private String resolvePath(String template, Request request) {
        String resolved = template;
        Matcher matcher = Pattern.compile(":(\\w+)").matcher(template);
        while (matcher.find()) {
            String param = matcher.group(1);
            String value = request.params(param);
            if (value != null) {
                resolved = resolved.replace(":" + param, value);
            }
        }
        return resolved;
    }

}
