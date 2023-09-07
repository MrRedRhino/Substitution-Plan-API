package org.pipeman.sp_api.pdfs;

public enum Day {
    TODAY("heutige"),
    TOMORROW("morgige");

    private final Object lock = new Object();
    private final String localization;

    Day(String localization) {
        this.localization = localization;
    }

    public String localization() {
        return localization;
    }

    public Object lock() {
        return lock;
    }
}
