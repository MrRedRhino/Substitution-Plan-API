package org.pipeman.sp_api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class Database {
    private static final Database INSTANCE = new Database();
    private final Jdbi jdbi;

    public Database() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://pipeman.org:5432/website");
        config.setUsername("pi");
        config.setPassword("fullbridgerectifier");
        jdbi = Jdbi.create(new HikariDataSource(config));
//        jdbi.registerRowMapper(ConstructorMapper.factory(Mapper.class));
    }

    public Optional<Long> getLastSeen(String playerName) {
        return getJDBI().withHandle(h -> h.createQuery("""
                        SELECT time
                        FROM kryeit_playtime
                        WHERE player_name = ?
                        ORDER BY time DESC
                        LIMIT 1
                        """)
                .bind(0, playerName)
                .mapTo(Long.class)
                .findFirst());
    }

    public Optional<Long> getPlaytime(String playerName) {
        return getJDBI().withHandle(h -> h.createQuery("""
                        SELECT sum(CASE WHEN "join" THEN -time ELSE time END) AS join_sum
                        FROM kryeit_playtime
                        WHERE player_name = ?
                        """)
                .bind(0, playerName)
                .mapTo(Long.class)
                .findFirst());
    }

    public void storeAction(boolean join, String playerName) {
        getJDBI().useHandle(h -> h.createUpdate("INSERT INTO kryeit_playtime VALUES (?, ?, ?)")
                .bind(0, playerName)
                .bind(1, System.currentTimeMillis())
                .bind(2, join)
                .execute());
    }

    public static Database get() {
        return INSTANCE;
    }

    public static Jdbi getJDBI() {
        return get().jdbi;
    }
}
