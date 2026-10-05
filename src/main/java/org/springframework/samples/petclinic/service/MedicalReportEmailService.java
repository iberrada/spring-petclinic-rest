package org.springframework.samples.petclinic.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ByteArrayResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MedicalReportEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@petclinic.example.com}")
    private String fromEmail;

    public MedicalReportEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendReport(String toEmail, MedicalReport report, byte[] pdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Medical Report for " + getPetName(report));

            Context context = new Context();
            context.setVariable("report", report);
            context.setVariable("petName", getPetName(report));
            context.setVariable("vetName", getVetName(report));
            context.setVariable("reportDate", report.getReportDate());
            context.setVariable("status", report.getStatus());

            String html = templateEngine.process("mail/medical-report", context);
            helper.setText(html, true);

            helper.addAttachment("medical-report-" + report.getId() + ".pdf",
                new ByteArrayResource(pdf), "application/pdf");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send medical report email", e);
        }
    }

    private String getPetName(MedicalReport report) {
        if (report.getVisit() != null && report.getVisit().getPet() != null) {
            return report.getVisit().getPet().getName();
        }
        return "Unknown Pet";
    }

    private String getVetName(MedicalReport report) {
        if (report.getAuthorVet() != null) {
            return report.getAuthorVet().getFirstName() + " " + report.getAuthorVet().getLastName();
        }
        return "Unknown Veterinarian";
    }
}