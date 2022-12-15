package org.pipeman.sp_api.pdfs;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.Utils;
import org.pipeman.sp_api.Config;
import org.pipeman.sp_api.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;

public class PlanDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanDownloader.class);

    private final DayData todayData = new DayData();
    private final DayData tomorrowData = new DayData();

    private ILAW ilaw;
    private long lastLogin = 0;
    private final Object loginLock = new Object();

    private byte[] getPlan(boolean today, Config config) {
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
            return ilaw.getHttpClient().send(Utils.createRequest(oneDriveBody.substring(start, oneDriveBody.indexOf('"', start))), HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshCache(boolean today, DayData data) {
        synchronized (data.lock()) {
            if (data.lastUpdate() < System.currentTimeMillis() - Main.conf().planCacheLifetime * 1000L) {
                data.lastUpdate(System.currentTimeMillis());

                long start = System.nanoTime();
                byte[] rawData = getPlan(today, Main.conf());
                PdfDocument document = new PdfDocument(rawData);
                data.html(convertPdfToHtml(document));
                data.data(PlanData.from(document));
                data.pdf(rawData);
                data.image(convertPdfToImage(document));
                logDuration(start);
            }
        }
    }

    private String convertPdfToHtml(PdfDocument pdf) {
        OutputStream output = new ByteArrayOutputStream();
        pdf.saveToStream(output, FileFormat.HTML);
        return output.toString().replace(Main.SPIRE_WARNING, "");
    }

    private byte[] convertPdfToImage(PdfDocument pdf) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(pdf.saveAsImage(0), "png", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    public DayData getTodayData() {
        refreshCache(true, todayData);
        return todayData;
    }

    public DayData getTomorrowData() {
        refreshCache(false, tomorrowData);
        return tomorrowData;
    }

    private static void logDuration(long start) {
        LOGGER.info("Took {}ms to download plan", (System.nanoTime() - start) / 1_000_000);
    }
}
