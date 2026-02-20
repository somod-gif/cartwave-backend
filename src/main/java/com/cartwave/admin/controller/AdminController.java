package com.cartwave.admin.controller;

import com.cartwave.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_OWNER')")
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        log.info("Admin dashboard endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Dashboard data", "Placeholder data"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<String>> getStats() {
        log.info("Admin stats endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Stats data", "Placeholder data"));
    }

}
