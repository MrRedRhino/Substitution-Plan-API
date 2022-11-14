package org.pipeman.sp_api;

import org.pipeman.pconf.AbstractConfig;

import java.nio.file.Path;

public class Config extends AbstractConfig {
    public final int serverPort = this.get("server-port", 13337);
    public final Path indexPath = this.get("index-file-path", Path.of("static/index.html"));
    public final String ilUrl = this.get("its-learning-url", "https://your-school.itslearning.com");
    public final String ilPassword = this.get("its-learning-password", "Your password");
    public final String ilUser = this.get("its-learning-user", "Your name");
    public final String ilTodayPlanId = this.get("its-learning-today-plan-id", "");
    public final String ilTomorrowPlanId = this.get("its-learning-tomorrow-plan-id", "");
    public final int planFetchInterval = this.get("plan-fetch-interval", 300);
    public final Path planSaveDir = this.get("plan-save-directory", Path.of(""));

    public Config(String file) {
        super(file);
        store(Path.of(file), "");
    }
}
