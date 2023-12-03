package org.pipeman.sp_api.api;

import io.javalin.http.Context;
import io.javalin.http.Header;
import org.pipeman.sp_api.AutoFetch;
import org.pipeman.sp_api.pdfs.Day;
import org.pipeman.sp_api.pdfs.Plan;
import org.pipeman.sp_api.pdfs.PlanDownloader;
import org.pipeman.sp_api.pdfs.PlanIdentifier;

public class API {
    private static final PlanDownloader DOWNLOADER = new PlanDownloader();

    static {
        AutoFetch.start(DOWNLOADER);
    }

    public static void getPlanToday(Context ctx) {
        sendData(ctx, Day.TODAY);
    }

    public static void getPlanTomorrow(Context ctx) {
        sendData(ctx, Day.TOMORROW);
    }

    private static void sendData(Context ctx, Day day) {
        String formatString = ctx.queryParam("format");
        if (formatString == null) Responses.FORMAT_REQUIRED.apply(ctx);
        else {
            Format format = Format.fromString(formatString);
            if (format == null) {
                Responses.FORMAT_INVALID.apply(ctx);
                return;
            }

            Plan data = DOWNLOADER.getCachedPlan(new PlanIdentifier("", day));
            switch (format) {
                case HTML -> ctx.html(data.html());
                case PDF -> ctx.result(data.pdf()).header(Header.CONTENT_TYPE, "application/pdf");
                case PNG -> ctx.result(data.image()).header(Header.CONTENT_TYPE, "image/png");
                case JSON -> ctx.json(data.data());
            }
        }
    }

    private enum Format {
        HTML,
        PDF,
        PNG,
        JSON;

        public static Format fromString(String s) {
            return switch (s.toLowerCase()) {
                case "html" -> HTML;
                case "pdf" -> PDF;
                case "png" -> PNG;
                case "json" -> JSON;
                default -> null;
            };
        }
    }
}
