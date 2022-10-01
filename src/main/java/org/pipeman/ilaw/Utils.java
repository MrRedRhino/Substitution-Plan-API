package org.pipeman.ilaw;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<HttpResponse<String>> followRedirects(String url, HttpClient client) throws IOException, InterruptedException {
        String[] urlAndDomain = splitDomainAndUrl(url);
        return followRedirects(urlAndDomain[0], urlAndDomain[1], client);
    }

    public static List<HttpResponse<String>> followRedirects(String domain, String path, HttpClient client) throws IOException, InterruptedException {
        List<HttpResponse<String>> out = new ArrayList<>();

        HttpResponse<String> lastResponse = client.send(createRequest(domain + path), HttpResponse.BodyHandlers.ofString());
        out.add(lastResponse);

        while (lastResponse.statusCode() == 301 || lastResponse.statusCode() == 302) {
            lastResponse = client.send(createRequest(createUrl(domain, lastResponse.headers().firstValue("location").orElse(""))), HttpResponse.BodyHandlers.ofString());
            out.add(lastResponse);
        }

        return out;
    }

    public static String[] splitDomainAndUrl(String url) {
        String[] out = new String[2];
        if (url.startsWith("/")) {
            out[0] = "";
            out[1] = url;
        } else {
            int domain = url.indexOf("://") + 3;
            int path = url.substring(domain).indexOf("/") + domain;

            out[0] = url.substring(0, path);
            out[1] = url.substring(path);
        }
        return out;
    }

    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static String createUrl(String domain, String path) {
        return path.startsWith("https://") || path.startsWith("http://") ? path : domain + path;
    }

    public static HttpRequest createRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
    }
}
