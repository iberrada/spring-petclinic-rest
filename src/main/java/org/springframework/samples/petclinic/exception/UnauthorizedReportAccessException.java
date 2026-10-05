package org.springframework.samples.petclinic.exception;

public class UnauthorizedReportAccessException extends RuntimeException {
    public UnauthorizedReportAccessException(String message) {
        super(message);
    }
}