package com.issueflow.service;

import com.issueflow.dto.response.ImportResultResponse;
import com.issueflow.entity.Project;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.entity.enums.Priority;
import com.issueflow.entity.enums.TicketStatus;
import com.issueflow.entity.enums.TicketType;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.ProjectRepository;
import com.issueflow.repository.TicketRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvService {

    private final TicketRepository ticketRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public String exportTicketsToCsv(Long projectId) {
        if (!projectRepo.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<Ticket> tickets = ticketRepo.findByProjectId(projectId);
        StringBuilder csv = new StringBuilder();
        csv.append("id,title,description,status,priority,type,assigneeId\n");

        for (Ticket ticket : tickets) {
            csv.append(ticket.getId()).append(",");
            csv.append(escapeCsv(ticket.getTitle())).append(",");
            csv.append(escapeCsv(ticket.getDescription())).append(",");
            csv.append(ticket.getStatus()).append(",");
            csv.append(ticket.getPriority()).append(",");
            csv.append(ticket.getType()).append(",");
            csv.append(ticket.getAssignee() != null ? ticket.getAssignee().getId() : "").append("\n");
        }

        return csv.toString();
    }

    @Transactional
    public ImportResultResponse importTicketsFromCsv(MultipartFile file, Long projectId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        int created = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line = reader.readLine();
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = parseCsvLine(line);
                    
                    if (fields.length < 6) {
                        errors.add("Line " + lineNumber + ": Insufficient fields");
                        failed++;
                        continue;
                    }

                    Ticket ticket = new Ticket();
                    ticket.setTitle(fields[0]);
                    ticket.setDescription(fields[1]);
                    ticket.setStatus(TicketStatus.valueOf(fields[2]));
                    ticket.setPriority(Priority.valueOf(fields[3]));
                    ticket.setType(TicketType.valueOf(fields[4]));
                    ticket.setProject(project);

                    if (fields.length > 5 && !fields[5].isEmpty()) {
                        Long assigneeId = Long.parseLong(fields[5]);
                        User assignee = userRepo.findById(assigneeId).orElse(null);
                        ticket.setAssignee(assignee);
                    }

                    ticket.setIsOverdue(false);
                    ticketRepo.save(ticket);
                    created++;

                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                    failed++;
                }
            }

        } catch (IOException e) {
            errors.add("Failed to read CSV file: " + e.getMessage());
            failed++;
        }

        return new ImportResultResponse(created, failed, errors);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }
}
