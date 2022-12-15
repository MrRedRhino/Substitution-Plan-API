package org.pipeman.sp_api.api;

import io.javalin.http.Context;

import java.util.Map;

public enum Responses {
    FORMAT_INVALID(0, 400, "Query parameter 'format' must be one of 'HTML', 'PDF', 'PNG', 'JSON'"),
    FORMAT_REQUIRED(1, 400, "Query parameter 'format' required.");

    private final int id;
    private final String name;
    private final String desc;
    private final int status;

    Responses(int id, int status, String desc) {
        this.id = id;
        this.name = name().toLowerCase().replace('_', '-');
        this.desc = desc;
        this.status = status;
    }

    public void apply(Context ctx) {
        ctx.status(status).json(Map.of(
                "error", Map.of(
                        "id", id,
                        "name", name,
                        "description", desc
                )
        ));
    }
}
