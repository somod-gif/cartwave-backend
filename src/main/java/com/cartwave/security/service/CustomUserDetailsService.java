package com.cartwave.security.service;

import com.cartwave.customer.repository.CustomerRepository;
import com.cartwave.security.model.CurrentUserPrincipal;
import com.cartwave.staff.entity.Staff;
import com.cartwave.staff.repository.StaffRepository;
import com.cartwave.store.repository.StoreRepository;
import com.cartwave.user.entity.User;
import com.cartwave.user.entity.UserRole;
import com.cartwave.user.entity.UserStatus;
import com.cartwave.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            StaffRepository staffRepository,
            CustomerRepository customerRepository,
            StoreRepository storeRepository
    ) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findUser(email);
        return toPrincipal(user, null);
    }

    public CurrentUserPrincipal loadUserByUsernameAndStoreId(String email, UUID storeId) {
        User user = findUser(email);
        return toPrincipal(user, storeId);
    }

    public List<UUID> getAccessibleStoreIds(User user) {
        Set<UUID> storeIds = new LinkedHashSet<>();
        storeRepository.findAllByOwnerId(user.getId()).forEach(store -> storeIds.add(store.getId()));
        storeIds.addAll(staffRepository.findStoreIdsByUserId(user.getId()));
        storeIds.addAll(customerRepository.findStoreIdsByUserId(user.getId()));
        return new ArrayList<>(storeIds);
    }

    private CurrentUserPrincipal toPrincipal(User user, UUID storeId) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        if (storeId != null) {
            if (storeRepository.findByOwnerId(user.getId()).map(store -> store.getId().equals(storeId)).orElse(false)) {
                authorities.add(new SimpleGrantedAuthority("STORE_OWNER"));
            }

            staffRepository.findByUserIdAndStoreId(user.getId(), storeId)
                    .filter(staff -> staff.getStatus() == com.cartwave.staff.entity.StaffStatus.ACTIVE)
                    .ifPresent(staff -> addStaffAuthorities(authorities, staff));

            customerRepository.findByUserIdAndStoreId(user.getId(), storeId)
                    .ifPresent(customer -> authorities.add(new SimpleGrantedAuthority("STORE_CUSTOMER")));
        }

        return new CurrentUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                storeId,
                user.getStatus() == UserStatus.ACTIVE,
                authorities
        );
    }

    private void addStaffAuthorities(Set<GrantedAuthority> authorities, Staff staff) {
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        authorities.add(new SimpleGrantedAuthority("STAFF_" + staff.getRole().name()));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
