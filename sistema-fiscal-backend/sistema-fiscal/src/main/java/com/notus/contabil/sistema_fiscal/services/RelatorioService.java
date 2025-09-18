package com.notus.contabil.sistema_fiscal.services;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

import com.fasterxml.jackson.core.JsonProcessingException; // <-- CORREÇÃO #1: IMPORT ADICIONADO
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
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
    
    // Objeto para transportar os dados do arquivo gerado
    public record ArquivoExportado(String nomeArquivo, ByteArrayInputStream stream) {}
    
    // Definição de Cores e Fontes para o padrão Nótus
    private static final Color COR_PRIMARIA = new Color(0xA1, 0x37, 0x51);
    private static final Color COR_CABECALHO_TABELA = new Color(0x34, 0x3A, 0x40); // Um cinza escuro
    private static final Color COR_LINHA_ZEBRA = new Color(0xF8, 0xF9, 0xFA); // Um cinza bem claro
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COR_PRIMARIA);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_HEADER_TABELA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font FONT_CORPO = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_CORPO_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);


    @Transactional(readOnly = true)
    public ArquivoExportado gerarCalculoPdf(Long calculoId) throws Exception {
         Calculo calculo = calculoRepository.findById(calculoId)
                .orElseThrow(() -> new RuntimeException("Cálculo não encontrado"));

        String nomeArquivo = criarNomeArquivo(calculo, "pdf");
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Adiciona Cabeçalho com Logo
            adicionarCabecalho(document);

            // Adiciona Informações do Cliente
            adicionarInfosCliente(document, calculo);
            
            // Adiciona Bloco de Resumo Geral
            adicionarResumoGeral(document, calculo);

            // Adiciona Detalhamento por Anexo
            adicionarDetalhesPorAnexo(document, calculo.getDetalhes());
            
            // Adiciona Rodapé
            adicionarRodape(document);

            document.close();
            
            return new ArquivoExportado(nomeArquivo, new ByteArrayInputStream(out.toByteArray()));
        }
    }

    private void adicionarCabecalho(Document document) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 3f});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            Image logo = Image.getInstance(getClass().getClassLoader().getResource("images/logo.png"));
            logo.scaleToFit(100, 40);
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);
        } catch (Exception e) {
             headerTable.addCell(new Phrase("Nótus Fiscal", FONT_TITULO));
        }

        PdfPCell titleCell = createCell("Relatório de Apuração do Simples Nacional", FONT_TITULO, Element.ALIGN_RIGHT);
        headerTable.addCell(titleCell);
        
        document.add(headerTable);
        adicionarEspaco(document, 20);
    }
    
    private void adicionarInfosCliente(Document document, Calculo calculo) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 4f});
        infoTable.getDefaultCell().setBorder(Rectangle.BOTTOM);
        infoTable.getDefaultCell().setBorderColor(Color.LIGHT_GRAY);
        infoTable.getDefaultCell().setPadding(5);

        infoTable.addCell(createCell("Cliente:", FONT_CORPO_BOLD));
        infoTable.addCell(createCell(calculo.getCliente().getRazaoSocial(), FONT_CORPO));
        infoTable.addCell(createCell("CNPJ:", FONT_CORPO_BOLD));
        infoTable.addCell(createCell(formatCnpj(calculo.getCliente().getCnpj()), FONT_CORPO));
        infoTable.addCell(createCell("Período:", FONT_CORPO_BOLD));
        infoTable.addCell(createCell(String.format("%02d/%d", calculo.getMesReferencia(), calculo.getAnoReferencia()), FONT_CORPO));

        document.add(infoTable);
    }

    private void adicionarResumoGeral(Document document, Calculo calculo) throws DocumentException, JsonProcessingException {
        adicionarEspaco(document, 25);
        document.add(new Paragraph("Resumo Geral da Apuração", FONT_SUBTITULO));
        adicionarEspaco(document, 10);
        
        PdfPTable resumoTable = new PdfPTable(3);
        resumoTable.setWidthPercentage(100);
        resumoTable.setWidths(new float[]{1f, 1f, 1f});

        double receitaTotal = calculo.getDetalhes().stream().mapToDouble(ResultadoCalculoDetalhado::rpaTotal).sum();
        double aliquotaMedia = receitaTotal > 0 ? (calculo.getDasTotal() / receitaTotal) * 100 : 0;
        
        resumoTable.addCell(criarBlocoResumo("Receita Bruta Total no Mês", formatCurrency(receitaTotal)));
        resumoTable.addCell(criarBlocoResumo("Alíquota Efetiva Média", String.format(Locale.forLanguageTag("pt-BR"), "%.4f%%", aliquotaMedia)));
        resumoTable.addCell(criarBlocoResumo("Valor Total do DAS a Pagar", formatCurrency(calculo.getDasTotal()), true));
        
        document.add(resumoTable);
    }
    
    private void adicionarDetalhesPorAnexo(Document document, List<ResultadoCalculoDetalhado> detalhes) throws DocumentException {
         adicionarEspaco(document, 25);
        document.add(new Paragraph("Detalhamento por Atividade", FONT_SUBTITULO));
       
        for (ResultadoCalculoDetalhado detalhe : detalhes) {
             adicionarEspaco(document, 10);
            
            String infoAdicional = String.format("Anexo %s | RBT12: %s | Alíquota Efetiva: %.4f%%", 
                detalhe.anexoAplicado(), formatCurrency(detalhe.rbt12()), detalhe.aliquotaEfetivaTotal() * 100);
            
            if(detalhe.fatorR() != null){
                infoAdicional += String.format(" | Fator R: %.2f%%", detalhe.fatorR());
            }

            document.add(new Paragraph(infoAdicional, FONT_CORPO_BOLD));
            adicionarEspaco(document, 8);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1.5f, 1.5f});
            
            addTableHeader(table, "Descrição da Receita", "Valor (R$)", "DAS (R$)");
            
            int rowIndex = 0;
            // <-- CORREÇÃO #2: As chamadas agora passam o rowIndex primeiro
            addTableRow(table, rowIndex++, "Receita (Tributação Normal)", formatCurrency(detalhe.rpaNormal()), formatCurrency(detalhe.dasNormal()));
            if (detalhe.rpaComRetencao() > 0) {
                addTableRow(table, rowIndex++, "Receita c/ Retenção de ISS", formatCurrency(detalhe.rpaComRetencao()), formatCurrency(detalhe.dasComRetencaoLiquido()));
                addTableRow(table, rowIndex++, "  └ Valor do ISS Retido na Fonte", "-", "(" + formatCurrency(detalhe.issRetido()) + ")");
            }
            if (detalhe.rpaStICMS() > 0) {
                 addTableRow(table, rowIndex++, "Receita c/ Substituição Tributária (ICMS)", formatCurrency(detalhe.rpaStICMS()), formatCurrency(detalhe.dasStICMS()));
            }
            addTableFooter(table, "Subtotal do Anexo", formatCurrency(detalhe.rpaTotal()), formatCurrency(detalhe.dasTotal()));
            document.add(table);
        }
    }
    
    private void adicionarRodape(Document document) throws DocumentException {
        adicionarEspaco(document, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Gerado em' dd/MM/yyyy 'às' HH:mm:ss");
        Paragraph footer = new Paragraph("Gerado por Nótus Sistema Fiscal | " + LocalDateTime.now().format(formatter), FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
    
    // MÉTODOS AUXILIARES
    
    private PdfPCell createCell(String content, Font font, int horizontalAlignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createCell(String content, Font font) {
        return createCell(content, font, Element.ALIGN_LEFT);
    }
    
    private PdfPCell criarBlocoResumo(String titulo, String valor, boolean isDestaque) {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font fontValor = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, isDestaque ? Color.WHITE : COR_PRIMARIA);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(isDestaque ? COR_PRIMARIA : COR_LINHA_ZEBRA);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        cell.addElement(new Paragraph(titulo, fontTitulo));
        cell.addElement(new Paragraph(valor, fontValor));

        return cell;
    }
    
    private PdfPCell criarBlocoResumo(String titulo, String valor) {
        return criarBlocoResumo(titulo, valor, false);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FONT_HEADER_TABELA));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(COR_CABECALHO_TABELA);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    // <-- CORREÇÃO #2: A assinatura do método foi corrigida. O varargs (String...) agora é o último parâmetro.
    private void addTableRow(PdfPTable table, int rowIndex, String... cells) {
        int i = 0;
        for (String content : cells) {
            PdfPCell cell = new PdfPCell(new Phrase(content, FONT_CORPO));
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            cell.setPadding(5);
            cell.setBackgroundColor(rowIndex % 2 == 0 ? Color.WHITE : COR_LINHA_ZEBRA);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
            i++;
        }
    }
    
    private void addTableFooter(PdfPTable table, String... cells) {
        int i = 0;
        for (String content : cells) {
            PdfPCell cell = new PdfPCell(new Phrase(content, FONT_CORPO_BOLD));
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(5);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
            i++;
        }
    }

    private void adicionarEspaco(Document document, float size) throws DocumentException {
        Paragraph space = new Paragraph(" ");
        space.setSpacingBefore(size);
        document.add(space);
    }
    
    private String criarNomeArquivo(Calculo calculo, String extensao) {
        String razaoSocial = calculo.getCliente().getRazaoSocial()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replace(" ", "_");
        return String.format("Apuracao_Simples_Nacional_%s_%02d-%d.%s", 
            razaoSocial, calculo.getMesReferencia(), calculo.getAnoReferencia(), extensao);
    }
    
    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(value);
    }
    
    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Transactional(readOnly = true)
    public ArquivoExportado gerarCalculoExcel(Long calculoId) throws IOException, JsonProcessingException {
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
}