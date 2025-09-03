package com.notus.contabil.sistema_fiscal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.notus.contabil.sistema_fiscal.services.RelatorioService;

@Controller
@RequestMapping("/api/relatorios")
public class RelatorioController {
    
    @Autowired
    private RelatorioService relatorioService;

    // Não precisamos mais do CalculoRepository aqui

    @GetMapping("/calculo/{calculoId}/exportar/excel")
    public ResponseEntity<InputStreamResource> exportarCalculoExcel(@PathVariable Long calculoId) {
        try {
            // 1. Chama o serviço, que agora retorna o nome e o conteúdo
            RelatorioService.ArquivoExportado arquivo = relatorioService.gerarCalculoExcel(calculoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + arquivo.nomeArquivo() + "\"");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(arquivo.stream()));
        } catch (Exception e) {
            e.printStackTrace(); // É bom ter isso para depuração
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calculo/{calculoId}/exportar/pdf")
    public ResponseEntity<InputStreamResource> exportarCalculoPdf(@PathVariable Long calculoId) {
        try {
            // 2. Chama o serviço, que agora retorna o nome e o conteúdo
            RelatorioService.ArquivoExportado arquivo = relatorioService.gerarCalculoPdf(calculoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"" + arquivo.nomeArquivo() + "\"");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(arquivo.stream()));
        } catch (Exception e) {
            e.printStackTrace(); // É bom ter isso para depuração
            return ResponseEntity.internalServerError().build();
        }
    }
}