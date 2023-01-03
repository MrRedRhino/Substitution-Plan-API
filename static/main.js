let todayReady = false;
let tomorrowReady = false;
let tables;
let lightMode;
const moon = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="var(--text-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class=""><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
const sun = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="var(--text-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" class=""><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>';

const delay = setTimeout(() => {
    setLoading(true);
}, 100);

function loadSettings() {
    const settings = parseInt(localStorage.getItem("settings"));
    if (isNaN(settings)) {
        lightMode = true;
        tables = false;
    } else {
        lightMode = Boolean(settings & 1);
        tables = Boolean(settings & 2);
    }
    saveSettings();
}

function saveSettings() {
    localStorage.setItem("settings", ((lightMode ? 1 : 0) | (tables ? 2 : 0)).toString());
}

function isBlank(string) {
    return string == null || string.trim() === '';
}

function toggleTheme() {
    lightMode = !lightMode;
    setTheme(lightMode);
    saveSettings();
}

function setTheme() {
    document.getElementById("theme-css").href = lightMode ? "default.css" : "dark.css";
    document.getElementById("theme-button").innerHTML = lightMode ? moon : sun;
}

function setLoading(loading) {
    document.getElementById("loading-div").hidden = !loading;
}

function onLoadEnd() {
    onResize();
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
    tables = !tables;
    saveSettings();
    location.reload();
}

function onResize() {
    if (window.innerWidth < 1140) {
        document.getElementById("sp-div").style.flexDirection = "column";

        document.getElementById("today").classList.remove("plan-div");
        document.getElementById("today").classList.add("plan-div-no-translate");
        document.getElementById("tomorrow").classList.remove("tomorrow");
        document.getElementById("tomorrow").classList.add("tomorrow-no-translate");
    } else {
        document.getElementById("sp-div").style.flexDirection = "row";

        document.getElementById("today").classList.add("plan-div");
        document.getElementById("today").classList.remove("plan-div-no-translate");
        document.getElementById("tomorrow").classList.add("tomorrow");
        document.getElementById("tomorrow").classList.remove("tomorrow-no-translate");
    }
}

window.addEventListener('resize', () => {
    onResize();
});

loadSettings();
setTheme();
getPlans();
