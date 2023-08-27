package org.pipeman.notifications;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import nl.martijndwars.webpush.Subscription.Keys;
import org.json.JSONObject;

import java.util.List;

public class SubscriptionApi {
    public static void getSubscriber(Context ctx) {
        Subscriber subscriber = Database.getSubscriber(getEndpoint(ctx))
                .orElseThrow(() -> new BadRequestResponse("Subscriber not found"));

        ctx.json(subscriber.filter());
    }

    public static void putSubscriber(Context ctx) {
        JSONObject body = get("Invalid JSON format", () -> new JSONObject(ctx.body()));

        String endpoint = get("Missing endpoint", () -> body.getString("endpoint").trim());

        String key = get("Missing key", () -> body.getString("key"));
        String auth = get("Missing auth", () -> body.getString("auth"));

        Database.getSubscriber(endpoint).orElseThrow(() -> new BadRequestResponse("Subscription already exists"));

        Subscriber subscriber = new Subscriber(new Keys(key, auth), endpoint);
        Database.putSubscriber(endpoint, subscriber);
    }

    public static void patchSubscriber(Context ctx) {
        List<String> filter = get("Malformed filter", () -> List.of(ctx.body().split(",")));

        String endpoint = getEndpoint(ctx);
        Subscriber subscriber = Database.getSubscriber(endpoint)
                .orElseThrow(() -> new BadRequestResponse("Subscriber not found"));

        subscriber.setFilter(filter);
        Database.putSubscriber(endpoint, subscriber);
    }

    public static void deleteSubscriber(Context ctx) {
        if (Database.deleteSubscriber(getEndpoint(ctx))) {
            ctx.status(200);
        } else {
            ctx.status(400).result("Subscriber not found");
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

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
