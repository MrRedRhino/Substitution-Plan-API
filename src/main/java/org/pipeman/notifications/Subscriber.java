package org.pipeman.notifications;

import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Subscription.Keys;
import org.json.JSONObject;

import java.util.List;

public class Subscriber {
    private List<String> filter;
    private final Subscription subscription;

    public Subscriber(String endpoint, String string) {
        this(endpoint, new JSONObject(string));
    }

    public Subscriber(String endpoint, JSONObject object) {
        filter = List.of(object.getString("filter").split(","));

        Keys keys = new Keys(
                object.getString("key"),
                object.getString("auth")
        );
        subscription = new Subscription(endpoint, keys);
    }

    public Subscriber(Keys keys, String endpoint) {
        this.filter = List.of();
        this.subscription = new Subscription(endpoint, keys);
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("filter", filterAsString())
                .put("key", subscription.keys.p256dh)
                .put("auth", subscription.keys.auth);
    }

    public void setFilter(List<String> newFilter) {
        this.filter = newFilter;
    }

    public List<String> filter() {
        return filter;
    }

    public Subscription subscription() {
        return subscription;
    }

    public String filterAsString() {
        return String.join(",", filter);
    }
}
