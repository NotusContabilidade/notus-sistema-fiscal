package com.notus.contabil.sistema_fiscal.services;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.notus.contabil.sistema_fiscal.entity.Calculo;
import com.notus.contabil.sistema_fiscal.entity.ResultadoCalculoDetalhado;
import com.notus.contabil.sistema_fiscal.repository.CalculoRepository;

@Service
public class RelatorioService {

    @Autowired
    private CalculoRepository calculoRepository;
    
    public record ArquivoExportado(String nomeArquivo, ByteArrayInputStream stream) {}

    @Transactional(readOnly = true)
    public ArquivoExportado gerarCalculoExcel(Long calculoId) throws IOException {
        Calculo calculo = calculoRepository.findById(calculoId)
                .orElseThrow(() -> new RuntimeException("Cálculo não encontrado"));
        
        String nomeArquivo = criarNomeArquivo(calculo, "xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Relatório de Apuração");

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("Relatório de Apuração - " + calculo.getCliente().getRazaoSocial());
            
            sheet.createRow(rowNum++); 

            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Período de Apuração:");
            infoRow.createCell(1).setCellValue(String.format("%02d/%d", calculo.getMesReferencia(), calculo.getAnoReferencia()));
            infoRow.createCell(3).setCellValue("Valor Total do DAS:");
            infoRow.createCell(4).setCellValue(formatCurrency(calculo.getDasTotal()));

            sheet.createRow(rowNum++);

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
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            return new ArquivoExportado(nomeArquivo, inputStream);
        }
    }

    @Transactional(readOnly = true)
    public ArquivoExportado gerarCalculoPdf(Long calculoId) throws Exception {
         Calculo calculo = calculoRepository.findById(calculoId)
                .orElseThrow(() -> new RuntimeException("Cálculo não encontrado"));

        String nomeArquivo = criarNomeArquivo(calculo, "pdf");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        HeaderFooter footer = new HeaderFooter(new Phrase("© " + LocalDateTime.now().getYear() + " Nótus Sistema Fiscal | Página: ", fontFooter), true);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setBorder(Rectangle.TOP);
        document.setFooter(footer);

        document.open();

        Font fontBrand = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(0xA1, 0x37, 0x51));
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Color primaryColor = new Color(0xA1, 0x37, 0x51);

        document.add(new Paragraph("Nótus Contábil", fontBrand));
        document.add(new Paragraph("Relatório de Apuração do Simples Nacional", fontTitle));
        document.add(new Paragraph(" ")); 

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 4f});
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        infoTable.addCell(new Phrase("Cliente:", fontHeader));
        infoTable.addCell(new Phrase(calculo.getCliente().getRazaoSocial(), fontBody));
        infoTable.addCell(new Phrase("CNPJ:", fontHeader));
        infoTable.addCell(new Phrase(formatCnpj(calculo.getCliente().getCnpj()), fontBody));
        infoTable.addCell(new Phrase("Período:", fontHeader));
        infoTable.addCell(new Phrase(String.format("%02d/%d", calculo.getMesReferencia(), calculo.getAnoReferencia()), fontBody));
        document.add(infoTable);
        
        document.add(new Paragraph(" "));
        PdfPTable totalDasTable = new PdfPTable(1);
        totalDasTable.setWidthPercentage(100);
        PdfPCell totalCell = new PdfPCell(new Phrase("Valor Total do DAS: " + formatCurrency(calculo.getDasTotal()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        totalCell.setBackgroundColor(primaryColor);
        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalCell.setPadding(8);
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalDasTable.addCell(totalCell);
        document.add(totalDasTable);
        document.add(new Paragraph(" "));

        for (ResultadoCalculoDetalhado detalhe : calculo.getDetalhes()) {
            document.add(new Paragraph("Detalhamento por Atividade - Anexo " + detalhe.anexoAplicado(), fontTitle));
            
            String infoAdicional = String.format("Cálculo com base no RBT12 de %s e Alíquota Efetiva de %.4f%%", formatCurrency(detalhe.rbt12()), detalhe.aliquotaEfetivaTotal() * 100);
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
        
        return new ArquivoExportado(nomeArquivo, new ByteArrayInputStream(out.toByteArray()));
    }
    
    private String criarNomeArquivo(Calculo calculo, String extensao) {
        String razaoSocial = calculo.getCliente().getRazaoSocial()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replace(" ", "_");

        LocalDateTime dataCalculo = calculo.getDataCalculo();
        String dataFormatada = "data_nao_disponivel";
        if (dataCalculo != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            dataFormatada = dataCalculo.format(formatter);
        }

        return String.format("Calculo-%s-%s.%s", razaoSocial, dataFormatada, extensao);
    }
    
    private void addTableHeader(PdfPTable table, String... headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(0x6c, 0x75, 0x7d));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... cells) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (int i=0; i < cells.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(cells[i], font));
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
    
    private void addTableFooter(PdfPTable table, String... cells) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        for (int i=0; i < cells.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(cells[i], font));
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }
    
    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}