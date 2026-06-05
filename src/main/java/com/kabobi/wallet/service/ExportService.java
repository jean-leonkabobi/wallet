package com.kabobi.wallet.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.kabobi.wallet.dto.ExportRequestDTO;
import com.kabobi.wallet.model.Expense;
import com.kabobi.wallet.model.Revenue;
import com.kabobi.wallet.repository.ExpenseRepository;
import com.kabobi.wallet.repository.RevenueRepository;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExportService {

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,##0.00 €");

    public byte[] exportToCSV(ExportRequestDTO request) {
        try {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);

            // En-tête
            String[] header = {"Type", "Date", "Description", "Catégorie", "Montant", "Notes"};
            csvWriter.writeNext(header);

            // Données
            List<String[]> data = prepareExportData(request);
            csvWriter.writeAll(data);

            // Total
            BigDecimal total = calculateTotal(data);
            String[] totalRow = {"", "", "", "TOTAL", total.toString(), ""};
            csvWriter.writeNext(totalRow);

            csvWriter.close();

            return stringWriter.toString().getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export CSV", e);
        }
    }

    public byte[] exportToPDF(ExportRequestDTO request) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Rapport des Transactions", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Période
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph period = new Paragraph(
                    "Période du " + request.getStartDate().format(DATE_FORMATTER) +
                            " au " + request.getEndDate().format(DATE_FORMATTER),
                    subtitleFont
            );
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // Tableau
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // En-têtes du tableau
            String[] headers = {"Type", "Date", "Description", "Catégorie", "Montant", "Notes"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Données
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            List<String[]> data = prepareExportData(request);

            for (String[] row : data) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, dataFont));
                    cell.setPadding(3);
                    table.addCell(cell);
                }
            }

            // Ligne total
            BigDecimal total = calculateTotal(data);
            PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL", headerFont));
            totalCell.setColspan(4);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            table.addCell(totalCell);

            PdfPCell totalAmountCell = new PdfPCell(new Phrase(AMOUNT_FORMATTER.format(total), headerFont));
            totalAmountCell.setPadding(5);
            table.addCell(totalAmountCell);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            table.addCell(emptyCell);

            document.add(table);

            // Pied de page
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Paragraph footer = new Paragraph(
                    "Document généré le " + new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()),
                    footerFont
            );
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'export PDF", e);
        }
    }

    private List<String[]> prepareExportData(ExportRequestDTO request) {
        List<String[]> allData = new java.util.ArrayList<>();

        // Récupérer les revenus si nécessaire
        if (request.getType() == null || "REVENUE".equalsIgnoreCase(request.getType()) || "ALL".equalsIgnoreCase(request.getType())) {
            List<Revenue> revenues = getRevenuesForExport(request);
            for (Revenue revenue : revenues) {
                String[] row = {
                        "Revenu",
                        revenue.getRevenueDate().format(DATE_FORMATTER),
                        revenue.getDescription(),
                        "",
                        AMOUNT_FORMATTER.format(revenue.getAmount()),
                        revenue.getNotes() != null ? revenue.getNotes() : ""
                };
                allData.add(row);
            }
        }

        // Récupérer les dépenses si nécessaire
        if (request.getType() == null || "EXPENSE".equalsIgnoreCase(request.getType()) || "ALL".equalsIgnoreCase(request.getType())) {
            List<Expense> expenses = getExpensesForExport(request);
            for (Expense expense : expenses) {
                String[] row = {
                        "Dépense",
                        expense.getExpenseDate().format(DATE_FORMATTER),
                        expense.getDescription(),
                        expense.getCategory().getName(),
                        "-" + AMOUNT_FORMATTER.format(expense.getAmount()),
                        expense.getNotes() != null ? expense.getNotes() : ""
                };
                allData.add(row);
            }
        }

        // Trier par date
        allData.sort((a, b) -> a[1].compareTo(b[1]));

        return allData;
    }

    private List<Revenue> getRevenuesForExport(ExportRequestDTO request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return revenueRepository.findByRevenueDateBetween(request.getStartDate(), request.getEndDate());
        }
        return revenueRepository.findAll();
    }

    private List<Expense> getExpensesForExport(ExportRequestDTO request) {
        List<Expense> expenses;

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getCategoryId() != null) {
                expenses = expenseRepository.findByCategoryIdAndExpenseDateBetween(
                        request.getCategoryId(), request.getStartDate(), request.getEndDate()
                );
            } else {
                expenses = expenseRepository.findByExpenseDateBetween(
                        request.getStartDate(), request.getEndDate()
                );
            }
        } else {
            expenses = expenseRepository.findAll();
        }

        return expenses;
    }

    private BigDecimal calculateTotal(List<String[]> data) {
        BigDecimal total = BigDecimal.ZERO;
        for (String[] row : data) {
            String amountStr = row[4].replace("€", "").replace(",", ".").replace(" ", "").replace("-", "");
            BigDecimal amount = new BigDecimal(amountStr);
            if (row[4].startsWith("-")) {
                total = total.subtract(amount);
            } else {
                total = total.add(amount);
            }
        }
        return total;
    }
}