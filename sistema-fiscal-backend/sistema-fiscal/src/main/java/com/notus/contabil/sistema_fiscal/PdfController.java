package com.notus.contabil.sistema_fiscal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfParserService pdfParserService;

    @PostMapping("/upload/livro-fiscal-bauru")
    // ✅ O tipo de retorno volta a ser '?', mas o corpo de sucesso será o ValoresExtraidosDTO
    public ResponseEntity<?> uploadLivroFiscal(@RequestParam("file") MultipartFile file) {
        try {
            // ✅ A chamada agora retorna o DTO simples novamente.
            ValoresExtraidosDTO valores = pdfParserService.extrairValoresDoLivroFiscal(file);
            return ResponseEntity.ok(valores);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao processar o arquivo PDF. Verifique o arquivo e tente novamente."));
        }
    }
}