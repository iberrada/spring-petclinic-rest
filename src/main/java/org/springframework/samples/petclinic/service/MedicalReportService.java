package org.springframework.samples.petclinic.service;


import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.samples.petclinic.rest.dto.MedicalReportDto;
import org.springframework.samples.petclinic.rest.dto.MedicalReportFieldsDto;

import java.util.List;

public interface MedicalReportService {

    MedicalReportDto createDraft(int currentVetId, MedicalReportFieldsDto dto);

    MedicalReportDto findById(int reportId);

    List<MedicalReportDto> findByVisitId(int visitId);

    List<MedicalReportDto> findByAuthorVetId(int vetId);

    List<MedicalReportDto> findBySharedWithVetId(int vetId);

    List<MedicalReportDto> findByStatus(ReportStatus status);

    List<MedicalReportDto> findAll();

    MedicalReportDto updateDraft(int reportId, MedicalReportFieldsDto dto);

    void deleteDraft(int reportId);

    MedicalReportDto finalizeReport(int reportId);

    MedicalReportDto shareReport(int reportId, int recipientVetId);

    byte[] generatePdf(int reportId);

    void emailReport(int reportId, String overrideEmail);
}
