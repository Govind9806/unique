package com.example.uniqueAproovaResidency;

import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.BillDto;
import com.example.uniqueAproovaResidency.module.maintenance.service.BillService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "SPRING_APPLICATION_NAME=uniqueAproovaResidency",
    "SERVER_PORT=8080",
    "SERVER_SERVLET_CONTEXT_PATH=/",
    "DATABASE_URL=jdbc:h2:mem:aproovadb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
    "DATABASE_USERNAME=sa",
    "DATABASE_PASSWORD=",
    "DATABASE_DRIVER=org.h2.Driver",
    "JPA_DDL_AUTO=validate",
    "JPA_SHOW_SQL=true",
    "HIBERNATE_FORMAT_SQL=true",
    "FLYWAY_ENABLED=true",
    "FLYWAY_BASELINE_ON_MIGRATE=true",
    "FLYWAY_LOCATIONS=classpath:db/migration",
    "SPRINGDOC_API_DOCS_PATH=/v3/api-docs",
    "SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html",
    "JWT_SECRET=9a4f2c8d7b1e3f5a6b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a",
    "JWT_EXPIRATION_MS=86400000",
    "JWT_REFRESH_EXPIRATION_MS=604800000",
    "FILE_STORAGE_PATH=./uploads",
    "PAYMENT_PROVIDER=direct_upi",
    "UPI_MERCHANT_VPA=aproovaresidency@upi",
    "UPI_MERCHANT_NAME=Aproova Residency Society",
    "PAYMENT_WEBHOOK_SECRET=aproova_webhook_secret_key_2026"
})
@Transactional
class BillServiceTest {

    @Autowired
    private BillService billService;

    @Test
    void testGenerateMonthlyBills_CreatesBillsFor16Flats() {
        List<BillDto> bills = billService.generateMonthlyBills(9, 2026, BigDecimal.valueOf(2500), LocalDate.now().plusDays(10));
        assertNotNull(bills);
        assertEquals(16, bills.size());
    }

    @Test
    void testGetApartmentBillStatus_ReturnsFull16FlatStatus() {
        ApartmentBillStatusDto status = billService.getApartmentBillStatus(8, 2026);
        assertNotNull(status);
        assertEquals(16, status.getTotalFlats());
    }
}
