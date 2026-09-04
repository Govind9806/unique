package com.example.uniqueAproovaResidency.module.report.service;

import com.example.uniqueAproovaResidency.module.finance.dto.FinanceSummaryDto;
import com.example.uniqueAproovaResidency.module.finance.service.FinanceService;
import com.example.uniqueAproovaResidency.module.maintenance.dto.ApartmentBillStatusDto;
import com.example.uniqueAproovaResidency.module.maintenance.service.BillService;
import com.example.uniqueAproovaResidency.module.water.dto.WaterSummaryDto;
import com.example.uniqueAproovaResidency.module.water.service.WaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final FinanceService financeService;
    private final BillService billService;
    private final WaterService waterService;

    @Transactional(readOnly = true)
    public FinanceSummaryDto getFinancialReport() {
        return financeService.getSummary();
    }

    @Transactional(readOnly = true)
    public ApartmentBillStatusDto getMaintenanceCollectionReport(Integer month, Integer year) {
        return billService.getApartmentBillStatus(month, year);
    }

    @Transactional(readOnly = true)
    public WaterSummaryDto getWaterReport() {
        return waterService.getWaterSummary();
    }
}
