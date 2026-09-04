package com.example.uniqueAproovaResidency.module.maintenance.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.BillDto;
import com.example.uniqueAproovaResidency.module.maintenance.dto.FlatDuesSummaryDto;
import com.example.uniqueAproovaResidency.module.maintenance.entity.MaintenanceBill;
import com.example.uniqueAproovaResidency.module.maintenance.repository.MaintenanceBillRepository;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.water.entity.WaterBill;
import com.example.uniqueAproovaResidency.module.water.entity.WaterReading;
import com.example.uniqueAproovaResidency.module.water.repository.WaterBillRepository;
import com.example.uniqueAproovaResidency.module.water.repository.WaterReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final MaintenanceBillRepository billRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;
    private final WaterReadingRepository waterReadingRepository;
    private final WaterBillRepository waterBillRepository;

    private static final BigDecimal FIXED_MAINTENANCE_FEE = BigDecimal.valueOf(1250);

    @Transactional(readOnly = true)
    public List<BillDto> getBillsForFlat(String flatId) {
        log.info("Fetching bills for flatId: {}", flatId);
        return billRepository.findByFlatId(flatId).stream()
                .map(BillDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDto> getPendingBillsForFlat(String flatId) {
        log.info("Fetching pending bills for flatId: {}", flatId);
        return billRepository.findByFlatIdAndStatus(flatId, "PENDING").stream()
                .map(BillDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillDto getBillById(String id) {
        log.info("Fetching bill by ID: {}", id);
        MaintenanceBill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceBill", "id", id));
        return BillDto.fromEntity(bill);
    }

    @Transactional(readOnly = true)
    public FlatDuesSummaryDto getFlatDuesSummary(String flatNumber) {
        log.info("Fetching authoritative flat dues summary from DB for flatNumber: {}", flatNumber);
        String cleanNum = flatNumber.toLowerCase().startsWith("flat-") ? flatNumber.substring(5) : flatNumber;

        Flat flat = flatRepository.findByFlatNumber(cleanNum)
                .orElseGet(() -> flatRepository.findById("flat-" + cleanNum)
                        .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatNumber", flatNumber)));

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        MaintenanceBill currentMonthBill = billRepository.findByFlatIdAndBillingMonthAndBillingYear(flat.getId(), currentMonth, currentYear)
                .orElse(null);

        // Fixed Maintenance Bill = ₹1,250
        BigDecimal fixedMaintenance = FIXED_MAINTENANCE_FEE;
        BigDecimal waterBillAmt = BigDecimal.ZERO;
        BigDecimal waterUnits = BigDecimal.ZERO;

        // Fetch latest water reading & calculated water bill for this flat
        Optional<WaterReading> latestReadingOpt = waterReadingRepository.findFirstByFlatIdOrderByReadingDateDesc(flat.getId());
        if (latestReadingOpt.isEmpty()) {
            latestReadingOpt = waterReadingRepository.findFirstByFlatIdOrderByReadingDateDesc("flat-" + flat.getFlatNumber());
        }

        if (latestReadingOpt.isPresent()) {
            WaterReading reading = latestReadingOpt.get();
            if (reading.getConsumption() != null && reading.getConsumption().compareTo(BigDecimal.ZERO) > 0) {
                waterUnits = reading.getConsumption();
                Optional<WaterBill> wbOpt = waterBillRepository.findByReadingId(reading.getId());
                if (wbOpt.isPresent() && wbOpt.get().getFinalAmount() != null && wbOpt.get().getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
                    waterBillAmt = wbOpt.get().getFinalAmount();
                } else {
                    waterBillAmt = waterUnits.multiply(BigDecimal.valueOf(40));
                }
            }
        }

        BigDecimal totalMonthlyBill = fixedMaintenance.add(waterBillAmt);

        String currentMonthStatus = (currentMonthBill != null) ? currentMonthBill.getStatus() : "PAID";
        BigDecimal currentMonthPendingAmount = "PENDING".equalsIgnoreCase(currentMonthStatus) ? totalMonthlyBill : BigDecimal.ZERO;

        List<MaintenanceBill> pendingBills = billRepository.findByFlatIdAndStatus(flat.getId(), "PENDING");

        List<MaintenanceBill> pastPendingBills = pendingBills.stream()
                .filter(b -> !(b.getBillingMonth() == currentMonth && b.getBillingYear() == currentYear))
                .collect(Collectors.toList());

        BigDecimal previousPendingArrears = pastPendingBills.stream()
                .map(MaintenanceBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallPendingAmount = currentMonthPendingAmount.add(previousPendingArrears);
        int pastUnpaidMonths = pastPendingBills.size();
        int totalUnpaidMonths = pendingBills.size();

        User resident = userRepository.findByFlatId(flat.getId()).stream().findFirst().orElse(null);
        String residentName = resident != null ? resident.getName() : "Resident";
        String residentPhone = resident != null ? resident.getPhone() : "N/A";

        List<BillDto> pendingBillDtos = pendingBills.stream()
                .map(BillDto::fromEntity)
                .collect(Collectors.toList());

        return FlatDuesSummaryDto.builder()
                .flatId(flat.getId())
                .flatNumber(flat.getFlatNumber())
                .residentName(residentName)
                .residentPhone(residentPhone)
                .currentMonthStatus(currentMonthStatus)
                .fixedMaintenanceAmount(fixedMaintenance)
                .waterBillAmount(waterBillAmt)
                .waterUnitsConsumed(waterUnits)
                .totalMonthlyBill(totalMonthlyBill)
                .currentMonthPendingAmount(currentMonthPendingAmount)
                .previousPendingArrears(previousPendingArrears)
                .overallPendingAmount(overallPendingAmount)
                .totalUnpaidMonths(totalUnpaidMonths)
                .pastUnpaidMonths(pastUnpaidMonths)
                .pendingBills(pendingBillDtos)
                .build();
    }

    @Transactional
    public List<BillDto> generateMonthlyBills(Integer month, Integer year, BigDecimal amount, LocalDate dueDate) {
        log.info("Generating monthly maintenance bills for month {}/{} fixed amount ₹1250", month, year);
        List<Flat> flats = flatRepository.findAll();
        List<MaintenanceBill> newBills = new ArrayList<>();

        for (Flat flat : flats) {
            if (!billRepository.existsByFlatIdAndBillingMonthAndBillingYear(flat.getId(), month, year)) {
                MaintenanceBill bill = MaintenanceBill.builder()
                        .id(UUID.randomUUID().toString())
                        .flat(flat)
                        .billingMonth(month)
                        .billingYear(year)
                        .amount(FIXED_MAINTENANCE_FEE)
                        .dueDate(dueDate != null ? dueDate : LocalDate.of(year, month, 10))
                        .status("PENDING")
                        .build();
                newBills.add(billRepository.save(bill));
            }
        }
        return newBills.stream().map(BillDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApartmentBillStatusDto getApartmentBillStatus(Integer month, Integer year) {
        if (month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        List<Flat> flats = flatRepository.findAll();
        List<MaintenanceBill> monthBills = billRepository.findByBillingMonthAndBillingYear(month, year);

        int paidCount = 0;
        int pendingCount = 0;
        int overdueCount = 0;
        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        List<ApartmentBillStatusDto.FlatBillItem> items = new ArrayList<>();

        for (Flat flat : flats) {
            MaintenanceBill bill = monthBills.stream()
                    .filter(b -> b.getFlat().getId().equals(flat.getId()))
                    .findFirst()
                    .orElse(null);

            String status = bill != null ? bill.getStatus() : "PAID";
            BigDecimal fixedAmt = FIXED_MAINTENANCE_FEE;
            BigDecimal waterBillAmt = BigDecimal.ZERO;
            BigDecimal waterUnits = BigDecimal.ZERO;

            Optional<WaterReading> latestReadingOpt = waterReadingRepository.findFirstByFlatIdOrderByReadingDateDesc(flat.getId());
            if (latestReadingOpt.isPresent()) {
                WaterReading reading = latestReadingOpt.get();
                if ("REGULAR".equalsIgnoreCase(reading.getReadingType())) {
                    waterUnits = reading.getConsumption() != null ? reading.getConsumption() : BigDecimal.ZERO;
                    Optional<WaterBill> wbOpt = waterBillRepository.findByReadingId(reading.getId());
                    if (wbOpt.isPresent()) {
                        waterBillAmt = wbOpt.get().getFinalAmount();
                    } else {
                        waterBillAmt = waterUnits.multiply(BigDecimal.valueOf(40));
                    }
                }
            }

            BigDecimal totalMonthlyBill = fixedAmt.add(waterBillAmt);
            totalExpected = totalExpected.add(totalMonthlyBill);

            if ("PAID".equalsIgnoreCase(status)) {
                paidCount++;
                totalCollected = totalCollected.add(totalMonthlyBill);
            } else if ("OVERDUE".equalsIgnoreCase(status)) {
                overdueCount++;
                totalPending = totalPending.add(totalMonthlyBill);
            } else {
                pendingCount++;
                totalPending = totalPending.add(totalMonthlyBill);
            }

            List<MaintenanceBill> pendingBills = billRepository.findByFlatIdAndStatus(flat.getId(), "PENDING");
            BigDecimal currentMonthPending = "PAID".equalsIgnoreCase(status) ? BigDecimal.ZERO : totalMonthlyBill;
            BigDecimal overallPending = pendingBills.stream()
                    .map(MaintenanceBill::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int unpaidMonths = pendingBills.size();

            String residentName = userRepository.findByFlatId(flat.getId()).stream()
                    .map(User::getName)
                    .findFirst()
                    .orElse("Resident");

            items.add(ApartmentBillStatusDto.FlatBillItem.builder()
                    .flatId(flat.getId())
                    .flatNumber(flat.getFlatNumber())
                    .status(status)
                    .fixedMaintenanceAmount(fixedAmt)
                    .waterBillAmount(waterBillAmt)
                    .waterUnitsConsumed(waterUnits)
                    .totalMonthlyBill(totalMonthlyBill)
                    .amount(totalMonthlyBill)
                    .residentName(residentName)
                    .currentMonthPendingAmount(currentMonthPending)
                    .overallPendingAmount(overallPending)
                    .totalUnpaidMonths(unpaidMonths)
                    .build());
        }

        return ApartmentBillStatusDto.builder()
                .totalFlats(flats.size())
                .paidFlats(paidCount)
                .pendingFlats(pendingCount)
                .overdueFlats(overdueCount)
                .totalExpected(totalExpected)
                .totalCollected(totalCollected)
                .totalPending(totalPending)
                .flatStatuses(items)
                .build();
    }
}
