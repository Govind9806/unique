package com.example.uniqueAproovaResidency.module.water.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import com.example.uniqueAproovaResidency.module.maintenance.repository.MaintenanceBillRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.water.dto.AddWaterReadingRequest;
import com.example.uniqueAproovaResidency.module.water.dto.WaterReadingDto;
import com.example.uniqueAproovaResidency.module.water.dto.WaterSummaryDto;
import com.example.uniqueAproovaResidency.module.water.entity.WaterBill;
import com.example.uniqueAproovaResidency.module.water.entity.WaterMeter;
import com.example.uniqueAproovaResidency.module.water.entity.WaterReading;
import com.example.uniqueAproovaResidency.module.water.repository.WaterBillRepository;
import com.example.uniqueAproovaResidency.module.water.repository.WaterMeterRepository;
import com.example.uniqueAproovaResidency.module.water.repository.WaterReadingRepository;
import com.example.uniqueAproovaResidency.module.watertanker.repository.WaterTankerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterService {

    private final WaterMeterRepository meterRepository;
    private final WaterReadingRepository readingRepository;
    private final WaterBillRepository waterBillRepository;
    private final WaterTankerRepository tankerRepository;
    private final FlatRepository flatRepository;
    private final MaintenanceBillRepository billRepository;

    @Transactional(readOnly = true)
    public List<WaterMeter> getAllMeters() {
        log.info("SERVICE [WaterService.getAllMeters]");
        return meterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WaterMeter getMeterByFlatId(String flatId) {
        log.info("SERVICE [WaterService.getMeterByFlatId] -> Flat ID: {}", flatId);
        String cleanNum = flatId.toLowerCase().startsWith("flat-") ? flatId.substring(5) : flatId;
        Flat flat = flatRepository.findByFlatNumber(cleanNum)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                        .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", flatId)));

        return meterRepository.findByFlatId(flat.getId())
                .orElseGet(() -> {
                    log.info("SERVICE [WaterService.getMeterByFlatId] -> Auto-registering Water Meter for Flat {}", flat.getFlatNumber());
                    WaterMeter newMeter = WaterMeter.builder()
                            .id(UUID.randomUUID().toString())
                            .flat(flat)
                            .meterNumber("WM-" + flat.getFlatNumber())
                            .installationDate(LocalDate.now())
                            .status("ACTIVE")
                            .build();
                    return meterRepository.save(newMeter);
                });
    }

    @Transactional
    public WaterReadingDto recordReading(AddWaterReadingRequest request, User recorder) {
        log.info("SERVICE [WaterService.recordReading] -> Flat ID: {}, Current Reading: {}", request.getFlatId(), request.getCurrentReading());

        if (request.getFlatId() == null || request.getFlatId().isBlank()) {
            throw new BusinessRuleException("MISSING_FLAT", "Flat ID is required");
        }
        if (request.getCurrentReading() == null) {
            throw new BusinessRuleException("MISSING_CURRENT_READING", "Current meter reading is required");
        }
        if (request.getCurrentReading().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("INVALID_READING", "Meter reading cannot be negative");
        }
        if (request.getMeterPhotoDocumentId() == null || request.getMeterPhotoDocumentId().isBlank()) {
            throw new BusinessRuleException("MISSING_METER_PHOTO", "A photograph of the physical water meter is mandatory for verification and transparency.");
        }

        String cleanNum = request.getFlatId().toLowerCase().startsWith("flat-") ? request.getFlatId().substring(5) : request.getFlatId();
        Flat flat = flatRepository.findByFlatNumber(cleanNum)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                        .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", request.getFlatId())));

        WaterMeter meter = getMeterByFlatId(flat.getId());

        Optional<WaterReading> latestOpt = readingRepository.findFirstByFlatIdOrderByReadingDateDesc(flat.getId());
        LocalDate readingDate = request.getReadingDate() != null ? request.getReadingDate() : LocalDate.now();

        WaterReading savedReading = null;
        BigDecimal prevReading = BigDecimal.ZERO;
        BigDecimal consumption = BigDecimal.ZERO;

        if (latestOpt.isEmpty()) {
            // First reading ever recorded for this flat -> Compute consumption from 0 to current reading
            prevReading = BigDecimal.ZERO;
            consumption = request.getCurrentReading();

            log.info("SERVICE [WaterService.recordReading] -> Initial reading for Flat {}. Current: {}, Consumption: {}", flat.getFlatNumber(), request.getCurrentReading(), consumption);

            savedReading = WaterReading.builder()
                    .id(UUID.randomUUID().toString())
                    .meter(meter)
                    .flat(flat)
                    .previousReading(prevReading)
                    .currentReading(request.getCurrentReading())
                    .consumption(consumption)
                    .readingDate(readingDate)
                    .readingType("REGULAR")
                    .recordedBy(recorder)
                    .meterPhotoDocumentId(request.getMeterPhotoDocumentId())
                    .build();

            savedReading = readingRepository.save(savedReading);
        } else {
            WaterReading lastReading = latestOpt.get();
            LocalDate lastReadingDate = lastReading.getReadingDate();

            // Check if reading is in the SAME calendar month -> UPDATE existing month reading
            if (lastReadingDate != null
                    && readingDate.getMonthValue() == lastReadingDate.getMonthValue()
                    && readingDate.getYear() == lastReadingDate.getYear()) {

                prevReading = lastReading.getPreviousReading() != null ? lastReading.getPreviousReading() : BigDecimal.ZERO;
                if (request.getCurrentReading().compareTo(prevReading) < 0) {
                    prevReading = BigDecimal.ZERO;
                }
                consumption = request.getCurrentReading().subtract(prevReading);

                log.info("SERVICE [WaterService.recordReading] -> Updating current month reading for Flat {}. Prev: {}, New Current: {}, Consumption: {}", flat.getFlatNumber(), prevReading, request.getCurrentReading(), consumption);

                lastReading.setPreviousReading(prevReading);
                lastReading.setCurrentReading(request.getCurrentReading());
                lastReading.setConsumption(consumption);
                lastReading.setReadingType("REGULAR");
                lastReading.setReadingDate(readingDate);
                lastReading.setRecordedBy(recorder);
                lastReading.setMeterPhotoDocumentId(request.getMeterPhotoDocumentId());

                savedReading = readingRepository.save(lastReading);
            } else {
                // Reading for a NEW month -> Previous reading is last month's ending currentReading
                prevReading = lastReading.getCurrentReading();
                if (request.getCurrentReading().compareTo(prevReading) < 0) {
                    throw new BusinessRuleException("INVALID_READING", "Current meter reading (" + request.getCurrentReading() + ") cannot be less than the previous reading (" + prevReading + ").");
                }
                consumption = request.getCurrentReading().subtract(prevReading);

                log.info("SERVICE [WaterService.recordReading] -> New month reading for Flat {}. Prev: {}, Current: {}, Consumption: {}", flat.getFlatNumber(), prevReading, request.getCurrentReading(), consumption);

                savedReading = WaterReading.builder()
                        .id(UUID.randomUUID().toString())
                        .meter(meter)
                        .flat(flat)
                        .previousReading(prevReading)
                        .currentReading(request.getCurrentReading())
                        .consumption(consumption)
                        .readingDate(readingDate)
                        .readingType("REGULAR")
                        .recordedBy(recorder)
                        .meterPhotoDocumentId(request.getMeterPhotoDocumentId())
                        .build();

                savedReading = readingRepository.save(savedReading);
            }
        }

        // Calculate Water Bill Amount (Rate: ₹40 / unit)
        BigDecimal ratePerUnit = BigDecimal.valueOf(40);
        BigDecimal billAmount = consumption.multiply(ratePerUnit);
        String billingPeriod = readingDate.getMonth().name() + " " + readingDate.getYear();

        // Save or Update WaterBill record
        WaterBill waterBill = waterBillRepository.findByReadingId(savedReading.getId())
                .orElseGet(() -> waterBillRepository.findByFlatIdAndBillingPeriod(flat.getId(), billingPeriod)
                        .orElse(null));

        if (waterBill == null) {
            waterBill = WaterBill.builder()
                    .id(UUID.randomUUID().toString())
                    .flat(flat)
                    .reading(savedReading)
                    .previousReading(prevReading)
                    .currentReading(request.getCurrentReading())
                    .consumption(consumption)
                    .ratePerUnit(ratePerUnit)
                    .fixedCharge(BigDecimal.ZERO)
                    .finalAmount(billAmount)
                    .billingPeriod(billingPeriod)
                    .status("PENDING")
                    .build();
        } else {
            waterBill.setReading(savedReading);
            waterBill.setPreviousReading(prevReading);
            waterBill.setCurrentReading(request.getCurrentReading());
            waterBill.setConsumption(consumption);
            waterBill.setRatePerUnit(ratePerUnit);
            waterBill.setFinalAmount(billAmount);
        }
        waterBill = waterBillRepository.save(waterBill);

        // Recalculate & Sync Total Monthly Bill (Fixed Maintenance ₹1250 + Water Bill Amount)
        int month = readingDate.getMonthValue();
        int year = readingDate.getYear();
        BigDecimal totalMonthlyBill = BigDecimal.valueOf(1250).add(billAmount);

        Optional<MaintenanceBill> mbOpt = billRepository.findByFlatIdAndBillingMonthAndBillingYear(flat.getId(), month, year);
        if (mbOpt.isPresent()) {
            MaintenanceBill mb = mbOpt.get();
            mb.setAmount(totalMonthlyBill);
            billRepository.save(mb);
            log.info("SERVICE [WaterService.recordReading] -> Synced MaintenanceBill for Flat {} ({}/{}): Total Bill = ₹{} (₹1250 fixed + ₹{} water)", flat.getFlatNumber(), month, year, totalMonthlyBill, billAmount);
        } else {
            MaintenanceBill mb = MaintenanceBill.builder()
                    .id(UUID.randomUUID().toString())
                    .flat(flat)
                    .billingMonth(month)
                    .billingYear(year)
                    .amount(totalMonthlyBill)
                    .dueDate(LocalDate.of(year, month, 10))
                    .status("PENDING")
                    .build();
            billRepository.save(mb);
            log.info("SERVICE [WaterService.recordReading] -> Created MaintenanceBill for Flat {} ({}/{}): Total Bill = ₹{} (₹1250 fixed + ₹{} water)", flat.getFlatNumber(), month, year, totalMonthlyBill, billAmount);
        }

        log.info("SERVICE [WaterService.recordReading] -> Saved Water Reading ID {}, Units: {} units, Water Bill: ₹{}, Total Monthly Bill: ₹{}",
                savedReading.getId(), consumption, billAmount, totalMonthlyBill);

        WaterReadingDto dto = WaterReadingDto.fromEntity(savedReading);
        dto.setRatePerUnit(ratePerUnit);
        dto.setWaterBillAmount(billAmount);
        dto.setBillStatus(waterBill.getStatus());
        return dto;
    }

    @Transactional(readOnly = true)
    public WaterReadingDto getLatestReadingForFlat(String flatId) {
        log.info("SERVICE [WaterService.getLatestReadingForFlat] -> Flat ID: {}", flatId);
        String cleanNum = flatId.toLowerCase().startsWith("flat-") ? flatId.substring(5) : flatId;
        Flat flat = flatRepository.findByFlatNumber(cleanNum)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                        .orElse(null));

        if (flat == null) return null;

        List<WaterReading> readings = readingRepository.findByFlatIdOrderByReadingDateDesc(flat.getId());
        if (readings.isEmpty()) return null;

        WaterReading reading = readings.get(0);
        WaterReadingDto dto = WaterReadingDto.fromEntity(reading);

        if (readings.size() > 1) {
            WaterReading prevReading = readings.get(1);
            if (prevReading.getMeterPhotoDocumentId() != null) {
                dto.setPreviousMeterPhotoDocumentId(prevReading.getMeterPhotoDocumentId());
                dto.setPreviousMeterPhotoUrl("/api/v1/documents/" + prevReading.getMeterPhotoDocumentId() + "/file");
            }
        }

        Optional<WaterBill> billOpt = waterBillRepository.findByReadingId(reading.getId());
        if (billOpt.isPresent()) {
            dto.setRatePerUnit(billOpt.get().getRatePerUnit());
            dto.setWaterBillAmount(billOpt.get().getFinalAmount());
            dto.setBillStatus(billOpt.get().getStatus());
        } else {
            BigDecimal waterBillAmount = reading.getConsumption() != null
                    ? reading.getConsumption().multiply(BigDecimal.valueOf(40))
                    : BigDecimal.ZERO;
            dto.setWaterBillAmount(waterBillAmount);
            dto.setBillStatus("PENDING");
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<WaterReadingDto> getReadingsForFlat(String flatId) {
        log.info("SERVICE [WaterService.getReadingsForFlat] -> Flat ID: {}", flatId);
        String cleanNum = flatId.toLowerCase().startsWith("flat-") ? flatId.substring(5) : flatId;
        Flat flat = flatRepository.findByFlatNumber(cleanNum)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                        .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", flatId)));

        List<WaterReading> readings = readingRepository.findByFlatIdOrderByReadingDateDesc(flat.getId());

        List<WaterReadingDto> dtos = new java.util.ArrayList<>();
        for (int i = 0; i < readings.size(); i++) {
            WaterReading reading = readings.get(i);
            WaterReadingDto dto = WaterReadingDto.fromEntity(reading);

            if (i + 1 < readings.size()) {
                WaterReading prevReading = readings.get(i + 1);
                if (prevReading.getMeterPhotoDocumentId() != null) {
                    dto.setPreviousMeterPhotoDocumentId(prevReading.getMeterPhotoDocumentId());
                    dto.setPreviousMeterPhotoUrl("/api/v1/documents/" + prevReading.getMeterPhotoDocumentId() + "/file");
                }
            }

            Optional<WaterBill> billOpt = waterBillRepository.findByReadingId(reading.getId());
            if (billOpt.isPresent()) {
                dto.setRatePerUnit(billOpt.get().getRatePerUnit());
                dto.setWaterBillAmount(billOpt.get().getFinalAmount());
                dto.setBillStatus(billOpt.get().getStatus());
            } else {
                BigDecimal waterBillAmount = reading.getConsumption() != null
                        ? reading.getConsumption().multiply(BigDecimal.valueOf(40))
                        : BigDecimal.ZERO;
                dto.setWaterBillAmount(waterBillAmount);
                dto.setBillStatus("PENDING");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public WaterSummaryDto getWaterSummary() {
        log.info("SERVICE [WaterService.getWaterSummary]");
        List<WaterMeter> meters = meterRepository.findAll();
        List<WaterReading> allReadings = readingRepository.findAll();

        BigDecimal totalConsumption = allReadings.stream()
                .map(WaterReading::getConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int tankerCount = tankerRepository.findAll().size();

        List<WaterReadingDto> recent = allReadings.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(WaterReadingDto::fromEntity)
                .collect(Collectors.toList());

        return WaterSummaryDto.builder()
                .totalMeters(meters.size())
                .totalConsumptionThisMonth(totalConsumption)
                .totalWaterExpensesThisMonth(BigDecimal.valueOf(15000))
                .tankerCountThisMonth(tankerCount)
                .recentReadings(recent)
                .build();
    }
}
