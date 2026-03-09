package com.cartwave.admin.service;

import com.cartwave.admin.dto.CreateAdminRequest;
import com.cartwave.admin.dto.CreatePlanRequest;
import com.cartwave.admin.dto.PlatformHealthDTO;
import com.cartwave.admin.dto.RevenueSummaryDTO;
import com.cartwave.admin.dto.UserAdminDTO;
import com.cartwave.exception.BusinessException;
import com.cartwave.email.entity.EmailStatus;
import com.cartwave.email.repository.EmailQueueRepository;
import com.cartwave.escrow.entity.EscrowDisputeStatus;
import com.cartwave.escrow.repository.EscrowDisputeRepository;
import com.cartwave.exception.ResourceNotFoundException;
import com.cartwave.order.repository.OrderRepository;
import com.cartwave.order.entity.PaymentStatus;
import com.cartwave.payment.repository.PaymentRepository;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.entity.SubscriptionStatus;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final EscrowDisputeRepository escrowDisputeRepository;
    private final EmailQueueRepository emailQueueRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserAdminDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> Boolean.FALSE.equals(u.getDeleted()))
                .map(this::toUserDto)
                .toList();
    }

    public UserAdminDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toUserDto(user);
    }

    @Transactional
    public UserAdminDTO suspendUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setStatus(UserStatus.SUSPENDED);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserAdminDTO activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setStatus(UserStatus.ACTIVE);
        return toUserDto(userRepository.save(user));
    }

    public RevenueSummaryDTO getRevenueSummary() {
        Instant monthStart = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(30, ChronoUnit.DAYS);

        return RevenueSummaryDTO.builder()
                .totalRevenue(paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED))
                .monthlyRevenue(paymentRepository.sumAmountByStatusSince(PaymentStatus.COMPLETED, monthStart))
                .totalTransactions(paymentRepository.countByDeletedFalse())
                .totalOrders(orderRepository.countByDeletedFalse())
                .totalStores(storeRepository.countByDeletedFalse())
                .totalUsers(userRepository.countByDeletedFalse())
                .activeSubscriptions(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE))
                .build();
    }

    public PlatformHealthDTO getPlatformHealth() {
        return PlatformHealthDTO.builder()
                .totalUsers(userRepository.countByDeletedFalse())
                .totalStores(storeRepository.countByDeletedFalse())
                .totalOrders(orderRepository.countByDeletedFalse())
                .pendingDisputes(escrowDisputeRepository.countByStatusAndDeletedFalse(EscrowDisputeStatus.OPEN))
                .pendingEmails(emailQueueRepository.countByStatus(EmailStatus.PENDING))
                .activeSubscriptions(subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE))
                .status("OPERATIONAL")
                .build();
    }

    @Transactional
    public SubscriptionPlan createPlan(CreatePlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(request.getName().toUpperCase());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setProductLimit(request.getProductLimit());
        plan.setStaffLimit(request.getStaffLimit());
        plan.setPaymentsEnabled(Boolean.TRUE.equals(request.getPaymentsEnabled()));
        plan.setCustomDomainEnabled(Boolean.TRUE.equals(request.getCustomDomainEnabled()));
        plan.setActive(true);
        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(UUID planId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", planId));
        plan.setActive(false);
        subscriptionPlanRepository.save(plan);
    }

    // ── Internal admin management ─────────────────────────────────────────────

    @Transactional
    public UserAdminDTO createAdmin(CreateAdminRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("EMAIL_TAKEN", "An account with this email already exists.");
        }
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setDeleted(false);
        admin.setEmailVerified(true); // internal accounts are pre-verified
        return toUserDto(userRepository.save(admin));
    }

    public List<UserAdminDTO> listAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> Boolean.FALSE.equals(u.getDeleted())
                        && (u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN))
                .map(this::toUserDto)
                .toList();
    }

    @Transactional
    public void removeAdmin(UUID adminId) {
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("NOT_AN_ADMIN", "Only ADMIN accounts can be removed via this endpoint.");
        }
        user.setDeleted(true);
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UserAdminDTO toUserDto(User u) {
        return UserAdminDTO.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole() == null ? null : u.getRole().name())
                .status(u.getStatus() == null ? null : u.getStatus().name())
                .emailVerified(u.getEmailVerified())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
