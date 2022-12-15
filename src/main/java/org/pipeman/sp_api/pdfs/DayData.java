package org.pipeman.sp_api.pdfs;

public class DayData {
    private final Object lock = new Object();
    private long lastUpdate = 0;
    private String html = "";
    private PlanData data = null;
    private byte[] image = new byte[0];
    private byte[] pdf = new byte[0];

    public Object lock() {
        return lock;
    }

    public long lastUpdate() {
        return lastUpdate;
    }

    public void lastUpdate(long newValue) {
        lastUpdate = newValue;
    }

    public String html() {
        return html;
    }

    public void html(String newValue) {
        html = newValue;
    }

    public PlanData data() {
        return data;
    }

    public void data(PlanData newValue) {
        data = newValue;
    }

    public byte[] image() {
        return image;
    }

    public void image(byte[] newValue) {
        this.image = newValue;
    }

    public byte[] pdf() {
        return pdf;
    }

    public void pdf(byte[] newValue) {
        this.pdf = newValue;
    }
}
