
package com.chaitanya.email_sender.controller;

import com.chaitanya.email_sender.Service.EmailJobService;
import com.chaitanya.email_sender.Service.ExcelService;
import com.chaitanya.email_sender.model.EmailJob;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final ExcelService excelService;
    private final EmailJobService emailJobService;

    public EmailController(
            ExcelService excelService,
            EmailJobService emailJobService) {
        this.excelService = excelService;
        this.emailJobService = emailJobService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "attachments", required = false)
            List<MultipartFile> attachments) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "Please upload an Excel file."));
            }

            List<String> emails = excelService.emails(file);

            if (emails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "No email addresses found in Excel."));
            }

            // Copy attachments before the HTTP request ends.
            List<byte[]> attachmentContents = new ArrayList<>();
            List<String> attachmentNames = new ArrayList<>();

            if (attachments != null) {
                for (MultipartFile attachment : attachments) {
                    if (attachment != null && !attachment.isEmpty()) {
                        attachmentContents.add(attachment.getBytes());

                        String name = attachment.getOriginalFilename();
                        attachmentNames.add(
                                name == null || name.isBlank()
                                        ? "attachment"
                                        : name
                        );
                    }
                }
            }

            EmailJob job = emailJobService.startJob(
                    emails,
                    subject,
                    body,
                    attachmentContents,
                    attachmentNames
            );

            return ResponseEntity.accepted().body(Map.of(
                    "jobId", job.getJobId(),
                    "status", job.getStatus(),
                    "total", job.getTotal(),
                    "message", "Email job started successfully."
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "message",
                            "Email upload or job creation failed."
                    ));
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(
            @PathVariable String jobId) {

        EmailJob job = emailJobService.getJob(jobId);

        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "jobId", job.getJobId(),
                "status", job.getStatus(),
                "total", job.getTotal(),
                "processed", job.getProcessed(),
                "sent", job.getSent(),
                "failed", job.getFailed(),
                "results", job.getResults()
        ));
    }
}
