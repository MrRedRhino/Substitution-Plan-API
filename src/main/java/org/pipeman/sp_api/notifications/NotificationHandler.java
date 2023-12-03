package org.pipeman.sp_api.notifications;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushAsyncService;
import org.asynchttpclient.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.pipeman.sp_api.Main;
import org.pipeman.sp_api.pdfs.Day;
import org.pipeman.sp_api.pdfs.Plan;
import org.pipeman.sp_api.pdfs.PlanData.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NotificationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);
    private static final PushAsyncService service;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            service = new PushAsyncService(
                    Main.conf().vapidPublicKey,
                    Main.conf().vapidPrivateKey,
                    Main.conf().vapidSubject
            );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendNotification(Subscriber subscriber, String message) {
        try {
            LOGGER.info("Sending notification to {}; filter: {}", subscriber.subscription().endpoint, subscriber.filter());
            service.send(new Notification(subscriber.subscription(), message))
                    .thenAccept(s -> deleteSubscriptionIfNecessary(s, subscriber.subscription().endpoint));
        } catch (GeneralSecurityException | IOException | JoseException e) {
            LOGGER.warn("Failed to send notification", e);
        }
    }

    private static void deleteSubscriptionIfNecessary(Response response, String endpoint) {
        int status = response.getStatusCode();
        if (status < 200 || status >= 300) {
            Database.deleteSubscriber(endpoint);
        }
    }

    public static void handlePlanUpdate(Day day, Plan plan) {
        Database.forEachSubscriber(subscriber -> {
            List<Row> filtered = filter(subscriber.filter(), plan.data().substitutions());
            if (filtered.isEmpty()) return;

            String message = createMessage(day, filtered.size());
            sendNotification(subscriber, message);
        });
    }

    private static List<Row> filter(Set<String> filter, Row[] rows) {
        if (filter.isEmpty()) {
            return List.of(rows);
        }

        List<Row> filtered = new ArrayList<>();
        for (Row row : rows) {
            if (filter.contains(row.clazz())) {
                filtered.add(row);
            }
        }

        return filtered;
    }

    private static String createMessage(Day day, int entries) {
        if (entries == 1) {
            return "Der %s Vertretungsplan enthält einen Eintrag, der Dich betrifft."
                    .formatted(day.localization());
        } else {
            return "Der %s Vertretungsplan enthält %s Einträge, die Dich betreffen."
                    .formatted(day.localization(), entries);
        }
    }
}
