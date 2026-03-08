package com.cartwave.staff.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.staff.dto.StaffDTO;
import com.cartwave.staff.entity.StaffRole;
import com.cartwave.staff.service.StaffService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<List<StaffDTO>>> listStaff() {
        return ResponseEntity.ok(ApiResponse.success("Staff retrieved successfully", staffService.listStaff()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<StaffDTO>> addStaff(@RequestBody AddStaffRequest req) {
        StaffDTO created = staffService.addStaff(req.getUserId(), req.getRole());
        return ResponseEntity.ok(ApiResponse.success("Staff added successfully", created));
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<StaffDTO>> deactivateStaff(@PathVariable UUID staffId) {
        return ResponseEntity.ok(ApiResponse.success("Staff deactivated successfully", staffService.deactivateStaff(staffId)));
    }

    @Data
    public static class AddStaffRequest {
        @NotNull
        private UUID userId;
        @NotNull
        private StaffRole role;
    }
}
