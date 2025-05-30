package com.example.handler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

public class ForwardingHandler implements Route {
    private static final Logger LOGGER = Logger.getLogger(ForwardingHandler.class.getName());

    private final Supplier<String> baseUrl;
    private final String pathTemplate;
    private final HttpMethod method;

    private static final ConcurrentHashMap<String, String> responseCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> idToKeys = new ConcurrentHashMap<>();

    public enum HttpMethod {
        GET, POST
    }

    public ForwardingHandler(Supplier<String> baseUrl, String pathTemplate, HttpMethod method) {
        this.baseUrl = baseUrl;
        this.pathTemplate = pathTemplate;
        this.method = method;
    }

    @Override
    public Object handle(Request request, Response response) {
        String path = resolvePath(pathTemplate, request);
        String query = request.queryString();
        String url = path + (query != null ? "?" + query : "");

        if (method == HttpMethod.GET) {
            String cached = responseCache.get(url);
            if (cached != null) {
                LOGGER.info("Cache hit for " + url);
                return cached;
            }
        }

        try {
            HttpResponse<String> forwardResponse = sendRequest(baseUrl.get() + url);
            LOGGER.info("\nReceived response from forwarded server\nStatus: " + forwardResponse.getStatus() + "\nBody: " + forwardResponse.getBody());
            response.status(forwardResponse.getStatus());

            if (forwardResponse.getStatus() == 200) {
                if (method == HttpMethod.GET) {
                    responseCache.put(url, forwardResponse.getBody());

                    // Extract id from path if present
                    Matcher idMatcher = Pattern.compile("/(\\d+)$").matcher(path);
                    if (idMatcher.find()) {
                        String id = idMatcher.group(1);
                        idToKeys.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(url);
                    }
                }
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

    public static void invalidateById(String id) {
        Set<String> keys = idToKeys.remove(id);
        if (keys != null) {
            for (String key : keys) {
                responseCache.remove(key);
                LOGGER.info("Invalidated cache entry for: " + key);
            }
        }
    }
}
