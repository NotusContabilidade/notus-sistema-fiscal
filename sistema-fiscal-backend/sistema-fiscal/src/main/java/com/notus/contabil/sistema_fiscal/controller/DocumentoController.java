package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.dto.DocumentoDTO;
import com.notus.contabil.sistema_fiscal.entity.Documento;
import com.notus.contabil.sistema_fiscal.entity.Cliente;
import com.notus.contabil.sistema_fiscal.services.DocumentoService;
import com.notus.contabil.sistema_fiscal.services.StorageService; // <-- NOVO IMPORT
import com.notus.contabil.sistema_fiscal.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI; // <-- NOVO IMPORT
import java.net.URL; // <-- NOVO IMPORT
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin // Habilita CORS para o frontend
public class DocumentoController {

    private final DocumentoService documentoService;
    private final StorageService storageService; // <-- INJETAR O STORAGE SERVICE
    private final ClienteRepository clienteRepository;

    @Autowired
    public DocumentoController(DocumentoService documentoService, StorageService storageService, ClienteRepository clienteRepository) { // <-- CONSTRUTOR ATUALIZADO
        this.documentoService = documentoService;
        this.storageService = storageService;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<DocumentoDTO> listarTodos() {
        return documentoService.listarTodos().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<DocumentoDTO> listarPorCliente(@PathVariable Long clienteId) {
        return documentoService.listarPorCliente(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DocumentoDTO> buscarPorId(@PathVariable Long id) {
        Optional<Documento> doc = documentoService.buscarPorId(id);
        return doc.map(documento -> ResponseEntity.ok(toDTO(documento)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * MÉTODO DOWNLOAD COMPLETAMENTE REFATORADO
     * Não envia mais os bytes do arquivo. Agora, gera uma URL segura e temporária
     * e redireciona o navegador do usuário para ela.
     * Isso é muito mais performático e escalável.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> downloadDocumento(@PathVariable Long id) {
        Documento doc = documentoService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        // 1. Pede ao StorageService para gerar uma URL de download pré-assinada
        URL url = storageService.gerarUrlParaDownload(doc.getStorageKey());
        
        // 2. Retorna uma resposta de redirecionamento (HTTP 302 Found)
        // O navegador do usuário irá seguir este link e baixar o arquivo diretamente do MinIO/S3.
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.toString()))
                .build();
    }

    /**
     * MÉTODO UPLOAD REFATORADO
     * A lógica principal agora está no DocumentoService. O controller apenas
     * orquestra a chamada, passando o MultipartFile para o serviço.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DocumentoDTO> uploadDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipoDocumento") String tipoDocumento,
            @RequestParam("usuarioUpload") String usuarioUpload,
            @RequestParam("clienteId") Long clienteId
    ) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            Documento documento = new Documento();
            documento.setNomeArquivo(file.getOriginalFilename());
            documento.setTipoDocumento(tipoDocumento);
            documento.setUsuarioUpload(usuarioUpload);
            documento.setCliente(cliente);
            
            // A mágica acontece aqui: passamos a entidade E o arquivo para o serviço.
            Documento salvo = documentoService.salvar(documento, file);
            return ResponseEntity.ok(toDTO(salvo));
        } catch (Exception e) {
            e.printStackTrace(); // É bom manter isso durante o desenvolvimento
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoDTO> aprovarDocumento(
            @PathVariable Long id,
            @RequestParam("usuarioAprovador") String usuarioAprovador,
            @RequestParam(value = "comentario", required = false) String comentario
    ) {
        try {
            Documento aprovado = documentoService.aprovarDocumento(id, usuarioAprovador, comentario);
            return ResponseEntity.ok(toDTO(aprovado));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoDTO> rejeitarDocumento(
            @PathVariable Long id,
            @RequestParam("usuarioAprovador") String usuarioAprovador,
            @RequestParam("comentario") String comentario
    ) {
        try {
            Documento rejeitado = documentoService.rejeitarDocumento(id, usuarioAprovador, comentario);
            return ResponseEntity.ok(toDTO(rejeitado));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        documentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private DocumentoDTO toDTO(Documento doc) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setId(doc.getId());
        dto.setNomeArquivo(doc.getNomeArquivo());
        dto.setTipoDocumento(doc.getTipoDocumento());
        dto.setStatus(doc.getStatus());
        dto.setComentario(doc.getComentario());
        dto.setDataUpload(doc.getDataUpload());
        dto.setDataAprovacao(doc.getDataAprovacao());
        dto.setUsuarioUpload(doc.getUsuarioUpload());
        dto.setUsuarioAprovador(doc.getUsuarioAprovador());
        dto.setClienteId(doc.getCliente() != null ? doc.getCliente().getId() : null);
        // Não expomos a storageKey no DTO, é um detalhe de implementação interna
        return dto;
    }
}