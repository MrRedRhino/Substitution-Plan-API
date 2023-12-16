let todayReady = false;
let tomorrowReady = false;
let tables;
let lightMode;

const delay = setTimeout(() => {
    setLoading(true);
}, 100);

function loadSettings() {
    const settings = JSON.parse(localStorage.getItem("settings"));
    if (settings == null) {
        lightMode = true;
        tables = false;
    } else {
        lightMode = settings["light-mode"];
        tables = settings["table-view"];
    }
    saveSettings();
}

function saveSettings() {
    const settings = {
        "light-mode": lightMode,
        "table-view": tables
    }
    document.getElementById("table-view-toggle").checked = tables;
    document.getElementById("theme-toggle").checked = !lightMode;
    localStorage.setItem("settings", JSON.stringify(settings));
}

function isBlank(string) {
    return string == null || string.trim() === '';
}

function closeSettings() {
    document.getElementById("settings-main").classList.remove("enabled");
}

function openSettings() {
    document.getElementById("settings-main").classList.add("enabled");
    document.getElementById("notifications-settings").hidden = true;
    load().then(enabled => {
        document.getElementById("notification-toggle").checked = enabled;
        document.getElementById("filter").value = filter;
        document.getElementById("notifications-settings").hidden = false;
    })
}

function toggleTheme() {
    lightMode = !document.getElementById("theme-toggle").checked;
    setTheme(lightMode);
    saveSettings();
}

function setTheme() {
    document.getElementById("theme-css").href = lightMode ? "default.css" : "dark.css";
}

function setLoading(loading) {
    document.getElementById("loading-div").hidden = !loading;
}

function onLoadEnd() {
    if (todayReady && tomorrowReady) {
        setLoading(false);
        clearTimeout(delay);
    }
}

function getPlans() {
    if (tables) {
        fetch("api/plans/today?format=json").then(r => r.json()
            .then(j => {
                const info = j["information"];
                if (!isBlank(info)) document.getElementById("message-today").innerText = info;
                else document.getElementById("message-today").innerText = "Keine Mitteilungen";

                displayDate(true, j["date"]);

                const table = document.getElementById("table-today");
                j["substitutions"].forEach(s => {
                    document.getElementById("no-subs-today").hidden = true;
                    document.getElementById("table-today").hidden = false;
                    addTableRow(table, s["class"], s["lesson"], s["substitution"], s["teacher"], s["room"], s["other"]);
                });
                todayReady = true;
                document.getElementById("wrapper-today").hidden = false;
                onLoadEnd();
            })
        );

        fetch("api/plans/tomorrow?format=json").then(r => r.json()
            .then(j => {
                const info = j["information"];
                if (!isBlank(info)) document.getElementById("message-tomorrow").innerText = info;
                else document.getElementById("message-tomorrow").innerText = "Keine Mitteilungen";

                displayDate(false, j["date"]);

                const table = document.getElementById("table-tomorrow");
                j["substitutions"].forEach(s => {
                    document.getElementById("no-subs-tomorrow").hidden = true;
                    document.getElementById("table-tomorrow").hidden = false;
                    addTableRow(table, s["class"], s["lesson"], s["substitution"], s["teacher"], s["room"], s["other"]);
                });
                tomorrowReady = true;
                document.getElementById("wrapper-tomorrow").hidden = false;
                onLoadEnd();
            })
        );
    } else {
        fetch("api/plans/today?format=html").then(response => {
            response.text().then(text => {
                document.getElementById("today").innerHTML = text;
                todayReady = true;
                onLoadEnd();
            });
        });

        fetch("api/plans/tomorrow?format=html").then(response => {
            response.text().then(text => {
                document.getElementById("tomorrow").innerHTML = text;
                tomorrowReady = true;
                onLoadEnd();
            });
        });
    }
}

function displayDate(today, date) {
    const dates = document.getElementsByClassName(today ? "date-today" : "date-tomorrow");
    for (let i = 0; i < dates.length; i++) {
        dates.item(i).innerText = date;
    }
}

function addTableRow(table, clazz, lesson, substitution, teacher, room, other) {
    const tr = document.createElement("tr");

    const clazzTD = document.createElement("td");
    clazzTD.innerText = clazz;
    tr.appendChild(clazzTD);

    const lessonTD = document.createElement("td");
    lessonTD.innerText = lesson;
    tr.appendChild(lessonTD);

    const substitutionTD = document.createElement("td");
    substitutionTD.innerText = substitution;
    tr.appendChild(substitutionTD);

    const teacherTD = document.createElement("td");
    teacherTD.innerText = teacher;
    tr.appendChild(teacherTD);

    const roomTD = document.createElement("td");
    roomTD.innerText = room;
    tr.appendChild(roomTD);

    const otherTD = document.createElement("td");
    otherTD.innerText = other;
    tr.appendChild(otherTD);

    table.appendChild(tr);
}

function toggleTable() {
    tables = document.getElementById("table-view-toggle").checked;
    saveSettings();
    location.reload();
}

loadSettings();
setTheme();
getPlans();
