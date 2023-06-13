package org.pipeman.sp_api.pdfs;

public enum Day {
    TODAY,
    TOMORROW;

    private final Object lock = new Object();

    public Object lock() {
        return lock;
    }
}
