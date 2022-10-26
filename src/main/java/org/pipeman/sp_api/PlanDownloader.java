package org.pipeman.sp_api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.Utils;

import java.net.http.HttpResponse;

public class PlanDownloader {
    public static final PlanDownloader INSTANCE = new PlanDownloader();
    private final Object todayPlanLock = new Object();
    private String todayPlan = "";
    private final Object tomorrowPlanLock = new Object();
    private String tomorrowPlan = "";
    private ILAW ilaw;

    private byte[] getPlan(boolean today, Config config, boolean login) {
        try {
            if (login) ilaw = ILAW.login(config.ilUrl, config.ilUser, config.ilPassword);

            String fileUrl = "/LearningToolElement/ViewLearningToolElement.aspx?LearningToolElementId="
                             + (today ? config.ilTodayPlanId : config.ilTomorrowPlanId);

            HttpResponse<String> response = ilaw.getHttpClient().send(Utils.createRequest(config.ilUrl + fileUrl), HttpResponse.BodyHandlers.ofString());
            String url = Jsoup.parse(response.body()).getElementById("ctl00_ContentPlaceHolder_ExtensionIframe").attr("src");

            Document doc = Jsoup.parse(Utils.getLast(Utils.followRedirects(url, ilaw.getHttpClient())).body());
            String proxyUrl = doc.getElementById("ctl00_ctl00_MainFormContent_PreviewIframe_OneDrivePreviewFrame").attr("src");

            String oneDriveBody = Utils.getLast(Utils.followRedirects(proxyUrl, ilaw.getHttpClient())).body();

            int start = oneDriveBody.indexOf("\"downloadUrl\":\"") + 15;
            return ilaw.getHttpClient().send(Utils.createRequest(oneDriveBody.substring(start, oneDriveBody.indexOf('"', start))), HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] updatePlan(boolean today) {
        if (today) {
            synchronized (todayPlanLock) {
                byte[] plan = getPlan(true, Main.conf(), true);
                todayPlan = SpApiUtils.convertPdfToHtml(plan);
                return plan;
            }
        } else {
            synchronized (tomorrowPlanLock) {
                byte[] plan = getPlan(false, Main.conf(), false);
                tomorrowPlan = SpApiUtils.convertPdfToHtml(plan);
                return plan;
            }
        }
    }

    public String getTodayPlan() {
        synchronized (todayPlanLock) {
            return todayPlan;
        }
    }

    public String getTomorrowPlan() {
        synchronized (tomorrowPlanLock) {
            return tomorrowPlan;
        }
    }
}
