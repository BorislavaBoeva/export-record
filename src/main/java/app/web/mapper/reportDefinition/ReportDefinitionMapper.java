package app.web.mapper.reportDefinition;

import app.model.ReportDefinition;
import app.web.dto.reportDefinition.ReportDefinitionResponseDto;
import app.web.dto.reportDefinition.ReportDefinitionUpsertRequestDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReportDefinitionMapper {
    public static ReportDefinition toEntity(ReportDefinitionUpsertRequestDto dto) {
        return ReportDefinition.builder()
                .userId(dto.getUserId())
                .format(dto.getFormat())
                .includeHours(dto.getIncludeHours())
                .build();
    }

    public static void updateEntityFromDto(ReportDefinition entity, ReportDefinitionUpsertRequestDto dto) {
        entity.setFormat(dto.getFormat());
        entity.setIncludeHours(dto.getIncludeHours());
    }

    public static ReportDefinitionResponseDto toDto(ReportDefinition entity) {
        return ReportDefinitionResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .format(entity.getFormat())
                .includeHours(entity.isIncludeHours())
                .build();
    }
}