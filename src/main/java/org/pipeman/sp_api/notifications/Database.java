package org.pipeman.sp_api.notifications;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Database {
    private static final DB database;

    static {
        Options options = new Options()
                .createIfMissing(true)
                .cacheSize(8 * 1024 * 1024);

        try {
            database = Iq80DBFactory.factory.open(new File("subscribers"), options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create database", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                database.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static Optional<Subscriber> getSubscriber(String endpoint) {
        byte[] bytes = database.get(endpoint.getBytes());
        if (bytes == null) return Optional.empty();

        return Optional.of(new Subscriber(endpoint, new String(bytes)));
    }

    public static void putSubscriber(String endpoint, Subscriber subscriber) {
        String jsonString = subscriber.toJson().toString();
        database.put(endpoint.getBytes(), jsonString.getBytes());
    }

    public static boolean deleteSubscriber(String endpoint) {
        if (getSubscriber(endpoint).isEmpty()) return false;

        database.delete(endpoint.getBytes());
        return true;
    }

    public static void forEachSubscriber(Consumer<Subscriber> consumer) {
        try (DBIterator iterator = database.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String endpoint = new String(iterator.peekNext().getKey());
                String value = new String(iterator.peekNext().getValue());

                consumer.accept(new Subscriber(endpoint, value));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Subscriber> getSubscribers() {
        List<Subscriber> list = new ArrayList<>();
        forEachSubscriber(list::add);
        return Collections.unmodifiableList(list);
    }
}
