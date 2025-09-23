package com.notus.contabil.sistema_fiscal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Import necessário
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import necessário
import org.springframework.web.multipart.MultipartFile;

import com.notus.contabil.sistema_fiscal.config.multitenancy.TenantContext;
import com.notus.contabil.sistema_fiscal.dto.TaskCreateDTO;
import com.notus.contabil.sistema_fiscal.entity.Documento;
import com.notus.contabil.sistema_fiscal.repository.DocumentoRepository;
import com.notus.contabil.sistema_fiscal.repository.TaskRepository;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final StorageService storageService;
    private final TaskService taskService;
    private final TaskRepository taskRepository; // Injeção do TaskRepository

    @Autowired
    public DocumentoService(DocumentoRepository documentoRepository, StorageService storageService, TaskService taskService, TaskRepository taskRepository) { // Construtor atualizado
        this.documentoRepository = documentoRepository;
        this.storageService = storageService;
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
    }

    public List<Documento> listarPorCliente(Long clienteId) {
        return documentoRepository.findByClienteId(clienteId);
    }

    public Optional<Documento> buscarPorId(Long id) {
        return documentoRepository.findById(id);
    }

    @Transactional
    public Documento salvar(Documento documento, MultipartFile file) {
        String pathPrefix = TenantContext.getTenantId() + "/" + documento.getCliente().getId() + "/";
        String storageKey = storageService.salvar(file, pathPrefix);
        
        documento.setStorageKey(storageKey);
        documento.setDataUpload(LocalDateTime.now());
        documento.setStatus("PENDENTE");

        Documento documentoSalvo = documentoRepository.save(documento);

        criarTarefaDeRevisaoParaDocumento(documentoSalvo);

        return documentoSalvo;
    }

    private void criarTarefaDeRevisaoParaDocumento(Documento documento) {
        TaskCreateDTO novaTarefa = new TaskCreateDTO();
        novaTarefa.setClienteId(documento.getCliente().getId());
        novaTarefa.setTitulo("Revisar Documento: " + documento.getNomeArquivo());
        novaTarefa.setDescricao("Um novo documento foi enviado e precisa de revisão. Tipo: " + documento.getTipoDocumento());
        novaTarefa.setCategoria("DOCUMENTO_CLIENTE");
        novaTarefa.setStatus("PENDENTE");
        
        taskService.criar(novaTarefa);
    }

    @Transactional
    public void deletar(Long id) {
        Documento doc = documentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado para deletar"));

        storageService.deletar(doc.getStorageKey());
        
        documentoRepository.deleteById(id);
    }

    @Transactional
    public Documento aprovarDocumento(Long id, String usuarioAprovador, String comentario) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus("APROVADO");
        doc.setUsuarioAprovador(usuarioAprovador);
        doc.setComentario(comentario);
        doc.setDataAprovacao(LocalDateTime.now());
        
        concluirTarefaDeRevisao(doc);

        return documentoRepository.save(doc);
    }

    @Transactional
    public Documento rejeitarDocumento(Long id, String usuarioAprovador, String comentario) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus("REJEITADO");
        doc.setUsuarioAprovador(usuarioAprovador);
        doc.setComentario(comentario);
        doc.setDataAprovacao(LocalDateTime.now());

        concluirTarefaDeRevisao(doc);

        return documentoRepository.save(doc);
    }

    private void concluirTarefaDeRevisao(Documento documento) {
        String tituloTarefa = "Revisar Documento: " + documento.getNomeArquivo();
        taskRepository.findByClienteIdAndTitulo(documento.getCliente().getId(), tituloTarefa)
            .ifPresent(task -> {
                task.setStatus("CONCLUIDO");
                task.setDataConclusao(LocalDateTime.now());
                taskRepository.save(task);
            });
    }
}