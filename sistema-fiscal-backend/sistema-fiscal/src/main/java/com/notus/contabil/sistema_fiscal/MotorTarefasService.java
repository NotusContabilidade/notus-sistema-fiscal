package com.notus.contabil.sistema_fiscal;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MotorTarefasService {

    private static final Logger logger = LoggerFactory.getLogger(MotorTarefasService.class);

    @Autowired
    private EscritorioRepository escritorioRepository;
    
    @Autowired
    private TarefaModeloRepository tarefaModeloRepository;
    
    @Autowired
    private TarefaRealRepository tarefaRealRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // A anotação @Scheduled executa este método automaticamente.
    // A expressão "cron" significa: "às 2 da manhã, todos os dias".
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void gerarTarefasRecorrentes() {
        logger.info("INICIANDO ROTINA DE GERAÇÃO DE TAREFAS RECORRENTES...");

        // 1. Busca todos os escritórios (tenants) cadastrados
        List<Escritorio> todosOsEscritorios = escritorioRepository.findAll();

        for (Escritorio escritorio : todosOsEscritorios) {
            try {
                // 2. Define o contexto para o escritório atual
                //    Toda operação de banco de dados a partir daqui será executada no schema deste escritório
                TenantContext.setCurrentTenant(escritorio.getSchemaName());
                logger.info("Processando para o tenant: {}", escritorio.getSchemaName());

                // 3. Busca os modelos de tarefa e clientes DESTE ESCRITÓRIO
                List<TarefaModelo> modelosDoTenant = tarefaModeloRepository.findAll();
                List<Cliente> clientesDoTenant = clienteRepository.findAll();

                if (modelosDoTenant.isEmpty() || clientesDoTenant.isEmpty()) {
                    logger.info("Nenhum modelo de tarefa ou cliente encontrado para {}. Pulando.", escritorio.getSchemaName());
                    continue; // Pula para o próximo escritório
                }

                // 4. Lógica de geração
                for (TarefaModelo modelo : modelosDoTenant) {
                    LocalDate hoje = LocalDate.now();
                    LocalDate dataDeCriacao = hoje.plusDays(modelo.getDiasParaCriacaoAntecipada());
                    
                    if (dataDeCriacao.getDayOfMonth() == modelo.getDiaVencimentoMes()) {
                        logger.info("Modelo '{}' corresponde à data de hoje. Gerando tarefas...", modelo.getTitulo());

                        for (Cliente cliente : clientesDoTenant) {
                            gerarTarefaParaCliente(modelo, cliente, dataDeCriacao);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ERRO ao processar o tenant {}: {}", escritorio.getSchemaName(), e.getMessage());
            } finally {
                // 5. Limpa o contexto para garantir o isolamento
                TenantContext.clear();
            }
        }
        logger.info("ROTINA DE GERAÇÃO DE TAREFAS CONCLUÍDA.");
    }

    // Método auxiliar para criar uma tarefa individual
    private void gerarTarefaParaCliente(TarefaModelo modelo, Cliente cliente, LocalDate dataVencimento) {
        // Verifica se a tarefa já não existe, para evitar duplicatas
        if (!tarefaRealRepository.existsByTarefaModeloAndClienteAndDataVencimento(modelo, cliente, dataVencimento)) {
            
            TarefaReal novaTarefa = new TarefaReal();
            novaTarefa.setTarefaModelo(modelo);
            novaTarefa.setCliente(cliente);
            novaTarefa.setTitulo(modelo.getTitulo());
            novaTarefa.setDataVencimento(dataVencimento);
            novaTarefa.setStatus(TarefaReal.StatusTarefa.PENDENTE);
            // O responsável será atribuído manualmente pelo gestor do escritório, por enquanto.

            tarefaRealRepository.save(novaTarefa);
            logger.info("Tarefa '{}' criada para o cliente '{}' com vencimento em {}.",
                    modelo.getTitulo(), cliente.getRazaoSocial(), dataVencimento);
        }
    }
}