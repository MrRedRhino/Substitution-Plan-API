package org.pipeman.notifications;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.pipeman.sp_api.Main;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

public class NotificationHandler {
    private static final PushService service;

    static {
        try {
            service = new PushService(
                    Main.conf().vapidPublicKey,
                    Main.conf().vapidPrivateKey,
                    Main.conf().vapidSubject
            );
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void sendNotification(Subscriber subscriber, String message) throws JoseException, GeneralSecurityException, IOException, ExecutionException, InterruptedException {
        service.send(new Notification(subscriber.subscription(), message));
    }
}
