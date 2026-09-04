package com.example.uniqueAproovaResidency.module.work.controller;

import com.example.uniqueAproovaResidency.common.ApiResponse;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.module.work.dto.CreateWorkRequest;
import com.example.uniqueAproovaResidency.module.work.dto.WorkDto;
import com.example.uniqueAproovaResidency.module.work.dto.WorkTimelineDto;
import com.example.uniqueAproovaResidency.module.work.service.WorkService;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/works")
@RequiredArgsConstructor
@Tag(name = "Work / Repair Management", description = "Endpoints for apartment pipeline, plumbing, electrical, and maintenance repairs")
public class WorkController {

    private final WorkService workService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get All Apartment Repair Works")
    public ResponseEntity<ApiResponse<List<WorkDto>>> getAllWorks() {
        log.info("REST REQUEST [GET /api/v1/works]");
        List<WorkDto> works = workService.getAllWorks();
        log.info("REST RESPONSE [GET /api/v1/works] -> Total works found: {}", works.size());
        return ResponseEntity.ok(ApiResponse.success(works));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Work Details by ID")
    public ResponseEntity<ApiResponse<WorkDto>> getWorkById(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/works/{}]", id);
        WorkDto work = workService.getWorkById(id);
        return ResponseEntity.ok(ApiResponse.success(work));
    }

    @PostMapping
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Create New Repair Work Request")
    public ResponseEntity<ApiResponse<WorkDto>> createWork(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateWorkRequest request) {
        log.info("REST REQUEST [POST /api/v1/works] -> Title: {}, EstimatedCost: {}", request.getTitle(), request.getEstimatedCost());
        User creator = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.createWork(request, creator);
        log.info("REST RESPONSE [POST /api/v1/works] -> Created Work ID: {}, Status: {}", work.getId(), work.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Work request created", work));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('FLAT_MEMBER')")
    @Operation(summary = "Approve Repair Work")
    public ResponseEntity<ApiResponse<WorkDto>> approveWork(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/approve]", id);
        User approver = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.approveWork(id, approver);
        return ResponseEntity.ok(ApiResponse.success("Work approved", work));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Mark Repair Work Started")
    public ResponseEntity<ApiResponse<WorkDto>> startWork(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/start]", id);
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.startWork(id, user);
        return ResponseEntity.ok(ApiResponse.success("Work started", work));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Mark Repair Work Completed")
    public ResponseEntity<ApiResponse<WorkDto>> completeWork(
            @PathVariable String id,
            @RequestParam(required = false) BigDecimal actualCost,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/complete] -> Actual Cost: {}", id, actualCost);
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.completeWork(id, actualCost, user);
        return ResponseEntity.ok(ApiResponse.success("Work completed", work));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get Work Timeline History")
    public ResponseEntity<ApiResponse<List<WorkTimelineDto>>> getWorkTimeline(@PathVariable String id) {
        log.info("REST REQUEST [GET /api/v1/works/{}/timeline]", id);
        List<WorkTimelineDto> timeline = workService.getWorkTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('FLAT_MEMBER') or hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Cast Flat Member Vote (Accept / Reject) on Repair Proposal")
    public ResponseEntity<ApiResponse<WorkDto>> voteOnWork(
            @PathVariable String id,
            @RequestParam String vote,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/vote] -> Vote: {}", id, vote);
        User voter = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.voteOnWork(id, vote, reason, voter);
        return ResponseEntity.ok(ApiResponse.success("Vote recorded successfully", work));
    }

    @PostMapping("/{id}/maintenance-respond")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('ROLE_MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER')")
    @Operation(summary = "Maintenance Flat Accepts or Rejects Approved Repair Request + Optional Message")
    public ResponseEntity<ApiResponse<WorkDto>> maintenanceRespond(
            @PathVariable String id,
            @RequestParam String decision,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/maintenance-respond] -> Decision: {}, Message: {}", id, decision, message);
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.maintenanceRespond(id, decision, message, user);
        return ResponseEntity.ok(ApiResponse.success("Maintenance response recorded successfully", work));
    }

    @PostMapping("/{id}/start-and-pay")
    @PreAuthorize("hasRole('MAINTENANCE_FLAT') or hasRole('MAINTENANCE_MEMBER') or hasRole('ADMIN')")
    @Operation(summary = "Start Approved Repair Work & Pay Final Cost with Proof Photo")
    public ResponseEntity<ApiResponse<WorkDto>> startAndPayWork(
            @PathVariable String id,
            @RequestParam BigDecimal finalCost,
            @RequestParam String photoUrl,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        log.info("REST REQUEST [POST /api/v1/works/{}/start-and-pay] -> Cost: ₹{}, Photo: {}", id, finalCost, photoUrl);
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        WorkDto work = workService.startAndPayWork(id, finalCost, photoUrl, user);
        return ResponseEntity.ok(ApiResponse.success("Work started, completed & payment recorded with proof", work));
    }
}
