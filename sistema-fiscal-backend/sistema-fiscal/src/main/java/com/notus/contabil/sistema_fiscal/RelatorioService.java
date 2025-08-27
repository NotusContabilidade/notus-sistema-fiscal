package com.notus.contabil.sistema_fiscal;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class RelatorioService {

    @Autowired
    private CalculoRepository calculoRepository;

    // ✅ MÉTODO DO EXCEL COMPLETO E CORRIGIDO
    @Transactional(readOnly = true)
    public ByteArrayInputStream gerarCalculoExcel(Long calculoId) throws IOException {
        Calculo calculo = calculoRepository.findById(calculoId)
                .orElseThrow(() -> new RuntimeException("Cálculo não encontrado"));

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Relatório de Apuração");

            // Estilos
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            int rowNum = 0;
            // Cabeçalho do Relatório
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("Relatório de Apuração - " + calculo.getCliente().getRazaoSocial());
            
            sheet.createRow(rowNum++); // Linha em branco

            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Período de Apuração:");
            infoRow.createCell(1).setCellValue(String.format("%02d/%d", calculo.getMesReferencia(), calculo.getAnoReferencia()));
            infoRow.createCell(3).setCellValue("Valor Total do DAS:");
            infoRow.createCell(4).setCellValue(formatCurrency(calculo.getDasTotal()));

            sheet.createRow(rowNum++); // Linha em branco

            // Tabela de Detalhes
            Row tableHeaderRow = sheet.createRow(rowNum++);
            String[] headers = { "Descrição", "Receita (R$)", "Valor do DAS (R$)" };
            for(int i=0; i < headers.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (ResultadoCalculoDetalhado detalhe : calculo.getDetalhes()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Anexo " + detalhe.anexoAplicado());
                
                Row rowNormal = sheet.createRow(rowNum++);
                rowNormal.createCell(0).setCellValue("  Receita Normal");
                rowNormal.createCell(1).setCellValue(detalhe.rpaNormal());
                rowNormal.createCell(2).setCellValue(detalhe.dasNormal());

                if (detalhe.rpaComRetencao() > 0) {
                     Row rowRetencao = sheet.createRow(rowNum++);
                     rowRetencao.createCell(0).setCellValue("  Receita c/ Retenção ISS");
                     rowRetencao.createCell(1).setCellValue(detalhe.rpaComRetencao());
                     rowRetencao.createCell(2).setCellValue(detalhe.dasComRetencaoLiquido());
                }

                Row subtotalRow = sheet.createRow(rowNum++);
                subtotalRow.createCell(0).setCellValue("Subtotal do Anexo");
                subtotalRow.createCell(1).setCellValue(detalhe.rpaTotal());
                subtotalRow.createCell(2).setCellValue(detalhe.dasTotal());
            }

            for(int i=0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // MÉTODO DO PDF (JÁ ESTAVA CORRETO)
    @Transactional(readOnly = true)
    public ByteArrayInputStream gerarCalculoPdf(Long calculoId) throws Exception {
         Calculo calculo = calculoRepository.findById(calculoId)
                .orElseThrow(() -> new RuntimeException("Cálculo não encontrado"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        document.add(new Paragraph("Relatório de Apuração do Simples Nacional", fontTitle));
        document.add(new Paragraph(" ")); 
        Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 10);
        document.add(new Paragraph("Cliente: " + calculo.getCliente().getRazaoSocial(), fontBody));
        document.add(new Paragraph("CNPJ: " + formatCnpj(calculo.getCliente().getCnpj()), fontBody));
        document.add(new Paragraph("Período de Apuração: " + String.format("%02d/%d", calculo.getMesReferencia(), calculo.getAnoReferencia()), fontBody));
        document.add(new Paragraph(" "));
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        document.add(new Paragraph("Valor Total do DAS: " + formatCurrency(calculo.getDasTotal()), fontHeader));
        document.add(new Paragraph(" "));

        for (ResultadoCalculoDetalhado detalhe : calculo.getDetalhes()) {
            document.add(new Paragraph("Detalhamento por Atividade - Anexo " + detalhe.anexoAplicado(), fontHeader));
            
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8);
            String infoAdicional = String.format("Cálculo com base no RBT12 de %s e Alíquota Efetiva de %.4f%%", 
                                                formatCurrency(detalhe.rbt12()), 
                                                detalhe.aliquotaEfetivaTotal() * 100);
            document.add(new Paragraph(infoAdicional, fontSmall));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1.5f, 1.5f});
            table.setSpacingBefore(10);
            
            addTableHeader(table, "Descrição", "Receita (R$)", "Valor do DAS (R$)");
            
            addTableRow(table, "Receita Normal", formatCurrency(detalhe.rpaNormal()), formatCurrency(detalhe.dasNormal()));
            if (detalhe.rpaComRetencao() > 0) {
                addTableRow(table, "Receita c/ Retenção ISS", formatCurrency(detalhe.rpaComRetencao()), formatCurrency(detalhe.dasComRetencaoLiquido()));
                addTableRow(table, "(ISS Retido na Fonte)", "-", "(" + formatCurrency(detalhe.issRetido()) + ")");
            }
            if (detalhe.rpaStICMS() > 0) {
                 addTableRow(table, "Receita c/ ICMS-ST", formatCurrency(detalhe.rpaStICMS()), formatCurrency(detalhe.dasStICMS()));
            }

            addTableFooter(table, "Subtotal do Anexo", formatCurrency(detalhe.rpaTotal()), formatCurrency(detalhe.dasTotal()));

            document.add(table);
            document.add(new Paragraph(" "));
        }
        
        document.close();
        
        return new ByteArrayInputStream(out.toByteArray());
    }
    
    // --- MÉTODOS AUXILIARES ---
    private void addTableHeader(PdfPTable table, String... headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... cells) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (String cellText : cells) {
            table.addCell(new Phrase(cellText, font));
        }
    }
    
    private void addTableFooter(PdfPTable table, String... cells) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        for (String cellText : cells) {
            PdfPCell cell = new PdfPCell(new Phrase(cellText, font));
            cell.setBackgroundColor(new Color(230, 230, 230));
            table.addCell(cell);
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }
    
    private String formatCnpj(String cnpj) {
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}