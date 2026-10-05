package org.springframework.samples.petclinic.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class MedicalReportPdfService {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 16;
    private static final float SECTION_GAP = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_OBLIQUE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    public byte[] generatePdf(MedicalReport report) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;

                // Title
                y = writeHeader(cs, y, page);

                // Report info
                y = writeReportInfo(cs, y, report);
                y -= SECTION_GAP;

                // Pet info
                y = writePetInfo(cs, y, report);
                y -= SECTION_GAP;

                // Veterinarian info
                y = writeVetInfo(cs, y, report);
                y -= SECTION_GAP;

                // Diagnosis
                y = writeSection(cs, y, "DIAGNOSIS", report.getDiagnosis(), page);
                y -= SECTION_GAP;

                // Treatment
                y = writeSection(cs, y, "TREATMENT", report.getTreatment(), page);
                y -= SECTION_GAP;

                // Notes
                y = writeSection(cs, y, "NOTES", report.getNotes(), page);
                y -= SECTION_GAP;

                // Footer
                writeFooter(cs, y, report, page);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private float writeHeader(PDPageContentStream cs, float y, PDPage page) throws IOException {
        cs.beginText();
        cs.setFont(FONT_BOLD, 18);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("SPRING PETCLINIC - MEDICAL REPORT");
        cs.endText();
        return y - LINE_HEIGHT * 2;
    }

    private float writeReportInfo(PDPageContentStream cs, float y, MedicalReport report) throws IOException {
        cs.beginText();
        cs.setFont(FONT_NORMAL, 10);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Report ID: " + report.getId());
        cs.endText();
        y -= LINE_HEIGHT;

        cs.beginText();
        cs.setFont(FONT_NORMAL, 10);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Date: " + (report.getReportDate() != null ? report.getReportDate().format(DATE_FORMATTER) : "N/A"));
        cs.endText();
        y -= LINE_HEIGHT;

        cs.beginText();
        cs.setFont(FONT_BOLD, 10);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Status: " + (report.getStatus() != null ? report.getStatus().name() : "N/A"));
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private float writePetInfo(PDPageContentStream cs, float y, MedicalReport report) throws IOException {
        y = writeSectionHeader(cs, y, "PET INFORMATION");

        if (report.getVisit() != null && report.getVisit().getPet() != null) {
            var pet = report.getVisit().getPet();
            var owner = pet.getOwner();
            var type = pet.getType();

            y = writeField(cs, y, "Name:", pet.getName());
            y = writeField(cs, y, "Type:", type != null ? type.getName() : "N/A");
            y = writeField(cs, y, "Birth Date:", pet.getBirthDate() != null ? pet.getBirthDate().format(DATE_FORMATTER) : "N/A");

            if (owner != null) {
                y = writeField(cs, y, "Owner:", owner.getFirstName() + " " + owner.getLastName());
                y = writeField(cs, y, "Owner Email:", owner.getEmail() != null ? owner.getEmail() : "Not provided");
                y = writeField(cs, y, "Owner Phone:", owner.getTelephone());
            }
        }
        return y;
    }

    private float writeVetInfo(PDPageContentStream cs, float y, MedicalReport report) throws IOException {
        y = writeSectionHeader(cs, y, "VETERINARIAN");

        if (report.getAuthorVet() != null) {
            y = writeField(cs, y, "Author:", report.getAuthorVet().getFirstName() + " " + report.getAuthorVet().getLastName());
            y = writeField(cs, y, "Email:", report.getAuthorVet().getEmail() != null ? report.getAuthorVet().getEmail() : "Not provided");
        }

        if (report.getSharedWithVet() != null) {
            y = writeField(cs, y, "Shared With:", report.getSharedWithVet().getFirstName() + " " + report.getSharedWithVet().getLastName());
            y = writeField(cs, y, "Email:", report.getSharedWithVet().getEmail() != null ? report.getSharedWithVet().getEmail() : "Not provided");
        }
        return y;
    }

    private float writeSectionHeader(PDPageContentStream cs, float y, String title) throws IOException {
        cs.beginText();
        cs.setFont(FONT_BOLD, 12);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(title);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private float writeSection(PDPageContentStream cs, float y, String title, String content, PDPage page) throws IOException {
        if (content == null || content.trim().isEmpty()) {
            return y;
        }

        y = writeSectionHeader(cs, y, title);
        return writeWrappedText(cs, y, content, page);
    }

    private float writeField(PDPageContentStream cs, float y, String label, String value) throws IOException {
        cs.beginText();
        cs.setFont(FONT_BOLD, 10);
        cs.newLineAtOffset(MARGIN + 20, y);
        cs.showText(label);
        cs.setFont(FONT_NORMAL, 10);
        cs.showText(" " + (value != null ? value : ""));
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private float writeWrappedText(PDPageContentStream cs, float y, String text, PDPage page) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return y;
        }

        float availableWidth = page.getMediaBox().getWidth() - 2 * MARGIN - 20;
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        cs.beginText();
        cs.setFont(FONT_NORMAL, 10);
        cs.newLineAtOffset(MARGIN + 20, y);

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            float width = FONT_NORMAL.getStringWidth(testLine) / 1000 * 10;

            if (width > availableWidth) {
                cs.showText(line.toString());
                cs.newLineAtOffset(0, -LINE_HEIGHT);
                y -= LINE_HEIGHT;

                if (y < MARGIN) {
                    cs.endText();
                    // For simplicity, we'll just continue on the same page
                    // In production, we'd add a new page
                }

                cs.beginText();
                cs.setFont(FONT_NORMAL, 10);
                cs.newLineAtOffset(MARGIN + 20, y);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            cs.showText(line.toString());
            y -= LINE_HEIGHT;
        }

        cs.endText();
        return y;
    }

    private void writeFooter(PDPageContentStream cs, float y, MedicalReport report, PDPage page) throws IOException {
        cs.beginText();
        cs.setFont(FONT_OBLIQUE, 8);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText("Generated: " + (report.getCreatedAt() != null ? report.getCreatedAt().format(DATETIME_FORMATTER) : "N/A"));
        cs.endText();
    }
}