package org.pipeman.sp_api.pdfs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pipeman.ilaw.ILAW;
import org.pipeman.ilaw.Utils;
import org.pipeman.sp_api.Config;
import org.pipeman.sp_api.Main;
import org.pipeman.sp_api.notifications.NotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class PlanDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanDownloader.class);
    private final Map<Day, DayData> data = new HashMap<>();
    private final Map<Day, DayData> previousData = new HashMap<>();

    private ILAW ilaw;
    private long lastLogin = 0;
    private final Object loginLock = new Object();

    private byte[] getPlan(Day day, Config config) {
        try {
            synchronized (loginLock) {
                if (lastLogin < System.currentTimeMillis() - 600_000) {
                    ilaw = ILAW.login(config.ilUrl, config.ilUser, config.ilPassword);
                    lastLogin = System.currentTimeMillis();
                }
            }

            String fileUrl = config.ilUrl + "/LearningToolElement/ViewLearningToolElement.aspx?LearningToolElementId="
                             + (day == Day.TODAY ? config.ilTodayPlanId : config.ilTomorrowPlanId);

            HttpResponse<String> response = ilaw.getHttpClient().send(Utils.createRequest(fileUrl), HttpResponse.BodyHandlers.ofString());
            String url = Jsoup.parse(response.body()).getElementById("ctl00_ContentPlaceHolder_ExtensionIframe").attr("src");

            Document doc = Jsoup.parse(Utils.getLast(Utils.followRedirects(url, ilaw.getHttpClient())).body());
            String proxyUrl = doc.getElementById("ctl00_ctl00_MainFormContent_PreviewIframe_OneDrivePreviewFrame").attr("src");

            String oneDriveBody = Utils.getLast(Utils.followRedirects(proxyUrl, ilaw.getHttpClient())).body();

            int start = oneDriveBody.indexOf("\"downloadUrl\":\"") + 15;
            return ilaw.getHttpClient().send(Utils.createRequest(oneDriveBody.substring(start, oneDriveBody.indexOf('"', start))), HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (Exception e) {
            LOGGER.error("Failed to get plan", e);
            throw new RuntimeException(e);
        }
    }

    private DayData refreshCache(Day day) {
        DayData data = this.data.get(day);
        synchronized (day.lock()) {
            if (data != null && hasNotExpired(data)) return data;

            long start = System.nanoTime();
            byte[] rawData = getPlan(day, Main.conf());
            data = new DayData(rawData);
            LOGGER.info("Took {}ms to download plan", (System.nanoTime() - start) / 1_000_000);

            DayData previousPlan = previousData.get(day);
            if (previousPlan != null && !previousPlan.equals(data)) {
                planChanged(day, data);
            }
            this.data.put(day, data);
            this.previousData.put(day, data);
            return data;
        }
    }

    private static boolean hasNotExpired(DayData data) {
        return data.creationTime() >= System.currentTimeMillis() - Main.conf().planCacheLifetime * 1000L;
    }

    private static void planChanged(Day day, DayData data) {
        NotificationHandler.handlePlanUpdate(day, data);
    }

    public DayData getData(Day day) {
        return refreshCache(day);
    }
}
