package org.springframework.samples.petclinic.service;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.mapper.MedicalReportMapper;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.MedicalReportRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.dto.MedicalReportDto;
import org.springframework.samples.petclinic.rest.dto.MedicalReportFieldsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicalReportServiceImpl implements MedicalReportService {

    private final MedicalReportRepository medicalReportRepository;
    private final VisitRepository visitRepository;
    private final VetRepository vetRepository;
    private final MedicalReportMapper medicalReportMapper;
    private final MedicalReportPdfService pdfService;
    private final Optional<MedicalReportEmailService> emailService;
    private final MedicalReportSecurity security;

    public MedicalReportServiceImpl(MedicalReportRepository medicalReportRepository,
                                    VisitRepository visitRepository,
                                    VetRepository vetRepository,
                                    MedicalReportMapper medicalReportMapper,
                                    MedicalReportPdfService pdfService,
                                    Optional<MedicalReportEmailService> emailService,
                                    MedicalReportSecurity security) {
        this.medicalReportRepository = medicalReportRepository;
        this.visitRepository = visitRepository;
        this.vetRepository = vetRepository;
        this.medicalReportMapper = medicalReportMapper;
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.security = security;
    }

    @Override
    public MedicalReportDto createDraft(int currentVetId, MedicalReportFieldsDto dto) {
        MedicalReport report = medicalReportMapper.toMedicalReport(dto);
        
        // Load visit
        if (dto.getVisitId() != null) {
            var visit = visitRepository.findById(dto.getVisitId());
            if (visit == null) {
                throw new DataAccessException("Visit not found: " + dto.getVisitId()) {};
            }
            report.setVisit(visit);
        }
        
        // Load author vet
        Vet authorVet = vetRepository.findById(currentVetId);
        if (authorVet == null) {
            throw new DataAccessException("Author vet not found: " + currentVetId) {};
        }
        report.setAuthorVet(authorVet);
        
        report.setStatus(ReportStatus.DRAFT);
        medicalReportRepository.save(report);
        return medicalReportMapper.toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalReportDto findById(int reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        return medicalReportMapper.toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReportDto> findByVisitId(int visitId) {
        return medicalReportRepository.findByVisitId(visitId).stream()
            .map(medicalReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReportDto> findByAuthorVetId(int vetId) {
        return medicalReportRepository.findByAuthorVetId(vetId).stream()
            .map(medicalReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReportDto> findBySharedWithVetId(int vetId) {
        return medicalReportRepository.findBySharedWithVetId(vetId).stream()
            .map(medicalReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReportDto> findByStatus(ReportStatus status) {
        return medicalReportRepository.findByStatus(status).stream()
            .map(medicalReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReportDto> findAll() {
        return medicalReportRepository.findAll().stream()
            .map(medicalReportMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public MedicalReportDto updateDraft(int reportId, MedicalReportFieldsDto dto) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException("Cannot update non-DRAFT report");
        }
        
        report.setDiagnosis(dto.getDiagnosis());
        report.setTreatment(dto.getTreatment());
        report.setNotes(dto.getNotes());
        
        if (dto.getVisitId() != null && !dto.getVisitId().equals(report.getVisit().getId())) {
            var visit = visitRepository.findById(dto.getVisitId());
            if (visit == null) {
                throw new DataAccessException("Visit not found: " + dto.getVisitId()) {};
            }
            report.setVisit(visit);
        }
        
        medicalReportRepository.save(report);
        return medicalReportMapper.toDto(report);
    }

    @Override
    public void deleteDraft(int reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException("Cannot delete non-DRAFT report");
        }
        medicalReportRepository.delete(report);
    }

    @Override
    public MedicalReportDto finalizeReport(int reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new IllegalStateException("Cannot finalize non-DRAFT report");
        }
        
        report.setStatus(ReportStatus.FINALIZED);
        report.setFinalizedAt(LocalDateTime.now());
        medicalReportRepository.save(report);
        return medicalReportMapper.toDto(report);
    }

    @Override
    public MedicalReportDto shareReport(int reportId, int recipientVetId) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        if (report.getStatus() != ReportStatus.FINALIZED) {
            throw new IllegalStateException("Cannot share non-FINALIZED report");
        }
        
        Vet recipientVet = vetRepository.findById(recipientVetId);
        if (recipientVet == null) {
            throw new DataAccessException("Recipient vet not found: " + recipientVetId) {};
        }
        
        report.setStatus(ReportStatus.SHARED);
        report.setSharedWithVet(recipientVet);
        report.setSharedAt(LocalDateTime.now());
        medicalReportRepository.save(report);
        return medicalReportMapper.toDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(int reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        return pdfService.generatePdf(report);
    }

    @Override
    public void emailReport(int reportId, String overrideEmail) {
        MedicalReport report = medicalReportRepository.findById(reportId);
        if (report == null) {
            throw new DataAccessException("Medical report not found: " + reportId) {};
        }
        if (report.getStatus() == ReportStatus.DRAFT) {
            throw new IllegalStateException("Cannot email DRAFT report");
        }
        
        final String recipientEmail = determineRecipientEmail(overrideEmail, report);
        
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Owner has no email on file and no override email provided");
        }
        
        // Validate email format
        if (!isValidEmail(recipientEmail)) {
            throw new IllegalArgumentException("Invalid email format: " + recipientEmail);
        }
        
        byte[] pdf = pdfService.generatePdf(report);
        emailService.ifPresent(service -> service.sendReport(recipientEmail, report, pdf));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private String determineRecipientEmail(String overrideEmail, MedicalReport report) {
        String recipientEmail = overrideEmail;
        if (recipientEmail == null || recipientEmail.isBlank()) {
            if (report.getVisit() != null && report.getVisit().getPet() != null 
                && report.getVisit().getPet().getOwner() != null) {
                recipientEmail = report.getVisit().getPet().getOwner().getEmail();
            }
        }
        return recipientEmail;
    }
}