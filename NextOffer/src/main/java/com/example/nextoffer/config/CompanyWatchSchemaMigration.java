package com.example.nextoffer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ensures {@code company_watches.ats_type} accepts every {@link com.example.nextoffer.career.AtsType}
 * on existing PostgreSQL databases. Hibernate {@code ddl-auto=update} does not widen check constraints.
 */
@Component
public class CompanyWatchSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CompanyWatchSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public CompanyWatchSchemaMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        if (!isPostgreSQL()) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE company_watches DROP CONSTRAINT IF EXISTS company_watches_ats_type_check");
        jdbcTemplate.execute("""
                ALTER TABLE company_watches
                ADD CONSTRAINT company_watches_ats_type_check
                CHECK (ats_type IN ('GREENHOUSE', 'WORKDAY', 'SMART_RECRUITERS', 'LEVER', 'CUSTOM_HTML'))
                """);
        log.info("Verified company_watches.ats_type check constraint includes all ATS types");
    }

    private boolean isPostgreSQL() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        }
    }
}
