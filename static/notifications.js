let filter;
let editTimeout;
let endpoint;

navigator.serviceWorker.register("notification-worker.js").then();

function filterEdited() {
    const timeout = setTimeout(() => uploadFilter(), 1000);

    if (editTimeout != null) {
        clearTimeout(editTimeout);
    }
    editTimeout = timeout;
    filter = document.getElementById("filter").value;
}

function uploadFilter() {
    fetch("api/subscriptions/?endpoint=" + endpoint, {
        method: "PATCH",
        body: filter
    }).then();
}

function toggleNotifications() {
    const toggle = document.getElementById("notification-toggle");
    if (toggle.checked) {
        enable();
    } else {
        disable();
    }
}

async function load() {
    const serviceWorker = await navigator.serviceWorker.ready;
    const subscription = await serviceWorker.pushManager.getSubscription();
    if (subscription == null) {
        filter = "";
        return false;
    }

    endpoint = encodeURIComponent(subscription.endpoint);
    const response = await fetch("api/subscriptions/?endpoint=" + endpoint);
    if (response.status === 200) {
        return await response.text().then(text => {
            filter = text;
            return true;
        });
    } else {
        filter = "";
        return false;
    }
}

function enable() {
    const toggle = document.getElementById("notification-toggle");
    toggle.disabled = true;

    Notification.requestPermission().then(async value => {
        if (value === "granted") {
            await subscribe();
            uploadFilter();
        } else {
            // TODO display message to user
        }
        toggle.disabled = false;
    });
}

function disable() {
    navigator.serviceWorker.ready.then(serviceWorker => {
        serviceWorker.pushManager.getSubscription().then(subscription => {
            const endpoint = encodeURIComponent(subscription.endpoint);
            fetch("api/subscriptions/?endpoint=" + endpoint, {
                method: "DELETE"
            }).then(() => {
                subscription.unsubscribe().then();
            });
        });
    });
}

async function subscribe() {
    const serviceWorker = await navigator.serviceWorker.ready;

    const subscription = await serviceWorker.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: "BEuSKswUwot0f81ljXwzZUBPVu9-pL0Te_-mujD_tsAUxTa2Iivs692KeOrHCUe2GmrgskOmbFf9gNZscrS_HQU"
    });

    const jsonSub = JSON.parse(JSON.stringify(subscription));
    endpoint = encodeURIComponent(subscription.endpoint);

    await fetch("api/subscriptions", {
        method: "PUT",
        body: JSON.stringify({
            "endpoint": subscription.endpoint,
            "key": jsonSub["keys"]["p256dh"],
            "auth": jsonSub["keys"]["auth"]
        })
    });
}
