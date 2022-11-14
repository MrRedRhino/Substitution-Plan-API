package org.pipeman.sp_api;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.pipeman.pconf.ConfigProvider;

import java.nio.file.Files;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Main {
    public static final ConfigProvider<Config> CONFIG = ConfigProvider.of("config.properties", Config::new);

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
    }

    public static Config conf() {
        return CONFIG.c();
    }
}
