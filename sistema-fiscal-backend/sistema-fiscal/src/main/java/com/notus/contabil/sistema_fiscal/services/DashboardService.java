package com.notus.contabil.sistema_fiscal.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notus.contabil.sistema_fiscal.dto.ClienteFinanceiroDTO;
import com.notus.contabil.sistema_fiscal.dto.ClientePendenteDTO;
import com.notus.contabil.sistema_fiscal.dto.DashboardStatsDTO;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.repository.CalculoRepository;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;

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
    
    @Transactional(readOnly = true)
    public List<ClientePendenteDTO> getClientesPendentes() {
        LocalDate hoje = LocalDate.now();
        List<Cliente> resultados = clienteRepository.findClientesSemCalculoNoMes(hoje.getYear(), hoje.getMonthValue());
        
        return resultados.stream().map(cliente -> new ClientePendenteDTO(
                cliente.getId(),
                cliente.getRazaoSocial()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClienteFinanceiroDTO> getFinanceiroDoMes() {
        LocalDate hoje = LocalDate.now();
        // ✅ O CÓDIGO AGORA É CONSISTENTE COM O REPOSITÓRIO
        return calculoRepository.findClientesComCalculoNoMes(hoje.getYear(), hoje.getMonthValue());
    }
}