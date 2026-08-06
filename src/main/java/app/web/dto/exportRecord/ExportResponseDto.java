package app.web.dto.exportRecord;

import app.model.ExportStatus;
import app.model.ExportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponseDto {
    private UUID id;
    private ExportType exportType;
    private String fileName;
    private LocalDateTime exportDate;
    private String description;
    private UUID userId;
    private LocalDateTime updatedOn;
    private ExportStatus exportStatus;
    private boolean deleted;
}