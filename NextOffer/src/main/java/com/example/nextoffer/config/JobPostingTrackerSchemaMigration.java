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
 * Ensures tracker columns exist on existing PostgreSQL databases where
 * {@code ddl-auto=update} did not add them automatically.
 */
@Component
public class JobPostingTrackerSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JobPostingTrackerSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public JobPostingTrackerSchemaMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        if (!isPostgreSQL()) {
            return;
        }

        jdbcTemplate.execute("""
                ALTER TABLE job_postings
                ADD COLUMN IF NOT EXISTS application_status VARCHAR(32) NOT NULL DEFAULT 'NEW'
                """);
        jdbcTemplate.execute("""
                ALTER TABLE job_postings
                ADD COLUMN IF NOT EXISTS status_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                """);
        jdbcTemplate.execute("""
                UPDATE job_postings
                SET status_updated_at = first_seen_at
                WHERE status_updated_at IS NULL
                   OR status_updated_at < first_seen_at
                """);
        log.info("Verified job_postings tracker columns (application_status, status_updated_at)");
    }

    private boolean isPostgreSQL() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        }
    }
}
