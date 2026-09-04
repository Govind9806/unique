package com.example.uniqueAproovaResidency.module.expense.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.document.dto.DocumentDto;
import com.example.uniqueAproovaResidency.module.document.service.DocumentService;
import com.example.uniqueAproovaResidency.module.expense.dto.CreateExpenseRequest;
import com.example.uniqueAproovaResidency.module.expense.dto.ExpenseDto;
import com.example.uniqueAproovaResidency.module.expense.service.ExpenseService;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Endpoints for creating and tracking apartment expenses and proofs")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final DocumentService documentService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Apartment Expenses")
    public ResponseEntity<ApiResponse<List<ExpenseDto>>> getAllExpenses() {
        log.info("REST REQUEST [GET /api/v1/expenses]");
        List<ExpenseDto> expenses = expenseService.getAllExpenses();
        log.info("REST RESPONSE [GET /api/v1/expenses] -> Total expenses found: {}", expenses.size());
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Expense Details by ID")
    public ResponseEntity<ApiResponse<ExpenseDto>> getExpenseById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/expenses/{}]", id);
        ExpenseDto expense = expenseService.getExpenseById(id);
        log.info("REST RESPONSE [GET /api/v1/expenses/{}] -> Category: {}, Amount: {}", id, expense.getCategory(), expense.getAmount());
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Create Expense Request & Deduct Balance")
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateExpenseRequest request) {
        log.info("REST REQUEST [POST /api/v1/expenses] -> Description: {}, Amount: {}, Category: {}, CreatedBy: {}", request.getDescription(), request.getAmount(), request.getCategory(), currentUser != null ? currentUser.getId() : "null");
        User creator = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;
        ExpenseDto dto = expenseService.createExpense(request, creator);
        log.info("REST RESPONSE [POST /api/v1/expenses] -> Created Expense ID: {}, Status: {}", dto.getId(), dto.getApprovalStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Expense submitted for approval", dto));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('TREASURER')")
    @Operation(summary = "Create Expense Request with Receipt Photo Upload (Multipart)")
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpenseWithPhoto(
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "isEmergency", required = false, defaultValue = "false") Boolean isEmergency,
            @RequestParam(value = "receiptImage", required = false) MultipartFile receiptImage,
            @AuthenticationPrincipal UserPrincipal currentUser) throws IOException {
        log.info("REST REQUEST [POST /api/v1/expenses/upload Multipart] -> Category: {}, Description: {}, Amount: {}", category, description, amount);
        User creator = currentUser != null ? userRepository.findById(currentUser.getId()).orElse(null) : null;

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setCategory(category);
        request.setDescription(description);
        request.setAmount(amount);
        request.setVendor(vendor);
        request.setIsEmergency(isEmergency);

        if (receiptImage != null && !receiptImage.isEmpty()) {
            DocumentDto doc = documentService.uploadDocument(receiptImage, "RECEIPT", "EXPENSE", null, creator);
            request.setReceiptPhotoDocumentId(doc.getId());
            request.setPhotoUrl("/api/v1/documents/" + doc.getId() + "/file");
        }

        ExpenseDto dto = expenseService.createExpense(request, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Expense recorded with receipt photo proof", dto));
    }
}
