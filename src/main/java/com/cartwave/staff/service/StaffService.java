package com.cartwave.staff.service;

import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.staff.entity.Staff;
import com.cartwave.staff.repository.StaffRepository;
import com.cartwave.staff.entity.StaffRole;
import com.cartwave.staff.entity.StaffStatus;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public Staff addStaff(UUID userId, StaffRole role) {
        UUID storeId = TenantContext.getTenantId();
        long currentCount = staffRepository.countByStoreIdAndDeletedFalse(storeId);
        subscriptionService.assertCanAddStaff(storeId, currentCount, 1);

        Staff staff = new Staff();
        staff.setStoreId(storeId);
        staff.setUserId(userId);
        staff.setRole(role);
        staff.setStatus(StaffStatus.ACTIVE);
        staff.setDeleted(false);
        return staffRepository.save(staff);
    }

    @Transactional(readOnly = true)
    public long countForStore(UUID storeId) {
        return staffRepository.countByStoreIdAndDeletedFalse(storeId);
    }

}

