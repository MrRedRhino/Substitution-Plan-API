package org.pipeman;

import org.pipeman.sp_api.Main;
import org.pipeman.sp_api.PlanDownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PlanSaver {
    private static int lastPlanHash;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kk-mm");

    public static void start(int interval) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                byte[] plan = PlanDownloader.INSTANCE.updatePlan(true);
                int newHash = Arrays.hashCode(plan);
                if (newHash != lastPlanHash) {
                    savePlan(plan);
                    lastPlanHash = newHash;
                }
                PlanDownloader.INSTANCE.updatePlan(false);
            }
        }, 0, interval);
    }

    private static void savePlan(byte[] plan) {
        Date date = new Date();

        String directory = DATE_FORMAT.format(date);
        String file = TIME_FORMAT.format(date) + ".pdf";

        try {
            //noinspection ResultOfMethodCallIgnored
            Path.of(Main.conf().planSaveDir.toString(), directory).toFile().mkdirs();

            Files.write(Path.of(Main.conf().planSaveDir.toString(), directory, file), plan);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
