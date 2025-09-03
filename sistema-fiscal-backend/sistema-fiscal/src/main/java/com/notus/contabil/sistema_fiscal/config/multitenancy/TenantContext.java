package com.notus.contabil.sistema_fiscal.config.multitenancy;

public final class TenantContext {

    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenant) {
        if (tenant == null) {
            clear();
        } else {
            tenantId.set(tenant);
        }
    }

    public static String getTenantId() {
        return tenantId.get();
    }

    public static void clear() {
        tenantId.remove();
    }
}
