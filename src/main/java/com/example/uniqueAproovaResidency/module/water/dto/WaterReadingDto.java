package com.example.uniqueAproovaResidency.module.water.dto;

import com.example.uniqueAproovaResidency.module.water.entity.WaterReading;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterReadingDto {
    private String id;
    private String meterId;
    private String flatId;
    private String flatNumber;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;
    private LocalDate readingDate;
    private String readingType;
    private String recordedByName;
    private String meterPhotoDocumentId;
    private String meterPhotoUrl;
    private String previousMeterPhotoDocumentId;
    private String previousMeterPhotoUrl;
    private BigDecimal ratePerUnit;
    private BigDecimal waterBillAmount;
    private String billStatus;
    private Boolean isCurrentMonthRecorded;
    private String nextAllowedMonth;

    public static WaterReadingDto fromEntity(WaterReading reading) {
        LocalDate lastDate = reading.getReadingDate();
        LocalDate now = LocalDate.now();
        boolean isSameMonth = lastDate != null && lastDate.getMonthValue() == now.getMonthValue() && lastDate.getYear() == now.getYear();
        LocalDate nextMonthDate = lastDate != null ? lastDate.plusMonths(1).withDayOfMonth(1) : null;
        String nextMonthStr = nextMonthDate != null ? (nextMonthDate.getMonth().name() + " " + nextMonthDate.getYear()) : null;

        return WaterReadingDto.builder()
                .id(reading.getId())
                .meterId(reading.getMeter() != null ? reading.getMeter().getId() : null)
                .flatId(reading.getFlat().getId())
                .flatNumber(reading.getFlat().getFlatNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .consumption(reading.getConsumption())
                .readingDate(reading.getReadingDate())
                .readingType(reading.getReadingType() != null ? reading.getReadingType() : "REGULAR")
                .recordedByName(reading.getRecordedBy() != null ? reading.getRecordedBy().getName() : null)
                .meterPhotoDocumentId(reading.getMeterPhotoDocumentId())
                .meterPhotoUrl(reading.getMeterPhotoDocumentId() != null
                        ? "/api/v1/documents/" + reading.getMeterPhotoDocumentId() + "/file"
                        : null)
                .isCurrentMonthRecorded(isSameMonth)
                .nextAllowedMonth(nextMonthStr)
                .build();
    }
}
