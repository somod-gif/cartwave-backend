package com.cartwave.superadmin.controller;

import com.cartwave.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        log.info("Super admin dashboard endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", "Placeholder data"));
    }

    @GetMapping("/system-stats")
    public ResponseEntity<ApiResponse<String>> getSystemStats() {
        log.info("Super admin system stats endpoint called");
        return ResponseEntity.ok(ApiResponse.success("System stats data", "Placeholder data"));
    }

}
