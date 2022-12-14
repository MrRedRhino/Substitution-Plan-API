package org.pipeman.sp_api;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.http.HttpResponse;

public class PlanDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanDownloader.class);

    private final Object todayPlanLock = new Object();
    private long lastTodayPlanUpdate = 0;
    private String todayPlan = "";
    private final Object tomorrowPlanLock = new Object();
    private long lastTomorrowPlanUpdate = 0;
    private String tomorrowPlan = "";
    private ILAW ilaw;
    private long lastLogin = 0;
    private final Object loginLock = new Object();

    public PlanDownloader(Config config) {
//        try {
//            ilaw = ILAW.login(config.ilUrl, config.ilUser, config.ilPassword);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    private String getPlan(boolean today, Config config) {
        try {
            synchronized (loginLock) {
                if (lastLogin < System.currentTimeMillis() - 600_000) {
                    ilaw = ILAW.login(config.ilUrl, config.ilUser, config.ilPassword);
                    lastLogin = System.currentTimeMillis();
                }
            }

            String fileUrl = "/LearningToolElement/ViewLearningToolElement.aspx?LearningToolElementId="
                             + (today ? config.ilTodayPlanId : config.ilTomorrowPlanId);

            HttpResponse<String> response = ilaw.getHttpClient().send(Utils.createRequest(config.ilUrl + fileUrl), HttpResponse.BodyHandlers.ofString());
            String url = Jsoup.parse(response.body()).getElementById("ctl00_ContentPlaceHolder_ExtensionIframe").attr("src");

            Document doc = Jsoup.parse(Utils.getLast(Utils.followRedirects(url, ilaw.getHttpClient())).body());
            String proxyUrl = doc.getElementById("ctl00_ctl00_MainFormContent_PreviewIframe_OneDrivePreviewFrame").attr("src");

            String oneDriveBody = Utils.getLast(Utils.followRedirects(proxyUrl, ilaw.getHttpClient())).body();

            int start = oneDriveBody.indexOf("\"downloadUrl\":\"") + 15;
            return convertPdfToHtml(ilaw.getHttpClient().send(Utils.createRequest(oneDriveBody.substring(start, oneDriveBody.indexOf('"', start))), HttpResponse.BodyHandlers.ofByteArray()).body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTodayPlan() {
        synchronized (todayPlanLock) {
            if (lastTodayPlanUpdate < System.currentTimeMillis() - Main.conf().planCacheLifetime * 1000L) {
                lastTodayPlanUpdate = System.currentTimeMillis();

                long start = System.nanoTime();
                todayPlan = getPlan(true, Main.conf());
                logDuration(start);
            }
            return todayPlan;
        }
    }

    public String getTomorrowPlan() {
        synchronized (tomorrowPlanLock) {
            if (lastTomorrowPlanUpdate < System.currentTimeMillis() - Main.conf().planCacheLifetime * 1000L) {
                lastTomorrowPlanUpdate = System.currentTimeMillis();

                long start = System.nanoTime();
                tomorrowPlan = getPlan(false, Main.conf());
                logDuration(start);
            }
            return tomorrowPlan;
        }
    }

    private String convertPdfToHtml(byte[] input) {
        OutputStream output = new ByteArrayOutputStream();
        new PdfDocument(input).saveToStream(output, FileFormat.HTML);
        return output.toString()
                .replace("Evaluation Warning : The document was created with Spire.PDF for java.", "");
    }

    private static void logDuration(long start) {
        LOGGER.info("Took {}ms to download plan", (System.nanoTime() - start) / 1_000_000);
    }
}
