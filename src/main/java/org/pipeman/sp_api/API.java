package org.pipeman.sp_api;

import io.javalin.http.Context;

public class API {
    public static void getPlanToday(Context ctx) {
        ctx.html(PlanDownloader.INSTANCE.getTodayPlan());
    }

    public static void getPlanTomorrow(Context ctx) {
        ctx.html(PlanDownloader.INSTANCE.getTomorrowPlan());
    }
}
