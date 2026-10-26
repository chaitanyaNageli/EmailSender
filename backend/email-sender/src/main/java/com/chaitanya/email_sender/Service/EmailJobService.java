
package com.chaitanya.email_sender.Service;

import com.chaitanya.email_sender.model.EmailJob;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailJobService {

    private final EmailService emailService;

    private final ConcurrentHashMap<String, EmailJob> jobs =
            new ConcurrentHashMap<>();

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    public EmailJobService(EmailService emailService) {
        this.emailService = emailService;
    }

    public EmailJob startJob(
            List<String> emails,
            String subject,
            String body,
            List<byte[]> attachmentContents,
            List<String> attachmentNames) {

        EmailJob job = new EmailJob(emails.size());
        jobs.put(job.getJobId(), job);

        executor.submit(() -> {
            job.setStatus("RUNNING");

            try {
                for (String email : emails) {
                    try {
                        emailService.sendEmailWithBytes(
                                email,
                                subject,
                                body,
                                attachmentContents,
                                attachmentNames
                        );

                        job.addResult(
                                email, "SENT", "SMTP operation completed"
                        );

                    } catch (Exception e) {
                        job.addResult(
                                email, "FAILED", "Sending failed"
                        );
                    }
                }

                job.setStatus("COMPLETED");

            } catch (Exception e) {
                job.setStatus("FAILED");
            }
        });

        return job;
    }

    public EmailJob getJob(String jobId) {
        return jobs.get(jobId);
    }
}
