package com.chaitanya.email_sender.Service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender)
    {
        this.mailSender=mailSender;
    }

    public void sendEmail(String to, String subject, String body, List<MultipartFile> attachments) throws Exception
    {
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
                            Objects.requireNonNull(attachment.getOriginalFilename()),
                            new ByteArrayResource(attachment.getBytes())
                    );
                }
            }
        }

        mailSender.send(message);

    }

}
