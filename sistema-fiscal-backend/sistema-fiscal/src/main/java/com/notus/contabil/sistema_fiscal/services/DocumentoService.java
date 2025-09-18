package com.notus.contabil.sistema_fiscal.services;

import com.notus.contabil.sistema_fiscal.entity.Documento;
import com.notus.contabil.sistema_fiscal.repository.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;

    @Autowired
    public DocumentoService(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
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

    public Documento salvar(Documento documento) {
        documento.setDataUpload(LocalDateTime.now());
        documento.setStatus("PENDENTE");
        return documentoRepository.save(documento);
    }

    public void deletar(Long id) {
        documentoRepository.deleteById(id);
    }

    public Documento aprovarDocumento(Long id, String usuarioAprovador, String comentario) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus("APROVADO");
        doc.setUsuarioAprovador(usuarioAprovador);
        doc.setComentario(comentario);
        doc.setDataAprovacao(LocalDateTime.now());
        return documentoRepository.save(doc);
    }

    public Documento rejeitarDocumento(Long id, String usuarioAprovador, String comentario) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));
        doc.setStatus("REJEITADO");
        doc.setUsuarioAprovador(usuarioAprovador);
        doc.setComentario(comentario);
        doc.setDataAprovacao(LocalDateTime.now());
        return documentoRepository.save(doc);
    }
}