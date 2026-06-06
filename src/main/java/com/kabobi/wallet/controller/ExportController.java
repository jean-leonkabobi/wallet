package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.ExportRequestDTO;
import com.kabobi.wallet.service.ExportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @PostMapping("/csv")
    public ResponseEntity<byte[]> exportToCSV(@Valid @RequestBody ExportRequestDTO request) {
        byte[] csvContent = exportService.exportToCSV(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> exportToPDF(@Valid @RequestBody ExportRequestDTO request) {
        byte[] pdfContent = exportService.exportToPDF(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transactions.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
}