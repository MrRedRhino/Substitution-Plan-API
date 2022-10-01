package org.pipeman.ilaw.prefs;

import java.io.File;

public interface Preferences {
    String getProfilePictureUrl();

    byte[] getProfilePictureBytes();

    void setProfilePicture(File image);

    void setProfilePicture();

    void cropProfilePicture(int x, int y, int width, int height);

    void removeProfilePicture();
}
