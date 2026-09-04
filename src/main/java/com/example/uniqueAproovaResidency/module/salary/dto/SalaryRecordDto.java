package com.example.uniqueAproovaResidency.module.salary.dto;

import com.example.uniqueAproovaResidency.module.salary.entity.SalaryRecord;
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
public class SalaryRecordDto {
    private String id;
    private String employeeId;
    private String employeeName;
    private String employeeRole;
    private String monthYear;
    private BigDecimal baseSalary;
    private BigDecimal advance;
    private BigDecimal deduction;
    private BigDecimal bonus;
    private BigDecimal finalAmount;
    private LocalDate paymentDate;
    private String status;
    private String proofDocumentId;

    public static SalaryRecordDto fromEntity(SalaryRecord record) {
        return SalaryRecordDto.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeName(record.getEmployee().getName())
                .employeeRole(record.getEmployee().getRole())
                .monthYear(record.getMonthYear())
                .baseSalary(record.getBaseSalary())
                .advance(record.getAdvance())
                .deduction(record.getDeduction())
                .bonus(record.getBonus())
                .finalAmount(record.getFinalAmount())
                .paymentDate(record.getPaymentDate())
                .status(record.getStatus())
                .proofDocumentId(record.getProofDocumentId())
                .build();
    }
}
