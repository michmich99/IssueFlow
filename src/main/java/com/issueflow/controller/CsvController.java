package com.issueflow.controller;

import com.issueflow.dto.response.ImportResultResponse;
import com.issueflow.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class CsvController {

    private final CsvService csvService;

    @GetMapping("/export")
    public ResponseEntity<String> exportTickets(@RequestParam Long projectId) {
        String csv = csvService.exportTicketsToCsv(projectId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "tickets.csv");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(csv);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultResponse> importTickets(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long projectId) {
        
        ImportResultResponse result = csvService.importTicketsFromCsv(file, projectId);
        return ResponseEntity.ok(result);
    }
}
