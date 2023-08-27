package org.pipeman.sp_api.notifications;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import nl.martijndwars.webpush.Subscription.Keys;
import org.jose4j.lang.JoseException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SubscriptionApi {
    public static void getSubscriber(Context ctx) {
        Subscriber subscriber = Database.getSubscriber(getEndpoint(ctx))
                .orElseThrow(() -> new BadRequestResponse("Subscription not found"));

        ctx.result(subscriber.filterAsString());
    }

    public static void putSubscriber(Context ctx) {
        JSONObject body = get("Invalid JSON format", () -> new JSONObject(ctx.body()));

        String endpoint = get("Missing endpoint", () -> body.getString("endpoint").trim());

        String key = get("Missing key", () -> body.getString("key"));
        String auth = get("Missing auth", () -> body.getString("auth"));

        if (Database.getSubscriber(endpoint).isPresent()) {
            throw new BadRequestResponse("Subscription already exists");
        }

        Subscriber subscriber = new Subscriber(new Keys(key, auth), endpoint);
        Database.putSubscriber(endpoint, subscriber);
        try {
            NotificationHandler.sendNotification(subscriber, "yoooo");
        } catch (JoseException | GeneralSecurityException | IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void patchSubscriber(Context ctx) {
        List<String> filter = get("Malformed filter", () -> list(ctx.body().split(",")));
        filter.replaceAll(String::trim);
        filter.removeIf(String::isEmpty);

        String endpoint = getEndpoint(ctx);
        Subscriber subscriber = Database.getSubscriber(endpoint)
                .orElseThrow(() -> new BadRequestResponse("Subscription not found"));

        subscriber.setFilter(filter);
        Database.putSubscriber(endpoint, subscriber);
    }

    public static void deleteSubscriber(Context ctx) {
        if (Database.deleteSubscriber(getEndpoint(ctx))) {
            ctx.status(200);
        } else {
            ctx.status(400).result("Subscription not found");
        }
    }

    private static <T> T get(String message, ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ignored) {
            throw new BadRequestResponse(message);
        }
    }

    private static String getEndpoint(Context ctx) {
        return ctx.pathParam("endpoint").strip();
    }

    private static List<String> list(String[] array) {
        return new ArrayList<>(List.of(array));
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
