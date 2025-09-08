package com.notus.contabil.sistema_fiscal.config;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registra nosso interceptor para ser executado em todas as requisições.
        registry.addInterceptor(tenantInterceptor);
    }

   @Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // <-- permite CORS em todas as rotas
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
}
}