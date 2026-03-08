package com.cartwave.startup;

import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(ApplicationArguments args) {
        seedPlans();
        seedSuperAdmin();
    }

    private void seedPlans() {
        createIfMissing("FREE", "Free tier with basic features", 20, 1, false, false, BigDecimal.ZERO);
        createIfMissing("STARTER", "Starter plan", 100, 3, true, false, BigDecimal.valueOf(19));
        createIfMissing("PRO", "Professional plan", 1000, 10, true, true, BigDecimal.valueOf(99));
        createIfMissing("ENTERPRISE", "Enterprise plan - unlimited products & staff", 0, 0, true, true, BigDecimal.valueOf(499));
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
        if (userRepository.findByEmail("superadmin@cartwave.local").isPresent()) {
            return;
        }
        User user = new User();
        user.setEmail("superadmin@cartwave.local");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRole(UserRole.SUPER_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setFirstName("System");
        user.setLastName("Admin");
        userRepository.save(user);
        log.info("Seeded default super admin: superadmin@cartwave.local / Password123!");
    }
}
