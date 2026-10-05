package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.model.ReportStatus;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for MedicalReport domain objects.
 */
public interface MedicalReportRepository {

    void save(MedicalReport medicalReport) throws DataAccessException;

    MedicalReport findById(int id) throws DataAccessException;

    List<MedicalReport> findByVisitId(Integer visitId);

    List<MedicalReport> findByAuthorVetId(Integer vetId);

    List<MedicalReport> findBySharedWithVetId(Integer vetId);

    List<MedicalReport> findByStatus(ReportStatus status);

    Collection<MedicalReport> findAll() throws DataAccessException;

    void delete(MedicalReport medicalReport) throws DataAccessException;
}