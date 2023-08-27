package org.pipeman.ilaw;

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
import java.text.MessageFormat;

public class ILAWImpl implements ILAW {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILAWImpl.class);
    final HttpClient HTTP_CLIENT = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    final String url;
    private Preferences preferences = null;

    ILAWImpl(String url, String username, String password) throws URISyntaxException, IOException, InterruptedException, LoginException {
        long start = System.nanoTime();
        this.url = url;

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(url + "/Index.aspx"))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getLoginBody(username, password)))
                .build();

        HttpResponse<Void> response2 = HTTP_CLIENT.send(postRequest, HttpResponse.BodyHandlers.discarding());
        if (response2.statusCode() == 200) {
            throw new LoginException();
        }
//        doRequestWithRedirect(url, response2);

        LOGGER.info("Itslearning login successful! Took " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private void doRequestWithRedirect(String mainSite, HttpResponse<?> response) throws IOException, InterruptedException, URISyntaxException {
        String redirect = response.headers().map().get("location").get(0);
        HTTP_CLIENT.send(HttpRequest.newBuilder().uri(new URI(mainSite + redirect)).build(),
                HttpResponse.BodyHandlers.discarding());
    }

    private String getLoginBody(String username, String password) {
        String body = "__EVENTTARGET=__Page&__EVENTARGUMENT=NativeLoginButtonClicked&__VIEWSTATE=%2FwEPDwUKMjA1MDAzNDQwOA8WAh4TVmFsaWRhdGVSZXF1ZXN0TW9kZQIBFgIFBWN0bDAwD2QWBAICD2QWBgIBD2QWBAIDD2QWCAIDDw8WBB4IQ3NzQ2xhc3MFEWgtaGlnaGxpZ2h0IGgtbXIwHgRfIVNCAgIWBB4Ecm9sZQUFYWxlcnQeCWFyaWEtbGl2ZQUJYXNzZXJ0aXZlFgICAQ8WAh4EVGV4dAUWU2llIHd1cmRlbiBhYmdlbWVsZGV0LmQCBA8WAh4FY2xhc3MFE2NjbC1yd2dtLWNvbHVtbi0xLTEWAgIBD2QWAgIDDw8WAh4HVmlzaWJsZWcWAh8GBRhoLWRzcC1iIGgtZm50LXNtIGgtcGR0MTBkAgYPFgIfB2gWAgIBD2QWBGYPFgIeC18hSXRlbUNvdW50Av%2F%2F%2F%2F8PZAIBDxYEHwYFJWgtbXJiMjAgaXRzbC1mZWRlcmF0ZWQtbG9naW4tZHJvcGRvd24fB2gWBAIBEBAPFgIeC18hRGF0YUJvdW5kZ2RkFgAy3AQAAQAAAP%2F%2F%2F%2F8BAAAAAAAAAAQBAAAA4gFTeXN0ZW0uQ29sbGVjdGlvbnMuR2VuZXJpYy5EaWN0aW9uYXJ5YDJbW1N5c3RlbS5TdHJpbmcsIG1zY29ybGliLCBWZXJzaW9uPTQuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OV0sW1N5c3RlbS5TdHJpbmcsIG1zY29ybGliLCBWZXJzaW9uPTQuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OV1dAwAAAAdWZXJzaW9uCENvbXBhcmVyCEhhc2hTaXplAAMACJIBU3lzdGVtLkNvbGxlY3Rpb25zLkdlbmVyaWMuR2VuZXJpY0VxdWFsaXR5Q29tcGFyZXJgMVtbU3lzdGVtLlN0cmluZywgbXNjb3JsaWIsIFZlcnNpb249NC4wLjAuMCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdhNWM1NjE5MzRlMDg5XV0IAAAAAAkCAAAAAAAAAAQCAAAAkgFTeXN0ZW0uQ29sbGVjdGlvbnMuR2VuZXJpYy5HZW5lcmljRXF1YWxpdHlDb21wYXJlcmAxW1tTeXN0ZW0uU3RyaW5nLCBtc2NvcmxpYiwgVmVyc2lvbj00LjAuMC4wLCBDdWx0dXJlPW5ldXRyYWwsIFB1YmxpY0tleVRva2VuPWI3N2E1YzU2MTkzNGUwODldXQAAAAALZAIDDw8WBh8FBRxadXIgZXh0ZXJuZW4gQW5tZWxkdW5nIGdlaGVuHwEFGGgtbXJ0MjAgaC1tcmIxMCBoLWZudC1zbR8CAgJkZAIHDxYCHwYFI2gtZHNwLWItbm90LWZvcmNlZCBoLWZudC1zbSBoLW1ydDEwZAIGD2QWAmYPZBYCZg9kFgYCAQ8WAh8FBQ1JbmZvcm1hdGlvbmVuZAIFDxYCHwdoZAIHDxQrAAIPFgQfCWcfCGZkZGQCAw9kFgJmDxYCHwYFB2gtaGxpc3QWAgIBDxYCHwgCBBYIZg9kFgJmDxUCVS9Db3Vyc2UvbGlzdF9lbnJvbG1lbnRfY291cnNlcy5hc3B4P2NsZWFuPXRydWUmYW1wO0xhbmd1YWdlSWQ9NiZhbXA7Q3VzdG9tZXJJZD05MDAyNDUURXh0ZXJuZXIgS3Vyc2thdGFsb2dkAgEPZBYCZg8VAj9odHRwczovL2l0c2xlYXJuaW5nLmNvbS9nbG9iYWwveW91ci1kYXRhLW1hdHRlcnMvYWNjZXNzaWJpbGl0eS8jRXJrbCYjMjI4O3J1bmcgenVyIEJhcnJpZXJlZnJlaWhlaXRkAgIPZBYCZg8VAi9odHRwczovL3N1cHBvcnQuaXRzbGVhcm5pbmcuY29tL2RlL3N1cHBvcnQvaG9tZQxPbmxpbmUtSGlsZmVkAgMPZBYCZg8VAhEvQ2xlYW5Db29raWUuYXNweCBpdHNsZWFybmluZy1Db29raWVzIGwmIzI0NjtzY2hlbmQCBQ9kFgJmDxUBUWh0dHBzOi8vcGxhdGZvcm0uaXRzbGVhcm5pbmcuY29tL1JlZGlyZWN0aW9uL1NldEN1c3RvbWVySWQuYXNweD9DdXN0b21lcklkPTkwMDI0NWQCBA9kFgJmDxUNLGN0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfbmF0aXZlQW5kTGRhcExvZ2luJWN0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfb3JTZXBhcmF0b3IoY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9mZWRlcmF0ZWRMb2dpbjJjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX25hdGl2ZUxvZ2luTGlua0NvbnRhaW5lci9jdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX2ZlZGVyYXRlZExvZ2luV3JhcHBlcjNjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX3Nob3dOYXRpdmVMb2dpblZhbHVlRmllbGQTY2NsLXJ3Z20tY29sdW1uLTEtMhNjY2wtcndnbS1jb2x1bW4tMS0yBHRydWUgcGRmaG9waWxibmRpZm1mY2VqZ2Rkb2FiaWdhZGpncG0nY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9DaHJvbWVib29rQXBwGG5hdGl2ZUxvZ2luQnV0dG9uQ2xpY2tlZEFmdW5jdGlvbigpIHsgX19kb1Bvc3RCYWNrKCdfX1BhZ2UnLCdOYXRpdmVMb2dpbkJ1dHRvbkNsaWNrZWQnKTsgfWQYAQUnY3RsMDAkQ29udGVudFBsYWNlSG9sZGVyMSROZXdzJE5ld3NMaXN0DzwrAA4DCGYMZg0C%2F%2F%2F%2F%2Fw9k6eERzyrHINvdG4Kw7u1e9NI%2FKOU%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEdAAXlDHqbTr8SoVxRfqqtzeMNBW7RgSH1lX1BdoQhflOecBcongFojABWqnW0ooATLS4RiRPiIDS215NijH3q7vi1LSREbUfcpJrCaxK3KzG83KR%2F3XvCbh9lYrUENfmYpThYOQn3&ctl00%24ContentPlaceHolder1%24Username={0}&ctl00%24ContentPlaceHolder1%24Password={1}";
        return MessageFormat.format(body, username, password);
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
