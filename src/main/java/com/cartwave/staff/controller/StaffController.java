package com.cartwave.staff.controller;

import com.cartwave.common.dto.ApiResponse;
import com.cartwave.staff.entity.Staff;
import com.cartwave.staff.entity.StaffRole;
import com.cartwave.staff.service.StaffService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Staff>> addStaff(@RequestBody AddStaffRequest req) {
        Staff created = staffService.addStaff(req.getUserId(), req.getRole());
        return ResponseEntity.ok(ApiResponse.success("Staff added", created));
    }

    @Data
    public static class AddStaffRequest {
        private UUID userId;
        private StaffRole role;
    }

}

