package com.cartwave.staff.service;

import com.cartwave.exception.BusinessException;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.staff.dto.StaffDTO;
import com.cartwave.staff.entity.Staff;
import com.cartwave.staff.entity.StaffRole;
import com.cartwave.staff.entity.StaffStatus;
import com.cartwave.staff.repository.StaffRepository;
import com.cartwave.subscription.service.SubscriptionService;
import com.cartwave.tenant.TenantContext;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public StaffDTO addStaff(UUID userId, StaffRole role) {
        UUID storeId = TenantContext.getTenantId();
        long currentCount = staffRepository.countByStoreIdAndDeletedFalse(storeId);
        subscriptionService.assertCanAddStaff(storeId, currentCount, 1);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        staffRepository.findByUserIdAndStoreId(userId, storeId).ifPresent(existing -> {
            throw new BusinessException("STAFF_EXISTS", "Staff member already exists for this store.");
        });

        Staff staff = Staff.builder()
                .userId(userId)
                .storeId(storeId)
                .role(role)
                .status(StaffStatus.ACTIVE)
                .permissionLevel(role.name())
                .hiredAt(Instant.now().toEpochMilli())
                .build();

        Staff saved = staffRepository.save(staff);
        if (user.getRole() == UserRole.CUSTOMER) {
            user.setRole(UserRole.STAFF);
            userRepository.save(user);
        }
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> listStaff() {
        UUID storeId = TenantContext.getTenantId();
        return staffRepository.findByStoreId(storeId).stream().map(this::toDto).toList();
    }

    public StaffDTO deactivateStaff(UUID staffId) {
        UUID storeId = TenantContext.getTenantId();
        Staff staff = staffRepository.findByIdAndStoreId(staffId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "id", staffId));
        staff.setStatus(StaffStatus.TERMINATED);
        staff.setDeleted(true);
        Staff saved = staffRepository.save(staff);

        User user = userRepository.findById(staff.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", staff.getUserId()));
        if (user.getRole() == UserRole.STAFF && staffRepository.countActiveByUserId(user.getId()) == 0) {
            user.setRole(UserRole.CUSTOMER);
            userRepository.save(user);
        }
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public long countForStore(UUID storeId) {
        return staffRepository.countByStoreIdAndDeletedFalse(storeId);
    }

    private StaffDTO toDto(Staff staff) {
        return StaffDTO.builder()
                .id(staff.getId())
                .userId(staff.getUserId())
                .storeId(staff.getStoreId())
                .role(staff.getRole().name())
                .status(staff.getStatus().name())
                .permissionLevel(staff.getPermissionLevel())
                .notes(staff.getNotes())
                .hiredAt(staff.getHiredAt())
                .createdAt(staff.getCreatedAt())
                .build();
    }
}
