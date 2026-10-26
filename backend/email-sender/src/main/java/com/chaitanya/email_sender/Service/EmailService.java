
package com.chaitanya.email_sender.Service;

import com.chaitanya.email_sender.model.EmailJob;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Existing method: keeps current functionality working
    public void sendEmail(
            String to,
            String subject,
            String body,
            List<MultipartFile> attachments) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (attachment != null && !attachment.isEmpty()) {
                    helper.addAttachment(
                            Objects.requireNonNull(
                                    attachment.getOriginalFilename()),
                            new ByteArrayResource(attachment.getBytes())
                    );
                }
            }
        }

        mailSender.send(message);
    }

    // New method: suitable for background jobs
    public void sendEmailWithBytes(
            String to,
            String subject,
            String body,
            List<byte[]> attachmentContents,
            List<String> attachmentNames) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);

        if (attachmentContents != null && attachmentNames != null) {
            for (int i = 0; i < attachmentContents.size(); i++) {
                byte[] content = attachmentContents.get(i);
                String name = attachmentNames.get(i);

                helper.addAttachment(
                        name,
                        new ByteArrayResource(content)
                );
            }
        }

        mailSender.send(message);
    }
}
