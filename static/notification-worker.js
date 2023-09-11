function receivePushNotification(event) {
    const notificationText = event.data.text();
    const title = "Vertretungsplan Änderungen"

    const options = {
        body: notificationText,
        icon: "https://pipeman.org/favicon.ico",
        vibrate: [500]
    };

    event.waitUntil(self.registration.showNotification(title, options));
}

self.addEventListener("push", receivePushNotification);
