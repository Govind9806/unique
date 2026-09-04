package com.example.uniqueAproovaResidency.config;

import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final FlatRepository flatRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking database schema migrations for water_readings table...");
        try {
            jdbcTemplate.execute("ALTER TABLE water_readings ADD COLUMN IF NOT EXISTS reading_type VARCHAR(255) DEFAULT 'REGULAR'");
            jdbcTemplate.execute("ALTER TABLE water_readings ADD COLUMN IF NOT EXISTS meter_photo_document_id VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE water_readings ALTER COLUMN previous_reading DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE expenses ADD COLUMN IF NOT EXISTS is_emergency BOOLEAN DEFAULT FALSE");
            jdbcTemplate.execute("ALTER TABLE expenses ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE expenses ADD COLUMN IF NOT EXISTS receipt_photo_document_id VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS receipt_photo_document_id VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE ledger_transactions ADD COLUMN IF NOT EXISTS vendor VARCHAR(255)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS expense_approval_flats (expense_id VARCHAR(36) NOT NULL REFERENCES expenses(id) ON DELETE CASCADE, flat_number VARCHAR(50) NOT NULL)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS expense_rejection_flats (expense_id VARCHAR(36) NOT NULL REFERENCES expenses(id) ON DELETE CASCADE, flat_number VARCHAR(50) NOT NULL)");
            jdbcTemplate.execute("ALTER TABLE works ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_decision VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_message VARCHAR(500)");
            jdbcTemplate.execute("ALTER TABLE works ADD COLUMN IF NOT EXISTS maintenance_responded_at TIMESTAMP");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_approval_flats (work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE, flat_number VARCHAR(50) NOT NULL)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_rejection_flats (work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE, flat_number VARCHAR(50) NOT NULL)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_photos (work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE, photo_url VARCHAR(500) NOT NULL)");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_vote_reasons (work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE, flat_number VARCHAR(50) NOT NULL, reason VARCHAR(500))");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_votes (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "work_id VARCHAR(36) NOT NULL REFERENCES works(id) ON DELETE CASCADE, " +
                    "flat_number VARCHAR(50) NOT NULL, " +
                    "vote VARCHAR(20) NOT NULL, " +
                    "reason VARCHAR(500), " +
                    "voted_by_user_id VARCHAR(36) REFERENCES users(id), " +
                    "voted_at TIMESTAMP NOT NULL, " +
                    "created_at TIMESTAMP, " +
                    "updated_at TIMESTAMP, " +
                    "CONSTRAINT uk_work_vote_flat UNIQUE (work_id, flat_number))");
            log.info("Database schema patch applied: columns & voting tables added for expenses and works.");
        } catch (Exception e) {
            log.warn("Database schema patch notice: {}", e.getMessage());
        }

        log.info("Ensuring 16 apartment flat entities exist in database (no bills or payments touched)...");

        List<String> flatNumbers = List.of(
                "001", "002", "003", "004",
                "101", "102", "103", "104",
                "201", "202", "203", "204",
                "301", "302", "303", "304"
        );

        for (String fn : flatNumbers) {
            String flatId = "flat-" + fn;
            if (!flatRepository.existsById(flatId)) {
                int floor = Integer.parseInt(fn.substring(0, 1));
                flatRepository.save(Flat.builder()
                        .id(flatId)
                        .flatNumber(fn)
                        .floor(floor)
                        .status("OCCUPIED")
                        .build());
            }
        }

        log.info("DataSeeder complete: 0 bills deleted/modified. All bills and payments preserved in database.");
    }
}
