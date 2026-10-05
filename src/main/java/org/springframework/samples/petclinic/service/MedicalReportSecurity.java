package org.springframework.samples.petclinic.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.samples.petclinic.repository.MedicalReportRepository;
import org.springframework.samples.petclinic.model.MedicalReport;

@Component
public class MedicalReportSecurity {

    private final MedicalReportRepository medicalReportRepository;

    public MedicalReportSecurity(MedicalReportRepository medicalReportRepository) {
        this.medicalReportRepository = medicalReportRepository;
    }

    public boolean isAuthor(Authentication auth, int reportId) {
        String vetEmail = auth.getName();
        MedicalReport report = medicalReportRepository.findById(reportId);
        return report != null && report.getAuthorVet() != null 
            && vetEmail.equalsIgnoreCase(report.getAuthorVet().getEmail());
    }

    public boolean isSharedWith(Authentication auth, int reportId) {
        String vetEmail = auth.getName();
        MedicalReport report = medicalReportRepository.findById(reportId);
        return report != null && report.getSharedWithVet() != null 
            && vetEmail.equalsIgnoreCase(report.getSharedWithVet().getEmail());
    }

    public boolean canAccess(Authentication auth, int reportId) {
        return isAuthor(auth, reportId) || isSharedWith(auth, reportId);
    }
}