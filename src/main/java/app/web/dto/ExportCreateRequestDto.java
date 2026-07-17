package app.web.dto;

import app.model.ExportStatus;
import app.model.ExportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportCreateRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "File name is required")
    @Size(min = 3, max = 255, message = "File name must be between 3 and 255 characters")
    private String fileName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Export type is required")
    private ExportType exportType;

    @NotNull(message = "Export status is required")
    private ExportStatus exportStatus;
}
