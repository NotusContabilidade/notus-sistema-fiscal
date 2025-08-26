package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List; // Adicione este import

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    // ✅ NOVO ENDPOINT
    @GetMapping("/clientes-pendentes")
    public ResponseEntity<List<ClientePendenteDTO>> getClientesPendentes() {
        return ResponseEntity.ok(dashboardService.getClientesPendentes());
    }

    // ✅ NOVO ENDPOINT
    @GetMapping("/financeiro-mes")
    public ResponseEntity<List<ClienteFinanceiroDTO>> getFinanceiroDoMes() {
        return ResponseEntity.ok(dashboardService.getFinanceiroDoMes());
    }
}