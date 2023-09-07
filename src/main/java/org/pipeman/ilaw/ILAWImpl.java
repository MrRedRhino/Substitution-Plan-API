package org.pipeman.ilaw;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pipeman.ilaw.prefs.Preferences;
import org.pipeman.ilaw.prefs.PreferencesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ILAWImpl implements ILAW {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILAWImpl.class);
    final HttpClient HTTP_CLIENT = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    final String url;
    private Preferences preferences = null;

    ILAWImpl(String url, String username, String password) throws URISyntaxException, IOException, InterruptedException, LoginException {
        long start = System.nanoTime();
        this.url = url;

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getLoginBody(username, password)))
                .build();

        HttpResponse<String> response2 = HTTP_CLIENT.send(postRequest, BodyHandlers.ofString());
        if (response2.statusCode() == 200) {
            throw new LoginException();
        }

        LOGGER.info("Itslearning login successful! Took " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private String getLoginBody(String username, String password) throws IOException, InterruptedException {
        HttpRequest indexRequest = HttpRequest.newBuilder(URI.create(url)).build();
        HttpResponse<String> response = HTTP_CLIENT.send(indexRequest, BodyHandlers.ofString());

        Document soup = Jsoup.parse(response.body());
        Element form = soup.getElementById("aspnetForm");
        Objects.requireNonNull(form, "Form not found.");

        Map<String, String> formData = new HashMap<>();
        for (Element element : form.getElementsByTag("input")) {
            if (!element.hasAttr("name")) continue;

            String name = element.attr("name");
            String value = switch (name) {
                case "ctl00$ContentPlaceHolder1$Username" -> username;
                case "ctl00$ContentPlaceHolder1$Password" -> password;
                default -> element.attr("value");
            };

            formData.put(name, value);
        }

        formData.put("__EVENTTARGET", "__Page");
        formData.put("__EVENTARGUMENT", "NativeLoginButtonClicked");

        return Utils.writeFormBody(formData);
    }

    @Override
    public HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    @Override
    public Preferences getPreferences() {
        if (preferences == null) preferences = new PreferencesImpl(this);
        return preferences;
    }
}
