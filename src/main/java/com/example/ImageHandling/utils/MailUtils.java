package com.example.ImageHandling.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class MailUtils {

    private final JavaMailSender javaMailSender;
    private final Executor executor;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public MailUtils(JavaMailSender javaMailSender, @Qualifier("emailTaskExecutor") Executor executor, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.executor = executor;
        this.templateEngine = templateEngine;
    }

    public CompletableFuture<Boolean> sendEmail(String fromEmail, String[] toEmails, String subject, String text) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isSuccess;
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                message.setFrom(new InternetAddress(fromEmail));

                InternetAddress[] recipients = Arrays.stream(toEmails)
                        .map(email -> {
                            try {
                                return new InternetAddress(email);
                            } catch (AddressException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toArray(InternetAddress[]::new);

                message.setRecipients(Message.RecipientType.TO, recipients);
                message.setSubject(subject);
                message.setText(text);

                javaMailSender.send(message);
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            }
            return isSuccess;
        }, executor);
    }

    public CompletableFuture<Boolean> sendEmailWithAttachments(String fromEmail, String[] toEmails, String ccEmail, String subject, String text, List<Attachment> attachments) {
        return CompletableFuture.supplyAsync(() -> {
            boolean isSuccess;
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(fromEmail);
                helper.setTo(toEmails);
                helper.setCc(ccEmail);
                helper.setSubject(subject);
                helper.setText(text, true);

                for (Attachment attachment : attachments) {
                    ByteArrayResource resource = new ByteArrayResource(attachment.getFileContent());
                    helper.addAttachment(attachment.getFileName(), resource);
                }

                javaMailSender.send(message);
                isSuccess = true;
            } catch (Exception e) {
                isSuccess = false;
                e.printStackTrace();
            }
            return isSuccess;
        }, executor);
    }

    public static class Attachment {
        private final String fileName;
        private final byte[] fileContent;

        public Attachment(String fileName, byte[] fileContent) {
            this.fileName = fileName;
            this.fileContent = fileContent;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFileContent() {
            return fileContent;
        }
    }
}
