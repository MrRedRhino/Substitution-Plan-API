package org.pipeman.sp_api;

import org.pipeman.sp_api.pdfs.Day;
import org.pipeman.sp_api.pdfs.PlanDownloader;
import org.pipeman.sp_api.pdfs.PlanIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class AutoFetch {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFetch.class);
    private static final long INTERVAL = Duration.of(15, ChronoUnit.MINUTES).toMillis();
    private static final Timer TIMER = new Timer(true);

    public static void start(PlanDownloader downloader) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    downloader.getCachedPlan(new PlanIdentifier("", Day.TODAY));
                    downloader.getCachedPlan(new PlanIdentifier("", Day.TOMORROW));
                } catch (Exception e) {
                    LOGGER.warn("Failed to auto fetch plan", e);
                }
            }
        };

        TIMER.schedule(task, INTERVAL, INTERVAL);
    }
}
