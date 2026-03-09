package com.cartwave.startup;

import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDataLoader implements ApplicationRunner {

    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${cartwave.superadmin.email:superadmin@cartwave.local}")
    private String superAdminEmail;

    @Value("${cartwave.superadmin.password:Password123!}")
    private String superAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        seedPlans();
        seedSuperAdmin();
    }

    private void seedPlans() {
        // Naira pricing as per spec
        createIfMissing("STARTER",    "Starter (free)",                   5,   1, false, false, BigDecimal.ZERO);
        createIfMissing("BASIC",      "Basic plan — up to 10 products",   10,  3, true,  false, BigDecimal.valueOf(5000));
        createIfMissing("GROWTH",     "Growth plan — up to 20 products",  20,  5, true,  false, BigDecimal.valueOf(15000));
        createIfMissing("PRO",        "Pro plan — up to 100 products",    100, 10, true, true,  BigDecimal.valueOf(30000));
        createIfMissing("ENTERPRISE", "Enterprise — unlimited products",  0,   0, true,  true,  BigDecimal.ZERO);
        // Keep legacy FREE plan for backward compat
        createIfMissing("FREE",       "Legacy free tier",                 20,  1, false, false, BigDecimal.ZERO);
    }

    private void createIfMissing(String name, String desc, int productLimit, int staffLimit, boolean payments, boolean customDomain, BigDecimal price) {
        Optional<SubscriptionPlan> existing = planRepository.findByName(name);
        if (existing.isPresent()) {
            return;
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .description(desc)
                .productLimit(productLimit)
                .staffLimit(staffLimit)
                .paymentsEnabled(payments)
                .customDomainEnabled(customDomain)
                .price(price)
                .active(true)
                .build();
        planRepository.save(plan);
        log.info("Seeded subscription plan: {}", name);
    }

    private void seedSuperAdmin() {
        if (userRepository.findByEmail(superAdminEmail).isPresent()) {
            return;
        }
        User user = new User();
        user.setEmail(superAdminEmail);
        user.setPassword(passwordEncoder.encode(superAdminPassword));
        user.setRole(UserRole.SUPER_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName("System");
        user.setLastName("Admin");
        userRepository.save(user);
        log.info("Seeded default super admin: {}", superAdminEmail);
    }
}
