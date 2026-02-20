package com.cartwave.tenant;

import com.cartwave.exception.TenantAccessDeniedException;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class TenantContext {

    private static final ThreadLocal<UUID> tenantId = new ThreadLocal<>();

    public static void setTenantId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        tenantId.set(id);
        log.debug("Tenant context set to: {}", id);
    }

    public static UUID getTenantId() {
        UUID id = tenantId.get();
        if (id == null) {
            throw new TenantAccessDeniedException("No tenant context found. Request must include valid store ID in JWT.");
        }
        return id;
    }

    public static void clear() {
        tenantId.remove();
        log.debug("Tenant context cleared");
    }

    public static boolean isSet() {
        return tenantId.get() != null;
    }

}
