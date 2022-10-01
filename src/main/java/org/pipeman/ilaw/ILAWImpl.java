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

public class ILAWImpl implements ILAW {
    private static final Logger LOGGER = LoggerFactory.getLogger(ILAWImpl.class);
    final HttpClient HTTP_CLIENT = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    final String url;
    private Preferences preferences = null;

    ILAWImpl(String url, String username, String password) throws URISyntaxException, IOException, InterruptedException, LoginException {
        long start = System.nanoTime();
        this.url = url;
        URI indexURI = URI.create(url + "/Index.aspx");

        HttpRequest request = HttpRequest.newBuilder().uri(indexURI).build();
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(indexURI)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getLoginBody(username, password)))
                .build();

        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        HttpResponse<Void> response2 = HTTP_CLIENT.send(request2, HttpResponse.BodyHandlers.discarding());
        if (response2.statusCode() == 200) {
            throw new LoginException();
        }
        doRequestWithRedirect(url, response2);

        LOGGER.info("Itslearning login successful! Took " + (System.nanoTime() - start) / 1_000_000 + "ms");
    }

    private void doRequestWithRedirect(String mainSite, HttpResponse<?> response) throws IOException, InterruptedException, URISyntaxException {
        String redirect = response.headers().map().get("location").get(0);
        HTTP_CLIENT.send(HttpRequest.newBuilder().uri(new URI(mainSite + redirect)).build(),
                HttpResponse.BodyHandlers.discarding());
    }

    private String getLoginBody(String username, String password) {
        return "__VIEWSTATE=%2FwEPDwUJNTIxNzk4NjQ2DxYCHhNWYWxpZGF0ZVJlcXVlc3RNb2RlAgEWAgUFY3RsMDAPZBYEAgIPZBYGAgEPZBYEAgMPZBYKAgIPDxYCHgdWaXNpYmxlaGRkAgMPDxYEHghDc3NDbGFzcwURaC1oaWdobGlnaHQgaC1tcjAeBF8hU0ICAhYEHgRyb2xlBQVhbGVydB4JYXJpYS1saXZlBQlhc3NlcnRpdmUWAgIBDxYCHgRUZXh0BSZVbmfDvGx0aWdlciBCZW51dHplcm5hbWUgb2RlciBQYXNzd29ydGQCBA8WAh4FY2xhc3MFE2NjbC1yd2dtLWNvbHVtbi0xLTEWAgIBD2QWCGYPFgIfBwULZWxvZ2luLWl0ZW0WAmYPD2QWDB4LcGxhY2Vob2xkZXIFDEJlbnV0emVybmFtZR4KYXJpYS1sYWJlbAUMQmVudXR6ZXJuYW1lHghyZXF1aXJlZAUIcmVxdWlyZWQeC2F1dG9jb3JyZWN0BQNvZmYeDmF1dG9jYXBpdGFsaXplBQNvZmYeEGFyaWEtZGVzY3JpYmVkYnkFK2N0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfRXJyb3JNZXNzYWdlUGFuZWxkAgEPFgIfBwULZWxvZ2luLWl0ZW0WAmYPD2QWDB8IBQhQYXNzd29ydB8JBQhQYXNzd29ydB8KBQhyZXF1aXJlZB8LBQNvZmYfDAUDb2ZmHw0FK2N0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfRXJyb3JNZXNzYWdlUGFuZWxkAgMPDxYGHwYFCEFubWVsZGVuHwIFYGNjbC1idXR0b24gY2NsLWJ1dHRvbi1jb2xvci1ncmVlbiBjY2wtYnV0dG9uLW9rIGl0c2wtbm8tdGV4dC1kZWNvcmF0aW9uIGl0c2wtbmF0aXZlLWxvZ2luLWJ1dHRvbh8DAgJkZAIEDw8WAh8BZxYCHwcFGGgtZHNwLWIgaC1mbnQtc20gaC1wZHQxMGQCBg8WAh8BaBYCAgEPZBYEZg8WAh4LXyFJdGVtQ291bnQC%2F%2F%2F%2F%2Fw9kAgEPFgQfBwUlaC1tcmIyMCBpdHNsLWZlZGVyYXRlZC1sb2dpbi1kcm9wZG93bh8BaBYEAgEQEA8WAh4LXyFEYXRhQm91bmRnZGQWADLcBAABAAAA%2F%2F%2F%2F%2FwEAAAAAAAAABAEAAADiAVN5c3RlbS5Db2xsZWN0aW9ucy5HZW5lcmljLkRpY3Rpb25hcnlgMltbU3lzdGVtLlN0cmluZywgbXNjb3JsaWIsIFZlcnNpb249NC4wLjAuMCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdhNWM1NjE5MzRlMDg5XSxbU3lzdGVtLlN0cmluZywgbXNjb3JsaWIsIFZlcnNpb249NC4wLjAuMCwgQ3VsdHVyZT1uZXV0cmFsLCBQdWJsaWNLZXlUb2tlbj1iNzdhNWM1NjE5MzRlMDg5XV0DAAAAB1ZlcnNpb24IQ29tcGFyZXIISGFzaFNpemUAAwAIkgFTeXN0ZW0uQ29sbGVjdGlvbnMuR2VuZXJpYy5HZW5lcmljRXF1YWxpdHlDb21wYXJlcmAxW1tTeXN0ZW0uU3RyaW5nLCBtc2NvcmxpYiwgVmVyc2lvbj00LjAuMC4wLCBDdWx0dXJlPW5ldXRyYWwsIFB1YmxpY0tleVRva2VuPWI3N2E1YzU2MTkzNGUwODldXQgAAAAACQIAAAAAAAAABAIAAACSAVN5c3RlbS5Db2xsZWN0aW9ucy5HZW5lcmljLkdlbmVyaWNFcXVhbGl0eUNvbXBhcmVyYDFbW1N5c3RlbS5TdHJpbmcsIG1zY29ybGliLCBWZXJzaW9uPTQuMC4wLjAsIEN1bHR1cmU9bmV1dHJhbCwgUHVibGljS2V5VG9rZW49Yjc3YTVjNTYxOTM0ZTA4OV1dAAAAAAtkAgMPDxYGHwYFHFp1ciBleHRlcm5lbiBBbm1lbGR1bmcgZ2VoZW4fAgUYaC1tcnQyMCBoLW1yYjEwIGgtZm50LXNtHwMCAmRkAgcPFgIfBwUjaC1kc3AtYi1ub3QtZm9yY2VkIGgtZm50LXNtIGgtbXJ0MTBkAgYPZBYCZg9kFgJmD2QWBgIBDxYCHwYFDUluZm9ybWF0aW9uZW5kAgUPFgIfAWhkAgcPFCsAAg8WBB8PZx8OZmRkZAIDD2QWAmYPFgIfBwUHaC1obGlzdBYCAgEPFgIfDgIDFgZmD2QWAmYPFQJVL0NvdXJzZS9saXN0X2Vucm9sbWVudF9jb3Vyc2VzLmFzcHg%2FY2xlYW49dHJ1ZSZhbXA7TGFuZ3VhZ2VJZD02JmFtcDtDdXN0b21lcklkPTkwMDI0NQtLdXJza2F0YWxvZ2QCAQ9kFgJmDxUCL2h0dHBzOi8vc3VwcG9ydC5pdHNsZWFybmluZy5jb20vZGUvc3VwcG9ydC9ob21lDE9ubGluZS1IaWxmZWQCAg9kFgJmDxUCES9DbGVhbkNvb2tpZS5hc3B4HEZyb250ZXItQ29va2llcyBsJiMyNDY7c2NoZW5kAgUPZBYCZg8VAVFodHRwczovL3BsYXRmb3JtLml0c2xlYXJuaW5nLmNvbS9SZWRpcmVjdGlvbi9TZXRDdXN0b21lcklkLmFzcHg%2FQ3VzdG9tZXJJZD05MDAyNDVkAgQPZBYCZg8VCyxjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX25hdGl2ZUFuZExkYXBMb2dpbiVjdGwwMF9Db250ZW50UGxhY2VIb2xkZXIxX29yU2VwYXJhdG9yKGN0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfZmVkZXJhdGVkTG9naW4yY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9uYXRpdmVMb2dpbkxpbmtDb250YWluZXIvY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9mZWRlcmF0ZWRMb2dpbldyYXBwZXIzY3RsMDBfQ29udGVudFBsYWNlSG9sZGVyMV9zaG93TmF0aXZlTG9naW5WYWx1ZUZpZWxkE2NjbC1yd2dtLWNvbHVtbi0xLTITY2NsLXJ3Z20tY29sdW1uLTEtMgR0cnVlIHBkZmhvcGlsYm5kaWZtZmNlamdkZG9hYmlnYWRqZ3BtJ2N0bDAwX0NvbnRlbnRQbGFjZUhvbGRlcjFfQ2hyb21lYm9va0FwcGQYAQUnY3RsMDAkQ29udGVudFBsYWNlSG9sZGVyMSROZXdzJE5ld3NMaXN0DzwrAA4DCGYMZg0C%2F%2F%2F%2F%2Fw9kxYVhLJ06s8ixCKt2mhDbJVQoMSE%3D&__VIEWSTATEGENERATOR=90059987&__EVENTVALIDATION=%2FwEdAAb%2BvVakc%2Bib09DOAvT9igBJ8fB5t%2B9v57KHoifeE6Ej%2B75MFqk64wecfXK5391QIHERiRPiIDS215NijH3q7vi1WK7yTHiL7l2vfUPiKuw9Xy0kRG1H3KSawmsStysxvNyeL1Su5WWwNuFE%2FLBTDl3D1vT9cQ%3D%3D&ctl00%24ContentPlaceHolder1%24Username%24input={USERNAME}&ctl00%24ContentPlaceHolder1%24Password%24input={PASSWORD}&ctl00%24ContentPlaceHolder1%24ChromebookApp=false&ctl00%24ContentPlaceHolder1%24nativeLoginButton=Anmelden&ctl00%24ContentPlaceHolder1%24showNativeLoginValueField="
                .replace("{USERNAME}", username)
                .replace("{PASSWORD}", password);
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
