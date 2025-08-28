package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TarefaModeloService {

    @Autowired
    private TarefaModeloRepository tarefaModeloRepository;

    @Transactional(readOnly = true)
    public List<TarefaModeloDTO> listarTodos() {
        return tarefaModeloRepository.findAll()
                .stream()
                .map(TarefaModeloDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TarefaModeloDTO criar(TarefaModeloRequestDTO dto) {
        TarefaModelo novaTarefa = new TarefaModelo();
        novaTarefa.setTitulo(dto.titulo());
        novaTarefa.setDescricao(dto.descricao());
        novaTarefa.setDepartamento(dto.departamento());
        novaTarefa.setDiasParaCriacaoAntecipada(dto.diasParaCriacaoAntecipada());
        novaTarefa.setDiaVencimentoMes(dto.diaVencimentoMes());
        novaTarefa.setChecklist(dto.checklist());
        
        TarefaModelo tarefaSalva = tarefaModeloRepository.save(novaTarefa);
        return TarefaModeloDTO.fromEntity(tarefaSalva);
    }

    @Transactional
    public Optional<TarefaModeloDTO> atualizar(Long id, TarefaModeloRequestDTO dto) {
        return tarefaModeloRepository.findById(id)
            .map(tarefaExistente -> {
                tarefaExistente.setTitulo(dto.titulo());
                tarefaExistente.setDescricao(dto.descricao());
                tarefaExistente.setDepartamento(dto.departamento());
                tarefaExistente.setDiasParaCriacaoAntecipada(dto.diasParaCriacaoAntecipada());
                tarefaExistente.setDiaVencimentoMes(dto.diaVencimentoMes());
                tarefaExistente.setChecklist(dto.checklist());
                return TarefaModeloDTO.fromEntity(tarefaExistente);
            });
    }

    @Transactional
    public boolean deletar(Long id) {
        if (tarefaModeloRepository.existsById(id)) {
            tarefaModeloRepository.deleteById(id);
            return true;
        }
        return false;
    }
}