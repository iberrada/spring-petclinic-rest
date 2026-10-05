package org.springframework.samples.petclinic.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.model.ReportStatus;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.MedicalReportRepository;
import org.springframework.stereotype.Repository;

/**
 * A simple JDBC-based implementation of the {@link MedicalReportRepository} interface.
 */
@DependsOnDatabaseInitialization
@Repository
@Profile("jdbc")
@Primary
public class JdbcMedicalReportRepositoryImpl implements MedicalReportRepository {

    protected SimpleJdbcInsert insertMedicalReport;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcMedicalReportRepositoryImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        this.insertMedicalReport = new SimpleJdbcInsert(dataSource)
            .withTableName("medical_reports")
            .usingGeneratedKeyColumns("id");
    }

    protected MapSqlParameterSource createMedicalReportParameterSource(MedicalReport report) {
        return new MapSqlParameterSource()
            .addValue("id", report.getId())
            .addValue("visit_id", report.getVisit().getId())
            .addValue("author_vet_id", report.getAuthorVet().getId())
            .addValue("shared_with_vet_id", report.getSharedWithVet() != null ? report.getSharedWithVet().getId() : null)
            .addValue("diagnosis", report.getDiagnosis())
            .addValue("treatment", report.getTreatment())
            .addValue("notes", report.getNotes())
            .addValue("report_date", report.getReportDate())
            .addValue("status", report.getStatus().name())
            .addValue("created_at", report.getCreatedAt())
            .addValue("finalized_at", report.getFinalizedAt())
            .addValue("shared_at", report.getSharedAt());
    }

    @Override
    public List<MedicalReport> findByVisitId(Integer visitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("visitId", visitId);
        return this.namedParameterJdbcTemplate.query(
            "SELECT * FROM medical_reports WHERE visit_id = :visitId ORDER BY created_at DESC",
            params,
            new JdbcMedicalReportRowMapper());
    }

    @Override
    public MedicalReport findById(int id) throws DataAccessException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            return this.namedParameterJdbcTemplate.queryForObject(
                "SELECT * FROM medical_reports WHERE id = :id",
                params,
                new JdbcMedicalReportRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(MedicalReport.class, id);
        }
    }

    @Override
    public List<MedicalReport> findByAuthorVetId(Integer vetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("vetId", vetId);
        return this.namedParameterJdbcTemplate.query(
            "SELECT * FROM medical_reports WHERE author_vet_id = :vetId ORDER BY created_at DESC",
            params,
            new JdbcMedicalReportRowMapper());
    }

    @Override
    public List<MedicalReport> findBySharedWithVetId(Integer vetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("vetId", vetId);
        return this.namedParameterJdbcTemplate.query(
            "SELECT * FROM medical_reports WHERE shared_with_vet_id = :vetId ORDER BY created_at DESC",
            params,
            new JdbcMedicalReportRowMapper());
    }

    @Override
    public List<MedicalReport> findByStatus(ReportStatus status) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status.name());
        return this.namedParameterJdbcTemplate.query(
            "SELECT * FROM medical_reports WHERE status = :status ORDER BY created_at DESC",
            params,
            new JdbcMedicalReportRowMapper());
    }

    @Override
    public Collection<MedicalReport> findAll() throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        return this.namedParameterJdbcTemplate.query(
            "SELECT * FROM medical_reports ORDER BY created_at DESC",
            params,
            new JdbcMedicalReportRowMapper());
    }

    @Override
    public void save(MedicalReport report) throws DataAccessException {
        if (report.isNew()) {
            Number newKey = this.insertMedicalReport.executeAndReturnKey(createMedicalReportParameterSource(report));
            report.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE medical_reports SET visit_id=:visit_id, author_vet_id=:author_vet_id, " +
                "shared_with_vet_id=:shared_with_vet_id, diagnosis=:diagnosis, treatment=:treatment, " +
                "notes=:notes, report_date=:report_date, status=:status, finalized_at=:finalized_at, " +
                "shared_at=:shared_at WHERE id=:id",
                createMedicalReportParameterSource(report));
        }
    }

    @Override
    public void delete(MedicalReport report) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", report.getId());
        this.namedParameterJdbcTemplate.update("DELETE FROM medical_reports WHERE id=:id", params);
    }

    protected class JdbcMedicalReportRowMapper implements RowMapper<MedicalReport> {

        @Override
        public MedicalReport mapRow(ResultSet rs, int rowNum) throws SQLException {
            MedicalReport report = new MedicalReport();
            report.setId((int) rs.getLong("id"));
            report.setDiagnosis(rs.getString("diagnosis"));
            report.setTreatment(rs.getString("treatment"));
            report.setNotes(rs.getString("notes"));
            
            LocalDate reportDate = rs.getDate("report_date") != null ? 
                rs.getDate("report_date").toLocalDate() : null;
            report.setReportDate(reportDate);
            
            String statusStr = rs.getString("status");
            report.setStatus(statusStr != null ? ReportStatus.valueOf(statusStr) : ReportStatus.DRAFT);
            
            LocalDateTime createdAt = rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null;
            report.setCreatedAt(createdAt);
            
            LocalDateTime finalizedAt = rs.getTimestamp("finalized_at") != null ?
                rs.getTimestamp("finalized_at").toLocalDateTime() : null;
            report.setFinalizedAt(finalizedAt);
            
            LocalDateTime sharedAt = rs.getTimestamp("shared_at") != null ?
                rs.getTimestamp("shared_at").toLocalDateTime() : null;
            report.setSharedAt(sharedAt);

            // Load visit with pet and owner
            Integer visitId = rs.getInt("visit_id");
            if (visitId != null) {
                Visit visit = loadVisit(visitId);
                report.setVisit(visit);
            }

            // Load author vet
            Integer authorVetId = rs.getInt("author_vet_id");
            if (authorVetId != null) {
                Vet authorVet = loadVet(authorVetId);
                report.setAuthorVet(authorVet);
            }

            // Load shared with vet
            Integer sharedWithVetId = rs.getInt("shared_with_vet_id");
            if (sharedWithVetId != null) {
                Vet sharedWithVet = loadVet(sharedWithVetId);
                report.setSharedWithVet(sharedWithVet);
            }

            return report;
        }

        private Visit loadVisit(Integer visitId) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", visitId);
            return namedParameterJdbcTemplate.queryForObject(
                "SELECT v.id, v.pet_id, v.visit_date, v.description, " +
                "p.id as pet_id, p.name as pet_name, p.birth_date as pet_birth_date, p.type_id as pet_type_id, p.owner_id as pet_owner_id, " +
                "o.id as owner_id, o.first_name as owner_first_name, o.last_name as owner_last_name, o.email as owner_email, o.telephone as owner_telephone, " +
                "t.name as pet_type_name " +
                "FROM visits v " +
                "JOIN pets p ON v.pet_id = p.id " +
                "JOIN owners o ON p.owner_id = o.id " +
                "JOIN types t ON p.type_id = t.id " +
                "WHERE v.id = :id",
                params,
                (rs, rowNum) -> {
                    Visit visit = new Visit();
                    visit.setId(rs.getInt("id"));
                    LocalDate visitDate = rs.getDate("visit_date") != null ? rs.getDate("visit_date").toLocalDate() : null;
                    visit.setDate(visitDate);
                    visit.setDescription(rs.getString("description"));

                    org.springframework.samples.petclinic.model.Pet pet = new org.springframework.samples.petclinic.model.Pet();
                    pet.setId(rs.getInt("pet_id"));
                    pet.setName(rs.getString("pet_name"));
                    LocalDate birthDate = rs.getDate("pet_birth_date") != null ? rs.getDate("pet_birth_date").toLocalDate() : null;
                    pet.setBirthDate(birthDate);
                    
                    org.springframework.samples.petclinic.model.PetType petType = new org.springframework.samples.petclinic.model.PetType();
                    petType.setId(rs.getInt("pet_type_id"));
                    petType.setName(rs.getString("pet_type_name"));
                    pet.setType(petType);
                    
                    org.springframework.samples.petclinic.model.Owner owner = new org.springframework.samples.petclinic.model.Owner();
                    owner.setId(rs.getInt("owner_id"));
                    owner.setFirstName(rs.getString("owner_first_name"));
                    owner.setLastName(rs.getString("owner_last_name"));
                    owner.setEmail(rs.getString("owner_email"));
                    owner.setTelephone(rs.getString("owner_telephone"));
                    pet.setOwner(owner);
                    
                    visit.setPet(pet);
                    return visit;
                });
        }

        private Vet loadVet(Integer vetId) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", vetId);
            return namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, email FROM vets WHERE id = :id",
                params,
                BeanPropertyRowMapper.newInstance(Vet.class));
        }
    }
}