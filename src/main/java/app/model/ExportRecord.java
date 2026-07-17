package app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "export_record")
public class ExportRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExportType exportType;

    @Column(nullable = false)
    private String fileName;

    @Column(name = "export_date")
    private LocalDateTime exportDate;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private UUID userId;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExportStatus exportStatus;

    private boolean deleted;
}
