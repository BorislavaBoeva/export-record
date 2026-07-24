package app.repository;

import app.model.ExportRecord;
import app.model.ExportStatus;
import app.model.ExportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExportRecordRepository extends JpaRepository<ExportRecord, UUID> {
    List<ExportRecord> findAllByUserIdAndDeletedFalse(UUID userId);
    List<ExportRecord> findAllByUserIdAndExportStatusAndDeletedFalse(UUID userId, ExportStatus status);

    boolean existsByUserIdAndExportTypeAndDeletedFalseAndExportDateAfter(
                                                            UUID userId,
                                                            ExportType exportType,
                                                            LocalDateTime after);

    List<ExportRecord> findAllByDeletedTrueAndUpdatedOnBefore(LocalDateTime threshold);

}