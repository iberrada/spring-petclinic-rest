package org.springframework.samples.petclinic.repository.springdatajpa;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.Repository;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.repository.MedicalReportRepository;

/**
 * Spring Data JPA specialization of the {@link MedicalReportRepository} interface
 */
@Profile("spring-data-jpa")
public interface SpringDataMedicalReportRepository extends MedicalReportRepository, Repository<MedicalReport, Integer> {
}