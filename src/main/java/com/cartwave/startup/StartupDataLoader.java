package com.cartwave.startup;

import com.cartwave.store.entity.Store;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.subscription.entity.Subscription;
import com.cartwave.subscription.entity.SubscriptionPlan;
import com.cartwave.subscription.repository.SubscriptionPlanRepository;
import com.cartwave.subscription.repository.SubscriptionRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import com.cartwave.staff.entity.Staff;
import com.cartwave.staff.entity.StaffRole;
import com.cartwave.staff.entity.StaffStatus;
import com.cartwave.staff.repository.StaffRepository;
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
    private final StoreRepository storeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffRepository staffRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedPlans();
        seedDefaultStoreAndSubscription();
        seedDefaultUserAndStaff();
    }

    private void seedPlans() {
        createIfMissing("FREE", "Free tier with basic features", 20, 1, false, false, BigDecimal.ZERO);
        createIfMissing("STARTER", "Starter plan", 100, 3, true, false, BigDecimal.valueOf(19));
        createIfMissing("PRO", "Professional plan", 1000, 10, true, true, BigDecimal.valueOf(99));
        createIfMissing("ENTERPRISE", "Enterprise plan - unlimited products & staff", 0, 0, true, true, BigDecimal.valueOf(499));
    }

    private void createIfMissing(String name, String desc, int productLimit, int staffLimit, boolean payments, boolean customDomain, BigDecimal price) {
        Optional<SubscriptionPlan> existing = planRepository.findByName(name);
        if (existing.isPresent()) return;

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(name)
                .description(desc)
                .productLimit(productLimit)
                .staffLimit(staffLimit)
                .paymentsEnabled(payments)
                .customDomainEnabled(customDomain)
                .price(price)
                .build();
        planRepository.save(plan);
        log.info("Seeded subscription plan: {}", name);
    }

    private void seedDefaultStoreAndSubscription() {
        // create a store if none exists so testers can use a store id
        if (storeRepository.count() == 0) {
            Store store = new Store();
            store.setName("Default Store");
            store.setSlug("default-store");
            store.setDeleted(false);
            storeRepository.save(store);
            log.info("Created default store with id={}", store.getId());

            Subscription sub = new Subscription();
            sub.setStoreId(store.getId());
            sub.setPlanName("FREE");
            sub.setStatus(com.cartwave.subscription.entity.SubscriptionStatus.ACTIVE);
            sub.setAmount(BigDecimal.ZERO);
            sub.setAutoRenewal(true);
            subscriptionRepository.save(sub);
            log.info("Created default FREE subscription for store={}", store.getId());
        }
    }

    private void seedDefaultUserAndStaff() {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setEmail("admin@cartwave.local");
            user.setPassword(passwordEncoder.encode("Password123!"));
            user.setRole(UserRole.BUSINESS_OWNER);
            user.setStatus(UserStatus.ACTIVE);
            user.setDeleted(false);
            userRepository.save(user);
            log.info("Created default user: {} / Password: Password123!", user.getEmail());

            // Attach to first store as staff
            Optional<Store> storeOpt = storeRepository.findAll().stream().findFirst();
            if (storeOpt.isPresent()) {
                Staff staff = new Staff();
                staff.setUserId(user.getId());
                staff.setStoreId(storeOpt.get().getId());
                staff.setRole(StaffRole.MANAGER);
                staff.setStatus(StaffStatus.ACTIVE);
                staff.setDeleted(false);
                staffRepository.save(staff);
                log.info("Created staff entry for user={} in store={}", user.getEmail(), storeOpt.get().getId());
            }
        }
    }
}
