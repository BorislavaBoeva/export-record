package app.web.dto.reportDefinition;

import app.model.ExportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDefinitionUpsertRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Format is required")
    private ExportType format;

    @NotNull(message = "Include hours flag is required")
    private Boolean includeHours;
}
