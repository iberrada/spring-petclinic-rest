package org.springframework.samples.petclinic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.samples.petclinic.model.MedicalReport;
import org.springframework.samples.petclinic.rest.dto.MedicalReportDto;
import org.springframework.samples.petclinic.rest.dto.MedicalReportFieldsDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Mapper(uses = {VisitMapper.class, VetMapper.class})
public interface MedicalReportMapper {

    @Mappings({
        @Mapping(source = "visitId", target = "visit.id"),
        @Mapping(source = "authorVetId", target = "authorVet.id"),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "finalizedAt", ignore = true),
        @Mapping(target = "sharedAt", ignore = true)
    })
    MedicalReport toMedicalReport(MedicalReportDto dto);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "visit", ignore = true),
        @Mapping(target = "authorVet", ignore = true),
        @Mapping(target = "sharedWithVet", ignore = true),
        @Mapping(target = "status", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "finalizedAt", ignore = true),
        @Mapping(target = "sharedAt", ignore = true),
        @Mapping(target = "reportDate", ignore = true)
    })
    MedicalReport toMedicalReport(MedicalReportFieldsDto dto);

    @Mappings({
        @Mapping(source = "visit.id", target = "visitId"),
        @Mapping(source = "authorVet.id", target = "authorVetId"),
        @Mapping(source = "sharedWithVet.id", target = "sharedWithVetId"),
        @Mapping(source = "visit.pet.name", target = "petName"),
        @Mapping(source = "visit.pet.type.name", target = "petType"),
        @Mapping(source = "visit.pet.birthDate", target = "petBirthDate"),
        @Mapping(target = "ownerName", expression = "java(report.getVisit().getPet().getOwner().getFirstName() + \" \" + report.getVisit().getPet().getOwner().getLastName())"),
        @Mapping(source = "visit.pet.owner.email", target = "ownerEmail"),
        @Mapping(source = "visit.pet.owner.telephone", target = "ownerPhone"),
        @Mapping(target = "authorVetName", expression = "java(report.getAuthorVet().getFirstName() + \" \" + report.getAuthorVet().getLastName())"),
        @Mapping(source = "authorVet.email", target = "authorVetEmail"),
        @Mapping(target = "sharedWithVetName", expression = "java(report.getSharedWithVet() != null ? report.getSharedWithVet().getFirstName() + \" \" + report.getSharedWithVet().getLastName() : null)"),
        @Mapping(source = "sharedWithVet.email", target = "sharedWithVetEmail")
    })
    MedicalReportDto toDto(MedicalReport report);

    List<MedicalReportDto> toDtos(List<MedicalReport> reports);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(java.time.ZoneOffset.UTC) : null;
    }
}