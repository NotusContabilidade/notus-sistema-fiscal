package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.entity.TarefaRecorrente;
import com.notus.contabil.sistema_fiscal.repository.TarefaRecorrenteRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AgendadorTarefasService {

    @Autowired private TarefaRecorrenteRepository recorrenteRepo;
    @Autowired private TaskRepository taskRepo;
    @Autowired private TaskService taskService;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void criarTarefasRecorrentes() {
        System.out.println("ROTINA AGENDADA: Iniciando verificação de tarefas recorrentes...");
        LocalDate hoje = LocalDate.now();
        List<TarefaRecorrente> recorrentesAtivas = recorrenteRepo.findAllByAtivaTrue();

        for (TarefaRecorrente molde : recorrentesAtivas) {
            if (molde.getFrequencia() == TarefaRecorrente.Frequencia.MENSAL) {
                if (hoje.getDayOfMonth() == 1) {
                    LocalDate prazo = hoje.withDayOfMonth(molde.getDiaVencimento());
                    String tituloDoMes = molde.getTitulo() + " - " + prazo.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                    
                    boolean jaExiste = taskRepo.existsByClienteIdAndTitulo(
                        molde.getCliente().getId(),
                        tituloDoMes
                    );

                    if (!jaExiste) {
                        criarTarefaDoMolde(molde, prazo, tituloDoMes);
                    }
                }
            }
        }
        System.out.println("ROTINA AGENDADA: Verificação de tarefas recorrentes concluída.");
    }

    private void criarTarefaDoMolde(TarefaRecorrente molde, LocalDate prazo, String titulo) {
        TaskCreateDTO novaTarefaDto = new TaskCreateDTO();
        novaTarefaDto.setClienteId(molde.getCliente().getId());
        novaTarefaDto.setTitulo(titulo);
        novaTarefaDto.setDescricao(molde.getDescricao());
        novaTarefaDto.setCategoria(molde.getCategoria());
        novaTarefaDto.setResponsavel(molde.getResponsavel());
        novaTarefaDto.setPrazo(prazo);
        novaTarefaDto.setStatus("PENDENTE");

        taskService.criar(novaTarefaDto);
        System.out.println("Criada tarefa recorrente: '" + titulo + "' para cliente " + molde.getCliente().getRazaoSocial());
    }
}