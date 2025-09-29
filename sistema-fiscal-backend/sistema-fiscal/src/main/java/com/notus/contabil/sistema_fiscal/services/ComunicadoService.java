package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.dto.ComunicadoDTO;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.Comunicado;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.ComunicadoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComunicadoService {

    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ComunicadoDTO> findByClienteId(Long clienteId) {
        List<Comunicado> comunicados = comunicadoRepository.findByClienteIdOrClienteIdIsNullOrderByDataCriacaoDesc(clienteId);
        return comunicados.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComunicadoDTO> findRecentes() {
        return comunicadoRepository.findTop5ByOrderByDataCriacaoDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComunicadoDTO createForCliente(Long clienteId, ComunicadoDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + clienteId));
        Comunicado comunicado = new Comunicado();
        comunicado.setTitulo(dto.getTitulo());
        comunicado.setMensagem(dto.getMensagem());
        comunicado.setCliente(cliente);
        Comunicado saved = comunicadoRepository.save(comunicado);
        return convertToDto(saved);
    }

    @Transactional
    public void createBroadcast(ComunicadoDTO dto) {
        Comunicado comunicado = new Comunicado();
        comunicado.setTitulo(dto.getTitulo());
        comunicado.setMensagem(dto.getMensagem());
        comunicado.setCliente(null); // Nulo indica que é para todos
        comunicadoRepository.save(comunicado);
    }

    private ComunicadoDTO convertToDto(Comunicado comunicado) {
        return new ComunicadoDTO(
                comunicado.getId(),
                comunicado.getTitulo(),
                comunicado.getMensagem(),
                comunicado.getDataCriacao(),
                comunicado.getCliente() != null ? comunicado.getCliente().getId() : null,
                comunicado.getCliente() != null ? comunicado.getCliente().getRazaoSocial() : "TODOS OS CLIENTES"
        );
    }
}