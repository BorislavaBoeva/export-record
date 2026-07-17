package app.web.mapper;

import app.model.ExportRecord;
import app.web.dto.ExportCreateRequestDto;
import app.web.dto.ExportResponseDto;
import app.web.dto.ExportUpdateRequestDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExportRecordMapper {
    // CreateDto -> Entity
    public static ExportRecord toEntity(ExportCreateRequestDto dto) {
        return ExportRecord.builder()
                .userId(dto.getUserId())
                .fileName(dto.getFileName())
                .description(dto.getDescription())
                .exportType(dto.getExportType())
                .exportStatus(dto.getExportStatus())
                .build();
    }

    // Entity -> Response DTO
    public static ExportResponseDto toDto(ExportRecord entity) {
        return ExportResponseDto.builder()
                .id(entity.getId())
                .exportType(entity.getExportType())
                .fileName(entity.getFileName())
                .exportDate(entity.getExportDate())
                .description(entity.getDescription())
                .userId(entity.getUserId())
                .updatedOn(entity.getUpdatedOn())
                .exportStatus(entity.getExportStatus())
                .deleted(entity.isDeleted())
                .build();
    }

    // UpdateDto -> existing Entity (partial update)
    public static void updateEntityFromDto(ExportRecord entity, ExportUpdateRequestDto dto) {
        entity.setFileName(dto.getFileName());
        entity.setDescription(dto.getDescription());
        entity.setExportType(dto.getExportType());
    }
}