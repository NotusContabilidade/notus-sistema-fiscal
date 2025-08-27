package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/api/relatorios")
public class RelatorioController {
    
    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/calculo/{calculoId}/exportar/excel")
    public ResponseEntity<InputStreamResource> exportarCalculoExcel(@PathVariable Long calculoId) {
        try {
            ByteArrayInputStream in = relatorioService.gerarCalculoExcel(calculoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=relatorio_calculo_" + calculoId + ".xlsx");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            // Tratamento de erro
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calculo/{calculoId}/exportar/pdf")
    public ResponseEntity<InputStreamResource> exportarCalculoPdf(@PathVariable Long calculoId) {
        try {
            ByteArrayInputStream in = relatorioService.gerarCalculoPdf(calculoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=relatorio_calculo_" + calculoId + ".pdf");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            // Tratamento de erro
            return ResponseEntity.internalServerError().build();
        }
    }
}