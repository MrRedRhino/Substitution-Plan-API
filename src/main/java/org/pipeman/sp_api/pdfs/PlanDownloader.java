package org.pipeman.sp_api.pdfs;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlanDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanDownloader.class);
    private final Map<PlanIdentifier, byte[]> hashes = new HashMap<>();
    private final LoadingCache<PlanIdentifier, Plan> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.of(5, ChronoUnit.MINUTES))
            .maximumSize(10)
            .build(this::getPlan);

    private ILAW ilaw;
    private long lastLogin = 0;
    private final Object loginLock = new Object();

    private byte[] downloadData(PlanIdentifier plan, Config config) {
        try {
            synchronized (loginLock) {
                if (lastLogin < System.currentTimeMillis() - 600_000) {
                    ilaw = ILAW.login(config.ilUrl, config.ilUser, config.ilPassword);
                    lastLogin = System.currentTimeMillis();
                }
            }

            String fileUrl = config.ilUrl + "/LearningToolElement/ViewLearningToolElement.aspx?LearningToolElementId="
                             + (plan.day() == Day.TODAY ? config.ilTodayPlanId : config.ilTomorrowPlanId);

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

    private Plan getPlan(PlanIdentifier identifier) {
        long start = System.nanoTime();
        Plan plan = new Plan(downloadData(identifier, Main.conf()));
        LOGGER.info("Took {}ms to download plan", (System.nanoTime() - start) / 1_000_000);

        byte[] oldHash = hashes.get(identifier);
        byte[] newHash = plan.getHash();
        if (oldHash != null && !Arrays.equals(oldHash, newHash)) {
            planChanged(identifier, plan);
        }
        hashes.put(identifier, newHash);
        return plan;
    }

    private static void planChanged(PlanIdentifier identifier, Plan data) {
        NotificationHandler.handlePlanUpdate(identifier.day(), data);
    }

    public Plan getCachedPlan(PlanIdentifier plan) {
        return cache.get(plan);
    }
}
