package com.notus.contabil.sistema_fiscal.config.multitenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    /**
     * Este método é executado ANTES de o Controller ser chamado.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Para requisições de registro/login, vamos pegar o tenantId do cabeçalho.
        // É mais simples e robusto do que ler o corpo da requisição aqui.
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }
        return true;
    }

    /**
     * Este método é executado DEPOIS que a requisição for completada.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Garante que o contexto seja limpo para a próxima requisição.
        TenantContext.clear();
    }
}