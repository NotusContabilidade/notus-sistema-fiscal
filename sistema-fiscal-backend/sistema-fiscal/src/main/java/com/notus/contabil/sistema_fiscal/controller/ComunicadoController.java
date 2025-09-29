package com.notus.contabil.sistema_fiscal.controller;

import com.notus.contabil.sistema_fiscal.dto.ComunicadoDTO;
import com.notus.contabil.sistema_fiscal.services.ComunicadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comunicados")
public class ComunicadoController {

    @Autowired
    private ComunicadoService comunicadoService;

    @GetMapping("/por-cliente/{clienteId}")
    public ResponseEntity<List<ComunicadoDTO>> getComunicadosByClienteId(@PathVariable Long clienteId) {
        List<ComunicadoDTO> comunicados = comunicadoService.findByClienteId(clienteId);
        return ResponseEntity.ok(comunicados);
    }

    @GetMapping("/recentes")
    public ResponseEntity<List<ComunicadoDTO>> getComunicadosRecentes() {
        List<ComunicadoDTO> recentes = comunicadoService.findRecentes();
        return ResponseEntity.ok(recentes);
    }

    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<ComunicadoDTO> createComunicadoParaCliente(@PathVariable Long clienteId, @RequestBody ComunicadoDTO comunicadoDTO) {
        ComunicadoDTO novoComunicado = comunicadoService.createForCliente(clienteId, comunicadoDTO);
        return ResponseEntity.ok(novoComunicado);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> createComunicadoBroadcast(@RequestBody ComunicadoDTO comunicadoDTO) {
        comunicadoService.createBroadcast(comunicadoDTO);
        return ResponseEntity.noContent().build();
    }
}