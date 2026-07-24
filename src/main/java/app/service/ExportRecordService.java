package app.service;

import app.exception.DuplicateExportException;
import app.exception.EntityNotFoundException;
import app.exception.NullArgumentException;
import app.model.ExportRecord;
import app.model.ExportStatus;
import app.repository.ExportRecordRepository;
import app.web.dto.exportRecord.ExportCreateRequestDto;
import app.web.dto.exportRecord.ExportResponseDto;
import app.web.dto.exportRecord.ExportUpdateRequestDto;
import app.web.mapper.exportRecord.ExportRecordMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExportRecordService {
    private final ExportRecordRepository exportRepository;

    @Autowired
    public ExportRecordService(ExportRecordRepository exportRecordRepository) {
        this.exportRepository = exportRecordRepository;
    }

    public ExportResponseDto create(ExportCreateRequestDto createDto) {
        validateCreateDto(createDto);
        boolean recentDuplicateExists = exportRepository
                .existsByUserIdAndExportTypeAndDeletedFalseAndExportDateAfter(
                        createDto.getUserId(),
                        createDto.getExportType(),
                        LocalDateTime.now().minusSeconds(5)
                );
        if (recentDuplicateExists) {
            throw new DuplicateExportException("An export request was already submitted moments ago");
        }
        ExportRecord record = ExportRecordMapper.toEntity(createDto);
        record.setExportDate(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());
        record.setDeleted(false);
        ExportRecord saved = exportRepository.save(record);
        return ExportRecordMapper.toDto(saved);
    }

    public ExportResponseDto getById(UUID id, UUID requestingUserId) {
        ExportRecord record = findOwnedRecordOrThrow(id, requestingUserId);
        return ExportRecordMapper.toDto(record);
    }

    public List<ExportResponseDto> getHistory(UUID userId) {
        validateUserId(userId);
        return exportRepository.findAllByUserIdAndDeletedFalse(userId)
                .stream()
                .map(ExportRecordMapper::toDto)
                .toList();
    }

    public ExportResponseDto update(UUID id, ExportUpdateRequestDto updateDto, UUID requestingUserId) {
        validateUpdateDto(updateDto);
        ExportRecord record = findOwnedRecordOrThrow(id, requestingUserId);
        ExportRecordMapper.updateEntityFromDto(record, updateDto);
        record.setUpdatedOn(LocalDateTime.now());
        ExportRecord saved = exportRepository.save(record);
        return ExportRecordMapper.toDto(saved);
    }

    public List<ExportResponseDto> getFailedByUserId(UUID userId) {
        validateUserId(userId);
        return exportRepository.findAllByUserIdAndExportStatusAndDeletedFalse(userId, ExportStatus.FAILED)
                .stream()
                .map(ExportRecordMapper::toDto)
                .toList();
    }

    public void retry(UUID id, ExportStatus newStatus, UUID requestingUserId) {
        validateStatus(newStatus);
        ExportRecord record = findOwnedRecordOrThrow(id, requestingUserId);
        record.setExportStatus(newStatus);
        record.setUpdatedOn(LocalDateTime.now());
        exportRepository.save(record);
    }

    public void delete(UUID id, UUID requestingUserId) {
        ExportRecord record = findOwnedRecordOrThrow(id, requestingUserId);
        record.setDeleted(true);
        record.setUpdatedOn(LocalDateTime.now());
        exportRepository.save(record);
    }

    public void purgeExpiredSoftDeleted() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<ExportRecord> expired = exportRepository.findAllByDeletedTrueAndUpdatedOnBefore(threshold);
        exportRepository.deleteAll(expired);
    }

    private ExportRecord findOwnedRecordOrThrow(UUID id, UUID requestingUserId) {
        validateId(id);
        validateUserId(requestingUserId);

        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Export record not found"));

        if (record.isDeleted()) {
            throw new EntityNotFoundException("Export record not found");
        }
        // Return 404, not 403, to avoid confirming the record's existence to a non-owner
        if (!record.getUserId().equals(requestingUserId)) {
            throw new EntityNotFoundException("Export record not found");
        }
        return record;
    }

    private void validateId(UUID id) {
        if (id == null) {
            throw new NullArgumentException("Export record ID is required");
        }
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new NullArgumentException("User ID is required");
        }
    }

    private void validateStatus(ExportStatus status) {
        if (status == null) {
            throw new NullArgumentException("Export status is required");
        }
    }

    private void validateCreateDto(ExportCreateRequestDto dto) {
        if (dto == null) {
            throw new NullArgumentException("Export create request is required");
        }
    }

    private void validateUpdateDto(ExportUpdateRequestDto dto) {
        if (dto == null) {
            throw new NullArgumentException("Export update request is required");
        }
    }
}