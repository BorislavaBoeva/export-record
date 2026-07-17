package app.repository;

import app.model.ExportRecord;
import app.model.ExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExportRecordRepository extends JpaRepository<ExportRecord, UUID> {
    Optional<ExportRecord> findByFileName(String fileName);
    List<ExportRecord> findAllByUserIdAndDeletedFalse(UUID userId);
    List<ExportRecord> findAllByUserIdAndExportStatusAndDeletedFalse(UUID userId, ExportStatus status);
}
