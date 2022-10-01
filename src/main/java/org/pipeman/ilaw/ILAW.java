package org.pipeman.ilaw;

import org.pipeman.ilaw.prefs.Preferences;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import javax.security.auth.login.LoginException;

public interface ILAW {

    /**
     * ILAW is an experimental Itslearning "API" wrapper.
     * @param url Your it's-learning instance URL, e.g. https://your-school.itslearning.com
     * @param username Your itslearning-account username
     * @param password Your itslearning-account password
     * @return A new ILAW instance
     * @throws LoginException When the login failed
     */
    static ILAW login(String url, String username, String password) throws LoginException {
        try {
            return new ILAWImpl(url, username, password);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    HttpClient getHttpClient();

    Preferences getPreferences();
}
