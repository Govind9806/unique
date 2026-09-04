package com.example.uniqueAproovaResidency;

import com.example.uniqueAproovaResidency.module.water.dto.AddWaterReadingRequest;
import com.example.uniqueAproovaResidency.module.water.dto.WaterReadingDto;
import com.example.uniqueAproovaResidency.module.water.repository.WaterBillRepository;
import com.example.uniqueAproovaResidency.module.water.service.WaterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    "JPA_DDL_AUTO=update",
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
class WaterServiceTest {

    @Autowired
    private WaterService waterService;

    @Autowired
    private WaterBillRepository waterBillRepository;

    @Test
    void testRecordReading_CalculatesConsumptionAndGeneratesBill() {
        AddWaterReadingRequest request = new AddWaterReadingRequest();
        request.setFlatId("flat-101");
        request.setCurrentReading(BigDecimal.valueOf(118.0));
        request.setReadingDate(LocalDate.now());
        request.setMeterPhotoDocumentId("doc-water-photo-101");

        WaterReadingDto readingDto = waterService.recordReading(request, null);
        assertNotNull(readingDto);
        assertEquals(0, BigDecimal.valueOf(118.0).compareTo(readingDto.getConsumption()));

        assertFalse(waterBillRepository.findAll().isEmpty());
    }
}
