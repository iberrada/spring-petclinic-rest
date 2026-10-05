package org.springframework.samples.petclinic.repository.jpa;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.samples.petclinic.repository.MedicalReportRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;

@Repository
@Profile("jpa")
@Transactional
public class JpaMedicalReportRepositoryImpl implements MedicalReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(MedicalReport medicalReport) throws DataAccessException {
        if (medicalReport.getId() == null) {
            em.persist(medicalReport);
        } else {
            em.merge(medicalReport);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalReport findById(int id) throws DataAccessException {
        try {
            return em.find(MedicalReport.class, id);
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReport> findByVisitId(Integer visitId) {
        TypedQuery<MedicalReport> query = em.createQuery(
            "SELECT mr FROM MedicalReport mr WHERE mr.visit.id = :visitId ORDER BY mr.createdAt DESC",
            MedicalReport.class);
        query.setParameter("visitId", visitId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReport> findByAuthorVetId(Integer vetId) {
        TypedQuery<MedicalReport> query = em.createQuery(
            "SELECT mr FROM MedicalReport mr WHERE mr.authorVet.id = :vetId ORDER BY mr.createdAt DESC",
            MedicalReport.class);
        query.setParameter("vetId", vetId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReport> findBySharedWithVetId(Integer vetId) {
        TypedQuery<MedicalReport> query = em.createQuery(
            "SELECT mr FROM MedicalReport mr WHERE mr.sharedWithVet.id = :vetId ORDER BY mr.createdAt DESC",
            MedicalReport.class);
        query.setParameter("vetId", vetId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalReport> findByStatus(ReportStatus status) {
        TypedQuery<MedicalReport> query = em.createQuery(
            "SELECT mr FROM MedicalReport mr WHERE mr.status = :status ORDER BY mr.createdAt DESC",
            MedicalReport.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<MedicalReport> findAll() throws DataAccessException {
        return em.createQuery("SELECT mr FROM MedicalReport mr ORDER BY mr.createdAt DESC", MedicalReport.class).getResultList();
    }

    @Override
    public void delete(MedicalReport medicalReport) throws DataAccessException {
        if (em.contains(medicalReport)) {
            em.remove(medicalReport);
        } else {
            em.remove(em.merge(medicalReport));
        }
    }
}