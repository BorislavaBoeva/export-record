package app.web.dto.reportDefinition;

import app.model.ExportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinitionResponseDto {
    private UUID id;
    private UUID userId;
    private ExportType format;
    private boolean includeHours;
  }