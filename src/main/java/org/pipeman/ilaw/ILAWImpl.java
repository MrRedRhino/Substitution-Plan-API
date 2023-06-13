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
        String body = "__VIEWSTATE=%2FwEPDwUKMTkyODI0NDEyNg8WAh4TVmFsaWRhdGVSZXF1ZXN0TW9kZQIBFgIFBWN0bDAwD2QWBAICD2QWBgIBD2QWBAIDD2QWCgICDw8WAh4HVmlzaWJsZWhkZAIDDw8WBB4IQ3NzQ2xhc3MFEWgtaGlnaGxpZ2h0IGgtbXIwHgRfIVNCAgJkFgICAQ8WAh4EVGV4dGVkAgQPFgIeBWNsYXNzBRNjY2wtcndnbS1jb2x1bW4tMS0xFgICAQ9kFghmDxYCHwUFC2Vsb2dpbi1pdGVtFgJmDw9kFgweC3BsYWNlaG9sZGVyBQxCZW51dHplcm5hbWUeCmFyaWEtbGFiZWwFDEJlbnV0emVybmFtZR4IcmVxdWlyZWQFCHJlcXVpcmVkHgpzcGVsbGNoZWNrBQVmYWxzZR4OYXV0b2NhcGl0YWxpemUFA29mZh4QYXJpYS1kZXNjcmliZWRieQUrY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9FcnJvck1lc3NhZ2VQYW5lbGQCAQ8WAh8FBQtlbG9naW4taXRlbRYCZg8PZBYMHwYFCFBhc3N3b3J0HwcFCFBhc3N3b3J0HwgFCHJlcXVpcmVkHwkFBWZhbHNlHwoFA29mZh8LBStjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX0Vycm9yTWVzc2FnZVBhbmVsZAIDDw8WBh8EBQhBbm1lbGRlbh8CBWBjY2wtYnV0dG9uIGNjbC1idXR0b24tY29sb3ItZ3JlZW4gY2NsLWJ1dHRvbi1vayBpdHNsLW5vLXRleHQtZGVjb3JhdGlvbiBpdHNsLW5hdGl2ZS1sb2dpbi1idXR0b24fAwICZGQCBA8PFgIfAWcWAh8FBRhoLWRzcC1iIGgtZm50LXNtIGgtcGR0MTBkAgYPFgIfAWgWAgIBD2QWBGYPFgIeC18hSXRlbUNvdW50Av%2F%2F%2F%2F8PZAIBDxYEHwUFJWgtbXJiMjAgaXRzbC1mZWRlcmF0ZWQtbG9naW4tZHJvcGRvd24fAWgWBAIBEBAPFgIeC18hRGF0YUJvdW5kZ2RkFgAy3AQAAQAAAP%2F%2F%2F%2F8BAAAAAAAAAAQBAAAA4gFTeXN0ZW0uQ29sbGVjdGlvbnMuR2VuZXJpYy5EaWN0aW9uYXJ5YDJbW1N5c3RlbS5TdHJpbmcsIG1zY29ybGliLCBWZXJzaW9uPTQuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OV0sW1N5c3RlbS5TdHJpbmcsIG1zY29ybGliLCBWZXJzaW9uPTQuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OV1dAwAAAAdWZXJzaW9uCENvbXBhcmVyCEhhc2hTaXplAAMACJIBU3lzdGVtLkNvbGxlY3Rpb25zLkdlbmVyaWMuR2VuZXJpY0VxdWFsaXR5Q29tcGFyZXJgMVtbU3lzdGVtLlN0cmluZywgbXNjb3JsaWIsIFZlcnNpb249NC4wLjAuMCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdhNWM1NjE5MzRlMDg5XV0IAAAAAAkCAAAAAAAAAAQCAAAAkgFTeXN0ZW0uQ29sbGVjdGlvbnMuR2VuZXJpYy5HZW5lcmljRXF1YWxpdHlDb21wYXJlcmAxW1tTeXN0ZW0uU3RyaW5nLCBtc2NvcmxpYiwgVmVyc2lvbj00LjAuMC4wLCBDdWx0dXJlPW5ldXRyYWwsIFB1YmxpY0tleVRva2VuPWI3N2E1YzU2MTkzNGUwODldXQAAAAALZAIDDw8WBh8EBRxadXIgZXh0ZXJuZW4gQW5tZWxkdW5nIGdlaGVuHwIFGGgtbXJ0MjAgaC1tcmIxMCBoLWZudC1zbR8DAgJkZAIHDxYCHwUFI2gtZHNwLWItbm90LWZvcmNlZCBoLWZudC1zbSBoLW1ydDEwZAIGD2QWAmYPZBYCZg9kFgYCAQ8WAh8EBQ1JbmZvcm1hdGlvbmVuZAIFDxYCHwFoZAIHDxQrAAIPFgQfDWcfDGZkZGQCAw9kFgJmDxYCHwUFB2gtaGxpc3QWAgIBDxYCHwwCBBYIZg9kFgJmDxUCVS9Db3Vyc2UvbGlzdF9lbnJvbG1lbnRfY291cnNlcy5hc3B4P2NsZWFuPXRydWUmYW1wO0xhbmd1YWdlSWQ9NiZhbXA7Q3VzdG9tZXJJZD05MDAyNDULS3Vyc2thdGFsb2dkAgEPZBYCZg8VAj9odHRwczovL2l0c2xlYXJuaW5nLmNvbS9nbG9iYWwveW91ci1kYXRhLW1hdHRlcnMvYWNjZXNzaWJpbGl0eS8XQWNjZXNzaWJpbGl0eSBzdGF0ZW1lbnRkAgIPZBYCZg8VAi9odHRwczovL3N1cHBvcnQuaXRzbGVhcm5pbmcuY29tL2RlL3N1cHBvcnQvaG9tZQxPbmxpbmUtSGlsZmVkAgMPZBYCZg8VAhEvQ2xlYW5Db29raWUuYXNweBxGcm9udGVyLUNvb2tpZXMgbCYjMjQ2O3NjaGVuZAIFD2QWAmYPFQFRaHR0cHM6Ly9wbGF0Zm9ybS5pdHNsZWFybmluZy5jb20vUmVkaXJlY3Rpb24vU2V0Q3VzdG9tZXJJZC5hc3B4P0N1c3RvbWVySWQ9OTAwMjQ1ZAIED2QWAmYPFQssY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9uYXRpdmVBbmRMZGFwTG9naW4lY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9vclNlcGFyYXRvcihjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX2ZlZGVyYXRlZExvZ2luMmN0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfbmF0aXZlTG9naW5MaW5rQ29udGFpbmVyL2N0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfZmVkZXJhdGVkTG9naW5XcmFwcGVyM2N0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfc2hvd05hdGl2ZUxvZ2luVmFsdWVGaWVsZBNjY2wtcndnbS1jb2x1bW4tMS0yE2NjbC1yd2dtLWNvbHVtbi0xLTIEdHJ1ZSBwZGZob3BpbGJuZGlmbWZjZWpnZGRvYWJpZ2FkamdwbSdjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX0Nocm9tZWJvb2tBcHBkGAEFJ2N0bDAwJENvbnRlbnRQbGFjZUhvbGRlcjEkTmV3cyROZXdzTGlzdA88KwAOAwhmDGYNAv%2F%2F%2F%2F8PZK3I0mAwY1Q6BHyjduTvr%2BOTe1ZE&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEdAAaMfZD04Z7sloMrBwfGOGMZ8fB5t%2B9v57KHoifeE6Ej%2B75MFqk64wecfXK5391QIHERiRPiIDS215NijH3q7vi1WK7yTHiL7l2vfUPiKuw9Xy0kRG1H3KSawmsStysxvNwsye2%2FZwOHnhaWof7IUbp9w1G0bw%3D%3D&ctl00%24ContentPlaceHolder1%24Username%24input={0}&ctl00%24ContentPlaceHolder1%24Password%24input={1}&ctl00%24ContentPlaceHolder1%24ChromebookApp=false&ctl00%24ContentPlaceHolder1%24nativeLoginButton=Anmelden&ctl00%24ContentPlaceHolder1%24showNativeLoginValueField=";
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
