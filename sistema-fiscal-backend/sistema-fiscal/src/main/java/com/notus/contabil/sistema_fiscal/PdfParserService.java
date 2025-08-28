package com.notus.contabil.sistema_fiscal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Service
public class PdfParserService {

    public ValoresExtraidosDTO extrairValoresDoLivroFiscal(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            
            // ✅ Usando as "lupas" (Regex) corretas para o texto exato que você enviou.
            BigDecimal comRetencao = extrairValor("TOTAL Serviços com Retenção No Município: ([\\d.,]+)", text);
            BigDecimal semRetencao = extrairValor("TOTAL Serviços sem Retenção No Município: ([\\d.,]+)", text);
            
            return new ValoresExtraidosDTO(comRetencao, semRetencao);
        }
    }

    private BigDecimal extrairValor(String regex, String text) {
        // Esta função auxiliar está perfeita e continua a mesma.
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String valorString = matcher.group(1);
            valorString = valorString.replace(".", "").replace(",", ".");
            return new BigDecimal(valorString);
        }
        
        return BigDecimal.ZERO;
    }
}