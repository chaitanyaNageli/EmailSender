
package com.chaitanya.email_sender.controller;

import com.chaitanya.email_sender.Service.EmailService;
import com.chaitanya.email_sender.Service.ExcelService;
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
    private final EmailService emailService;

    public EmailController(
            ExcelService excelService,
            EmailService emailService) {
        this.excelService = excelService;
        this.emailService = emailService;
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
                        .body("Please upload an Excel file.");
            }

            List<String> emails = excelService.emails(file);

            if (emails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("No email addresses found in Excel.");
            }


            List<Map<String, String>> results = new ArrayList<>();

            for (String email : emails) {
                try {
                    emailService.sendEmail(email, subject, body, attachments);

                    results.add(Map.of(
                            "email", email,
                            "status", "SENT",
                            "message", "Accepted by server"
                    ));

                } catch (Exception e) {
                    results.add(Map.of(
                            "email", email,
                            "status", "FAILED",
                            "message", "Sending failed"
                    ));
                }
            }
            return ResponseEntity.ok(Map.of(
                    "total", results.size(),
                    "sent", results.stream()
                            .filter(r -> r.get("status").equals("SENT")).count(),
                    "failed", results.stream()
                            .filter(r -> r.get("status").equals("FAILED")).count(),
                    "results", results
            ));


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Email sending or Excel processing failed.");
        }
    }
}
