package org.springframework.samples.petclinic.exception;

public class MedicalReportNotFoundException extends RuntimeException {
    public MedicalReportNotFoundException(int id) {
        super("Medical report not found with id: " + id);
    }
}