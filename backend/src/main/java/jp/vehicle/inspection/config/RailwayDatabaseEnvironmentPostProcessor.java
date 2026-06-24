package jp.vehicle.inspection.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Railway Postgres は DATABASE_URL (postgresql://...) を提供するため JDBC URL に変換する。
 */
public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(p -> p.test("railway"))) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(databaseUrl.replace("postgresql://", "http://").replace("postgres://", "http://"));
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String db = (path != null && path.length() > 1) ? path.substring(1) : "railway";

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", "jdbc:postgresql://" + host + ":" + port + "/" + db);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                props.put("spring.datasource.username", parts[0]);
                if (parts.length > 1) {
                    props.put("spring.datasource.password", parts[1]);
                }
            }

            environment.getPropertySources().addFirst(new MapPropertySource("railwayDatabase", props));
        } catch (Exception ignored) {
            // PGHOST 等の個別変数にフォールバック
        }
    }
}
