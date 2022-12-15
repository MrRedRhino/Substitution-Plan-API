package org.pipeman.sp_api;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.pipeman.pconf.ConfigProvider;
import org.pipeman.sp_api.api.API;
import org.pipeman.sp_api.pdfs.PdfDataSerializer;
import org.pipeman.sp_api.pdfs.PlanData;

import java.nio.file.Files;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Main {
    public static final ConfigProvider<Config> CONFIG = ConfigProvider.of("config.properties", Config::new);
    public static final String SPIRE_WARNING = "Evaluation Warning : The document was created with Spire.PDF for java.";

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "static";
                staticFiles.location = Location.EXTERNAL;
                staticFiles.precompress = false;
            });
        }).start(conf().serverPort);

        app.routes(() -> {
            get("", ctx -> ctx.html(Files.readString(conf().indexPath)));

            path("api", () -> {
                get("plans/today", API::getPlanToday);
                get("plans/tomorrow", API::getPlanTomorrow);
            });
        });

        SimpleModule module = new SimpleModule();
        module.addSerializer(PlanData.class, new PdfDataSerializer());
        JavalinJackson.Companion.defaultMapper().registerModule(module);
    }

    public static Config conf() {
        return CONFIG.c();
    }
}
