package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.MedicalReportMapper;
import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.samples.petclinic.rest.api.MedicalReportsApi;
import org.springframework.samples.petclinic.rest.dto.EmailMedicalReportRequestDto;
import org.springframework.samples.petclinic.rest.dto.MedicalReportDto;
import org.springframework.samples.petclinic.rest.dto.MedicalReportFieldsDto;
import org.springframework.samples.petclinic.rest.dto.ShareMedicalReportRequestDto;
import org.springframework.samples.petclinic.service.MedicalReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class MedicalReportRestControllerV1 implements MedicalReportsApi {

    private final MedicalReportService medicalReportService;
    private final MedicalReportMapper medicalReportMapper;

    public MedicalReportRestControllerV1(MedicalReportService medicalReportService, MedicalReportMapper medicalReportMapper) {
        this.medicalReportService = medicalReportService;
        this.medicalReportMapper = medicalReportMapper;
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<List<MedicalReportDto>> listMedicalReports(
            @RequestParam(required = false) Integer visitId,
            @RequestParam(required = false) Integer authorVetId,
            @RequestParam(required = false) String status) {
        List<MedicalReportDto> reports;
        if (visitId != null) {
            reports = medicalReportService.findByVisitId(visitId);
        } else if (authorVetId != null) {
            reports = medicalReportService.findByAuthorVetId(authorVetId);
        } else if (status != null) {
            try {
                ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
                reports = medicalReportService.findByStatus(reportStatus);
            } catch (IllegalArgumentException e) {
                reports = medicalReportService.findAll();
            }
        } else {
            reports = medicalReportService.findAll();
        }
        return reports.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(reports);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.canAccess(authentication, #reportId)")
    @Override
    public ResponseEntity<MedicalReportDto> getMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId) {
        return ResponseEntity.ok(medicalReportService.findById(reportId));
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN)")
    @Override
    public ResponseEntity<MedicalReportDto> createMedicalReport(@Valid @RequestBody MedicalReportFieldsDto medicalReportFieldsDto) {
        int currentVetId = getCurrentVetId();
        MedicalReportDto created = medicalReportService.createDraft(currentVetId, medicalReportFieldsDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/medical-reports/" + created.getId()));
        return new ResponseEntity<>(created, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.isAuthor(authentication, #reportId)")
    @Override
    public ResponseEntity<MedicalReportDto> updateMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId, @Valid @RequestBody MedicalReportFieldsDto medicalReportFieldsDto) {
        return ResponseEntity.ok(medicalReportService.updateDraft(reportId, medicalReportFieldsDto));
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.isAuthor(authentication, #reportId)")
    @Override
    public ResponseEntity<Void> deleteMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId) {
        medicalReportService.deleteDraft(reportId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.isAuthor(authentication, #reportId)")
    @Override
    public ResponseEntity<MedicalReportDto> finalizeMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId) {
        return ResponseEntity.ok(medicalReportService.finalizeReport(reportId));
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.isAuthor(authentication, #reportId)")
    @Override
    public ResponseEntity<MedicalReportDto> shareMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId, @Valid @RequestBody ShareMedicalReportRequestDto shareMedicalReportRequestDto) {
        return ResponseEntity.ok(medicalReportService.shareReport(reportId, shareMedicalReportRequestDto.getRecipientVetId()));
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.canAccess(authentication, #reportId)")
    @Override
    public ResponseEntity<org.springframework.core.io.Resource> generateMedicalReportPdf(@Min(0) @PathVariable("reportId") Integer reportId) {
        byte[] pdf = medicalReportService.generatePdf(reportId);
        ByteArrayResource resource = new ByteArrayResource(pdf);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
            .filename("medical-report-" + reportId + ".pdf").build());
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(pdf.length)
            .body(resource);
    }

    @PreAuthorize("hasRole(@roles.VET_ADMIN) and @medicalReportSecurity.canAccess(authentication, #reportId)")
    @Override
    public ResponseEntity<Void> emailMedicalReport(@Min(0) @PathVariable("reportId") Integer reportId, @RequestBody(required = false) EmailMedicalReportRequestDto emailMedicalReportRequestDto) {
        String overrideEmail = emailMedicalReportRequestDto != null ? emailMedicalReportRequestDto.getOverrideEmail() : null;
        medicalReportService.emailReport(reportId, overrideEmail);
        return ResponseEntity.ok().build();
    }

    private int getCurrentVetId() {
        return 1;
    }
}