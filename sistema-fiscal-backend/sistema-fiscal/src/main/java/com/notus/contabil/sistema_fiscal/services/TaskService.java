package com.notus.contabil.sistema_fiscal.services;

import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.notus.contabil.sistema_fiscal.dto.ComentarioDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.dto.TaskDTO;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.entity.Comentario;
import com.notus.contabil.sistema_fiscal.entity.Task;
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;
import com.notus.contabil.sistema_fiscal.repository.ComentarioRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private MinIOStorageService storageService; // Injeção do serviço de storage

    public List<TaskDTO> listar() {
        return taskRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO criar(TaskCreateDTO dto) {
        Task task = new Task();
        task.setTitulo(dto.getTitulo());
        task.setDescricao(dto.getDescricao());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDENTE");
        task.setPrazo(dto.getPrazo());
        task.setResponsavel(dto.getResponsavel());
        task.setDataCriacao(LocalDateTime.now());
        task.setAnexos(new ArrayList<>()); // Inicia com lista vazia
        task.setCategoria(dto.getCategoria());

        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            task.setCliente(cliente);
        }
        task.setHistorico(new ArrayList<>());
        
        return toDTO(taskRepository.save(task));
    }

    public TaskDTO atualizar(Long id, TaskCreateDTO dto) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task não encontrada"));
        
        task.setTitulo(dto.getTitulo());
        task.setDescricao(dto.getDescricao());
        task.setStatus(dto.getStatus());
        task.setPrazo(dto.getPrazo());
        task.setResponsavel(dto.getResponsavel());
        task.setAnexos(dto.getAnexos());
        task.setCategoria(dto.getCategoria());

        if (dto.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            task.setCliente(cliente);
        }
        
        return toDTO(taskRepository.save(task));
    }

    public void deletar(Long id) {
        taskRepository.deleteById(id);
    }

    public List<TaskDTO> listarMinhasTarefas(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com email: " + email));
        
        return taskRepository.findByClienteId(cliente.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> listarTarefasPorClienteId(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente não encontrado com ID: " + clienteId);
        }
        
        return taskRepository.findByClienteId(clienteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada com ID: " + id));
        return toDTO(task);
    }

    /**
     * Adiciona um anexo a uma tarefa, salvando-o em uma pasta organizada no MinIO.
     */
    @Transactional
    public TaskDTO adicionarAnexo(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada com o ID: " + taskId));

        Cliente cliente = task.getCliente();
        if (cliente == null) {
            throw new IllegalStateException("A tarefa precisa estar associada a um cliente para adicionar anexos.");
        }

        // 1. Monta o prefixo do caminho (a "pasta")
        String nomePastaCliente = sanitizarNome(cliente.getRazaoSocial());
        String anoMes = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String pathPrefix = String.format("%s/%s/", nomePastaCliente, anoMes);

        // 2. Salva o arquivo usando o serviço de storage, que retorna a chave de armazenamento
        String storageKey = storageService.salvar(file, pathPrefix);

        // 3. Gera uma URL de download para a chave e adiciona à lista de anexos da tarefa
        URL downloadUrl = storageService.gerarUrlParaDownload(storageKey);
        
        List<String> anexos = task.getAnexos();
        if (anexos == null) {
            anexos = new ArrayList<>();
        }
        anexos.add(downloadUrl.toString());
        task.setAnexos(anexos);

        Task updatedTask = taskRepository.save(task);
        return toDTO(updatedTask);
    }

    /**
     * Remove acentos e caracteres especiais para criar um nome de pasta seguro.
     */
    private String sanitizarNome(String nome) {
        if (nome == null || nome.isBlank()) return "sem-cliente";
        
        String nomeNormalizado = Normalizer.normalize(nome, Normalizer.Form.NFD);
        nomeNormalizado = nomeNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        
        return nomeNormalizado
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-") // Substitui espaços por hífens
                .replaceAll("[^a-z0-9-]", ""); // Remove caracteres inválidos
    }

    public List<ComentarioDTO> getComentariosByTaskId(Long taskId) {
        return comentarioRepository.findByTaskIdOrderByDataCriacaoAsc(taskId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioDTO adicionarComentario(Long taskId, String autor, String texto) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada para adicionar comentário"));
        
        Comentario comentario = new Comentario();
        comentario.setTask(task);
        comentario.setAutor(autor);
        comentario.setTexto(texto);
        
        String historicoMsg = String.format("Comentário adicionado por '%s' em %s.",
            autor,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        task.getHistorico().add(historicoMsg);

        Comentario comentarioSalvo = comentarioRepository.save(comentario);
        return toDTO(comentarioSalvo);
    }

    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitulo(task.getTitulo());
        dto.setDescricao(task.getDescricao());
        dto.setStatus(task.getStatus());
        dto.setPrazo(task.getPrazo());
        dto.setResponsavel(task.getResponsavel());
        dto.setCategoria(task.getCategoria());
        dto.setDataCriacao(task.getDataCriacao());
        dto.setDataConclusao(task.getDataConclusao());
        dto.setAnexos(task.getAnexos());
        dto.setHistorico(task.getHistorico());
        if (task.getCliente() != null) {
            dto.setClienteId(task.getCliente().getId());
        }
        return dto;
    }

    private ComentarioDTO toDTO(Comentario comentario) {
        return new ComentarioDTO(
            comentario.getId(),
            comentario.getTask().getId(),
            comentario.getAutor(),
            comentario.getTexto(),
            comentario.getDataCriacao()
        );
    }
}