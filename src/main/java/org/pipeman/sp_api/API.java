package org.pipeman.sp_api;

import io.javalin.http.Context;

public class API {
    private static final PlanDownloader DOWNLOADER = new PlanDownloader(Main.conf());

    public static void getPlanToday(Context ctx) {
        ctx.html(DOWNLOADER.getTodayPlan());
    }

    public static void getPlanTomorrow(Context ctx) {
        ctx.html(DOWNLOADER.getTomorrowPlan());
    }
}
