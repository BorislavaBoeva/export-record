package app.util;

import app.model.ExportRecord;
import app.model.ExportStatus;
import app.model.ExportType;
import app.web.dto.exportRecord.ExportCreateRequestDto;
import app.web.dto.exportRecord.ExportResponseDto;
import app.web.dto.exportRecord.ExportUpdateRequestDto;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class ExportTestFactory {
    public static ExportCreateRequestDto getExportCreateRequestDto() {
        return ExportCreateRequestDto.builder()
                .userId(UUID.randomUUID())
                .fileName("test-export.csv")
                .description("Test export")
                .exportType(ExportType.CSV)
                .exportStatus(ExportStatus.SUCCEEDED)
                .build();
    }

    public static ExportUpdateRequestDto getExportUpdateRequestDto() {
        return ExportUpdateRequestDto.builder()
                .fileName("updated-export.pdf")
                .description("Updated description")
                .exportType(ExportType.PDF)
                .build();
    }

    public static ExportRecord getExportRecord() {
        return ExportRecord.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .fileName("test-export.csv")
                .description("Test export")
                .exportType(ExportType.CSV)
                .exportStatus(ExportStatus.SUCCEEDED)
                .exportDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    public static ExportResponseDto getExportResponseDto() {
        return ExportResponseDto.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .fileName("test-export.csv")
                .description("Test export")
                .exportType(ExportType.CSV)
                .exportStatus(ExportStatus.SUCCEEDED)
                .exportDate(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .deleted(false)
                .build();
    }
}