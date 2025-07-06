package com.example.ImageHandling.utils;

import com.example.ImageHandling.domains.InvoiceMetadata;
import com.example.ImageHandling.domains.Invoices;
import com.example.ImageHandling.domains.Item;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExcelUtils {

    public static byte[] generateExcelFile(List<Invoices> invoicesList, String language, String zipFileName) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Invoices");

            createHeaderRow(sheet, workbook, language);

            Set<String> fileNames = new HashSet<>();
            int rowIndex = 1;

            Map<String, List<Invoices>> invoicesByIssuer = new HashMap<>();

            for (Invoices invoice : invoicesList) {
                String issuer = invoice.getInvoiceMetadata().getCompanyName();
                invoicesByIssuer.computeIfAbsent(issuer, k -> new ArrayList<>()).add(invoice);

                createInvoiceRow(sheet, rowIndex++, invoice, workbook, language);
            }

            autoSizeColumns(sheet);
            addExcelToZip(zipOutputStream, workbook, zipFileName);

            for (Map.Entry<String, List<Invoices>> entry : invoicesByIssuer.entrySet()) {
                String issuer = entry.getKey();
                List<Invoices> invoices = entry.getValue();

                for (Invoices invoice : invoices) {
                    String uniqueFileName = getUniqueFileName(invoice.getFileName(), fileNames);
                    addInvoiceToZip(zipOutputStream, invoice, "Invoices/" + issuer + "/" + uniqueFileName);
                }
            }

            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static void createHeaderRow(Sheet sheet, Workbook workbook, String language) {
        Row header = sheet.createRow(0);
        Map<String, String[]> headersMap = new HashMap<>();
        populateEnglishHeaders( headersMap );
        populatePortugueseHeaders( headersMap );
        populateFrenchHeaders( headersMap );
        populateSpanishHeaders( headersMap );

        String[] headers = headersMap.getOrDefault(language, headersMap.get("en"));

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFColor headerColor = new XSSFColor(new java.awt.Color(95, 155, 210), null);
        ((XSSFCellStyle) headerStyle).setFillForegroundColor(headerColor);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    public static String getUniqueFileName(String fileName, Set<String> fileNames) {
        if (!fileNames.contains(fileName)) {
            fileNames.add(fileName);
            return fileName;
        }

        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        String newFileName;
        do {
            newFileName = baseName + "_" + counter++ + extension;
        } while (fileNames.contains(newFileName));

        fileNames.add(newFileName);
        return newFileName;
    }

    public static void addInvoiceToZip(ZipOutputStream zipOutputStream, Invoices invoice, String zipEntryName) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zipEntry.setSize(invoice.getFileContent().length);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(invoice.getFileContent());
        zipOutputStream.closeEntry();
    }

    public static void createInvoiceRow(Sheet sheet, int rowIndex, Invoices invoice, Workbook workbook, String language) {
        Row row = sheet.createRow(rowIndex);

        CellStyle rowStyle = sheet.getWorkbook().createCellStyle();
        rowStyle.setBorderTop(BorderStyle.THIN);
        rowStyle.setBorderBottom(BorderStyle.THIN);
        rowStyle.setBorderLeft(BorderStyle.THIN);
        rowStyle.setBorderRight(BorderStyle.THIN);

        invoice = Optional.ofNullable(invoice).orElse(Invoices.builder().build() );
        InvoiceMetadata invoiceMetadata = Optional.ofNullable(invoice.getInvoiceMetadata()).orElse(new InvoiceMetadata());

        row.createCell(0).setCellValue(Optional.ofNullable(invoice.getFileName()).orElse(""));
        row.createCell(1).setCellValue(Optional.ofNullable(invoiceMetadata.getIssuerVATNumber()).orElse(""));
        row.createCell(2).setCellValue(Optional.ofNullable(invoiceMetadata.getAcquirerVATNumber()).orElse(""));
        row.createCell(3).setCellValue(Optional.ofNullable(invoiceMetadata.getCompanyName()).orElse(""));
        row.createCell(4).setCellValue(Optional.ofNullable(invoiceMetadata.getSite()).orElse(""));
        row.createCell(5).setCellValue(Optional.ofNullable(invoiceMetadata.getPhoneNumber()).orElse(""));
        row.createCell(6).setCellValue(Optional.ofNullable(invoiceMetadata.getEmail()).orElse(""));
        row.createCell(7).setCellValue(Optional.ofNullable(invoiceMetadata.getPostalCode()).orElse(""));
        row.createCell(8).setCellValue(Optional.ofNullable(invoiceMetadata.getAcquirerCountry()).orElse(""));
        row.createCell(9).setCellValue(Optional.ofNullable(invoiceMetadata.getInvoiceDate()).orElse( LocalDate.MIN ));
        row.createCell(10).setCellValue(Optional.ofNullable(invoiceMetadata.getInvoiceNumber()).orElse(""));
        row.createCell(11).setCellValue(Optional.ofNullable(invoiceMetadata.getAddress()).orElse(""));

        Cell itemsCell = row.createCell(12);
        itemsCell.setCellValue(formatItems(invoiceMetadata.getItems(), workbook, language));

        row.createCell(13).setCellValue(Optional.ofNullable(invoiceMetadata.getDocumentPaidAt()).orElse(""));
        row.createCell(14).setCellValue(Optional.ofNullable(invoiceMetadata.getClient()).orElse(""));
        row.createCell(15).setCellValue(Optional.ofNullable(invoiceMetadata.getCurrency()).orElse(""));
        row.createCell(16).setCellValue(Optional.ofNullable(invoiceMetadata.getDueDate()).orElse(""));
        row.createCell(17).setCellValue(Optional.ofNullable(invoiceMetadata.getValueAddedTax()).orElse(""));
        row.createCell(18).setCellValue(Optional.ofNullable(invoiceMetadata.getSubtotal()).orElse(""));
        row.createCell(19).setCellValue(Optional.ofNullable(invoiceMetadata.getTotal()).orElse(""));
        row.createCell(20).setCellValue(Optional.ofNullable(invoiceMetadata.getPaymentStatus()).orElse(""));
        row.createCell(21).setCellValue(Optional.ofNullable(invoiceMetadata.getComment()).orElse(""));

        for (int i = 0; i < 22; i++) {
            row.getCell(i).setCellStyle(rowStyle);
        }
    }

    public static XSSFRichTextString formatItems(List<Item> items, Workbook workbook, String language) {
        if (items == null || items.isEmpty()) {
            return new XSSFRichTextString("");
        }

        Map<String, String> translations = new HashMap<>();
        switch (language) {
            case "pt":
                translations.put("Item", "Item");
                translations.put("Quantity", "Quantidade");
                translations.put("Value Added Tax", "Imposto sobre Valor Acrescentado");
                translations.put("Subtotal", "Subtotal");
                translations.put("Total", "Total");
                translations.put("ArticleRef", "Referência do Artigo");
                break;
            case "fr":
                translations.put("Item", "Article");
                translations.put("Quantity", "Quantité");
                translations.put("Value Added Tax", "Taxe sur la Valeur Ajoutée");
                translations.put("Subtotal", "Sous-total");
                translations.put("Total", "Total");
                translations.put("ArticleRef", "Référence de l'Article");
                break;
            case "es":
                translations.put("Item", "Artículo");
                translations.put("Quantity", "Cantidad");
                translations.put("Value Added Tax", "Impuesto sobre el Valor Añadido");
                translations.put("Subtotal", "Subtotal");
                translations.put("Total", "Total");
                translations.put("ArticleRef", "Referencia del Artículo");
                break;
            default:
                translations.put("Item", "Item");
                translations.put("Quantity", "Quantity");
                translations.put("Value Added Tax", "Value Added Tax");
                translations.put("Subtotal", "Subtotal");
                translations.put("Total", "Total");
                translations.put("ArticleRef", "Article Reference");
        }

        StringBuilder itemsStringBuilder = new StringBuilder();
        for (Item item : items) {
            if (itemsStringBuilder.length() > 0) {
                itemsStringBuilder.append("; ");
            }

            itemsStringBuilder.append(translations.get("Item")).append(": ");
            if (item.getItemName() != null) {
                itemsStringBuilder.append(item.getItemName());
            }
            if (item.getItemQuantity() != null) {
                itemsStringBuilder.append(", ").append(translations.get("Quantity")).append(": ").append(item.getItemQuantity());
            }
            if (item.getItemValue() != null) {
                itemsStringBuilder.append(", ").append(translations.get("Value Added Tax")).append(": ").append(item.getItemValue());
            }
            if (item.getItemSubtotal() != null) {
                itemsStringBuilder.append(", ").append(translations.get("Subtotal")).append(": ").append(item.getItemSubtotal());
            }
            if (item.getTotalAmount() != null) {
                itemsStringBuilder.append(", ").append(translations.get("Total")).append(": ").append(item.getTotalAmount());
            }
            if (item.getArticleRef() != null) {
                itemsStringBuilder.append(", ").append(translations.get("ArticleRef")).append(": ").append(item.getArticleRef());
            }
        }

        XSSFRichTextString richTextString = new XSSFRichTextString(itemsStringBuilder.toString());
        String itemsString = richTextString.getString();

        XSSFFont boldFont = (XSSFFont) workbook.createFont();
        boldFont.setBold(true);

        int startIndex = 0;
        while ((startIndex = itemsString.indexOf(translations.get("Item") + ": ", startIndex)) != -1) {
            int endIndex = startIndex + translations.get("Item").length() + 2;
            richTextString.applyFont(startIndex, endIndex, boldFont);
            startIndex = endIndex;
        }

        return richTextString;
    }



    public static void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i <= 21; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static void addExcelToZip(ZipOutputStream zipOutputStream, Workbook workbook,  String zipFileName) throws IOException {
        try (ByteArrayOutputStream excelOutputStream = new ByteArrayOutputStream()) {
            workbook.write(excelOutputStream);
            workbook.close();

            String excelFileName = zipFileName.replace(".zip", ".xlsx");
            ZipEntry excelEntry = new ZipEntry(excelFileName);
            zipOutputStream.putNextEntry(excelEntry);
            zipOutputStream.write(excelOutputStream.toByteArray());
            zipOutputStream.closeEntry();
        }
    }

    private static void populateSpanishHeaders( Map<String, String[]> headersMap ) {
        headersMap.put("es", new String[]{"Nombres de Archivos", "Número de IVA del Emisor", "Número de IVA del Adquirente", "Nombre de la Empresa", "Sitio", "Número de Teléfono", "Correo Electrónico", "Código Postal",
            "País del Adquirente", "Fecha de la Factura", "Número de Factura", "Dirección",
            "Artículos", "Documento Pagado En", "Cliente", "Moneda", "Fecha de Vencimiento",
            "Impuesto sobre el Valor Añadido", "Subtotal", "Total", "Estado del Pago", "Comentario"});
    }

    private static void populatePortugueseHeaders( Map<String, String[]> headersMap ) {
        headersMap.put("pt", new String[]{"Nomes dos Arquivos", "NIF do Emitente", "NIF do Adquirente", "Nome da Empresa", "Site", "Número de Telefone", "Email", "Código Postal",
            "País do Adquirente", "Data da Fatura", "Número da Fatura", "Endereço",
            "Itens", "Documento Pago Em", "Cliente", "Moeda", "Data de Vencimento",
            "Imposto sobre Valor Acrescentado", "Subtotal", "Total", "Status do Pagamento", "Comentar"});
    }

    private static void populateFrenchHeaders( Map<String, String[]> headersMap ) {
        headersMap.put("fr", new String[]{"Noms des Fichiers", "Numéro de TVA de l'émetteur", "Numéro de TVA de l'acquéreur", "Nom de l'entreprise", "Site", "Numéro de téléphone", "Email", "Code Postal",
            "Pays de l'acquéreur", "Date de la facture", "Numéro de la facture", "Adresse",
            "Articles", "Document Payé À", "Client", "Devise", "Date d'échéance",
            "Taxe sur la Valeur Ajoutée", "Sous-total", "Total", "Statut du Paiement", "Commentaire"});
    }

    private static void populateEnglishHeaders( Map<String, String[]> headersMap ) {
        headersMap.put("en", new String[]{"File Names", "Issuer VAT Number", "Acquirer VAT Number", "Company Name", "Site", "Phone Number", "Email", "Postal Code",
            "Acquirer Country", "Invoice Date", "Invoice Number", "Address",
            "Items", "Document Paid At", "Client", "Currency", "Due Date",
            "Value Added Tax", "Subtotal", "Total", "Payment Status", "Comment"});
    }
}
