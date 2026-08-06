package app.web.dto.reportDefinition;

import app.model.ExportType;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinitionResponseDto {
    private UUID id;
    private UUID userId;
    private ExportType format;
    private boolean includeHours;
  }