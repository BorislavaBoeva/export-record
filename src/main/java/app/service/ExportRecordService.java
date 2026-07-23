package app.service;

import app.exception.DuplicateExportException;
import app.exception.ExportRecordNotFoundException;
import app.model.ExportRecord;
import app.model.ExportStatus;
import app.repository.ExportRecordRepository;
import app.web.dto.ExportCreateRequestDto;
import app.web.dto.ExportResponseDto;
import app.web.dto.ExportUpdateRequestDto;
import app.web.mapper.ExportRecordMapper;
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
        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new ExportRecordNotFoundException("Export record not found"));
        if (record.isDeleted()) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        // Return 404, not 403, to avoid confirming the record's existence to a non-owner
        if (!record.getUserId().equals(requestingUserId)) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        return ExportRecordMapper.toDto(record);
    }

    public List<ExportResponseDto> getHistory(UUID userId) {
        return exportRepository.findAllByUserIdAndDeletedFalse(userId)
                .stream()
                .map(ExportRecordMapper::toDto)
                .toList();
    }

    public ExportResponseDto update(UUID id, ExportUpdateRequestDto updateDto, UUID requestingUserId) {
        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new ExportRecordNotFoundException("Export record not found"));
        if (record.isDeleted()) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        if (!record.getUserId().equals(requestingUserId)) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        ExportRecordMapper.updateEntityFromDto(record, updateDto);
        record.setUpdatedOn(LocalDateTime.now());

        ExportRecord saved = exportRepository.save(record);
        return ExportRecordMapper.toDto(saved);
    }

    public List<ExportResponseDto> getFailedByUserId(UUID userId) {
        return exportRepository.findAllByUserIdAndExportStatusAndDeletedFalse(userId, ExportStatus.FAILED)
                .stream()
                .map(ExportRecordMapper::toDto)
                .toList();
    }

    public void retry(UUID id, ExportStatus newStatus, UUID requestingUserId) {
        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new ExportRecordNotFoundException("Export record not found"));
        if (record.isDeleted()) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        if (!record.getUserId().equals(requestingUserId)) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        record.setExportStatus(newStatus);
        record.setUpdatedOn(LocalDateTime.now());
        exportRepository.save(record);
    }

    public void delete(UUID id, UUID requestingUserId) {
        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new ExportRecordNotFoundException("Export record not found"));
        if (record.isDeleted()) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        if (!record.getUserId().equals(requestingUserId)) {
            throw new ExportRecordNotFoundException("Export record not found");
        }
        record.setDeleted(true);
        record.setUpdatedOn(LocalDateTime.now());
        exportRepository.save(record);
    }
}