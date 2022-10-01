package org.pipeman.ilaw.prefs;

import org.pipeman.ilaw.ILAW;

import java.io.File;

public class PreferencesImpl implements Preferences {
    public PreferencesImpl(ILAW ilaw) {

    }

    @Override
    public String getProfilePictureUrl() {
        return null;
    }

    @Override
    public byte[] getProfilePictureBytes() {
        return null;
    }

    @Override
    public void setProfilePicture(File image) {

    }

    @Override
    public void setProfilePicture() {

    }

    @Override
    public void cropProfilePicture(int x, int y, int width, int height) {

    }

    @Override
    public void removeProfilePicture() {

    }
}
