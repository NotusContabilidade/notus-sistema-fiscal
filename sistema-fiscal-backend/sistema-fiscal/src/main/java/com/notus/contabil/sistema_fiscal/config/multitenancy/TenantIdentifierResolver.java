package com.notus.contabil.sistema_fiscal.config.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    // Define um schema padrão. Este valor será usado como uma salvaguarda
    // para a conexão principal ou em cenários onde nenhum tenant foi especificado.
    private static final String DEFAULT_TENANT = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        // Tenta obter o tenant que foi definido pelo nosso TenantInterceptor.
        String tenantId = TenantContext.getCurrentTenant();

        // Se o tenantId foi definido (não é nulo nem vazio), usa ele.
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }

        // Se nenhum tenant foi definido no contexto, retorna o schema padrão.
        return DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}