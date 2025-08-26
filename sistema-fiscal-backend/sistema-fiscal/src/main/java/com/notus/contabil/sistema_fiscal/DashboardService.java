package com.notus.contabil.sistema_fiscal;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired; // Import necessário para conversão
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CalculoRepository calculoRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        LocalDate hoje = LocalDate.now();
        int mesAtual = hoje.getMonthValue();
        int anoAtual = hoje.getYear();

        long totalClientes = clienteRepository.count();
        Double totalDasNoMes = calculoRepository.sumDasTotalByAnoAndMes(anoAtual, mesAtual);
        long clientesPendentes = clienteRepository.countClientesSemCalculoNoMes(anoAtual, mesAtual);

        return new DashboardStatsDTO(totalClientes, totalDasNoMes, clientesPendentes);
    }

    // ✅ MÉTODO ATUALIZADO com conversão manual
    @Transactional(readOnly = true)
    public List<ClientePendenteDTO> getClientesPendentes() {
        LocalDate hoje = LocalDate.now();
        List<Object[]> resultados = clienteRepository.findClientesSemCalculoNoMes(hoje.getYear(), hoje.getMonthValue());
        
        // Mapeia a lista genérica para a lista de DTOs
        return resultados.stream().map(resultado -> new ClientePendenteDTO(
                ((Number) resultado[0]).longValue(), // ID
                (String) resultado[1]  // Razão Social
        )).collect(Collectors.toList());
    }

    // ✅ MÉTODO ATUALIZADO com conversão manual
    @Transactional(readOnly = true)
    public List<ClienteFinanceiroDTO> getFinanceiroDoMes() {
        LocalDate hoje = LocalDate.now();
        List<Object[]> resultados = calculoRepository.findClientesComCalculoNoMes(hoje.getYear(), hoje.getMonthValue());
        
        // Mapeia a lista genérica para a lista de DTOs
        return resultados.stream().map(resultado -> new ClienteFinanceiroDTO(
                ((Number) resultado[0]).longValue(), // ID
                (String) resultado[1],  // Razão Social
                (Double) resultado[2]   // DAS Total
        )).collect(Collectors.toList());
    }
}