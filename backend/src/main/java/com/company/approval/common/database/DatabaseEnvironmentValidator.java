package com.company.approval.common.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class DatabaseEnvironmentValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseEnvironmentValidator.class);

    private final DataSource dataSource;
    private final Environment environment;

    public DatabaseEnvironmentValidator(DataSource dataSource, Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String appEnvironment = environment.getProperty("app.environment", "development");
        if (!isKnownEnvironment(appEnvironment)) {
            throw new IllegalStateException("Unknown app.environment: " + appEnvironment);
        }

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            log.info("Database environment verified: environment={}, database={}, url={}",
                    appEnvironment, metadata.getDatabaseProductName(), sanitizeUrl(metadata.getURL()));
        }
    }

    private boolean isKnownEnvironment(String value) {
        return "development".equalsIgnoreCase(value)
                || "test".equalsIgnoreCase(value)
                || "production".equalsIgnoreCase(value);
    }

    private String sanitizeUrl(String url) {
        if (url == null) {
            return "";
        }
        int queryIndex = url.indexOf('?');
        if (queryIndex >= 0) {
            return url.substring(0, queryIndex);
        }
        return url;
    }
}
