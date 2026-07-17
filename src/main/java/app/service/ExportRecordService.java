package app.service;

import app.exception.ExportRecordNotFoundException;
import app.model.ExportRecord;
import app.model.ExportStatus;
import app.repository.ExportRecordRepository;
import app.web.dto.ExportCreateRequestDto;
import app.web.dto.ExportResponseDto;
import app.web.dto.ExportUpdateRequestDto;
import app.web.mapper.ExportRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExportRecordService {
    private final ExportRecordRepository exportRepository;

    @Autowired
    public ExportRecordService(ExportRecordRepository exportRecordRepository) {
        this.exportRepository = exportRecordRepository;
    }
    public ExportResponseDto create(ExportCreateRequestDto createDto) {
        //1.DTO → Entity
        ExportRecord record = ExportRecordMapper.toEntity(createDto);

        //2.Set system-managed fields
        record.setExportDate(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());
        record.setDeleted(false);
        // exportStatus вече идва от createDto през toEntity(), не се override-ва тук

        //3.Entity → DB
        ExportRecord saved = exportRepository.save(record);

        //4.Return the created record Entity → DTO
        return ExportRecordMapper.toDto(saved);
    }
    //Прието и за двете. Ето финалната версия на getById:
   public ExportResponseDto getById(UUID id, UUID requestingUserId) {
        ExportRecord record = exportRepository.findById(id)
                .orElseThrow(() -> new ExportRecordNotFoundException("Export record not found"));

        // Soft-deleted records are treated as non-existent
        if (record.isDeleted()) {
            throw new ExportRecordNotFoundException("Export record not found");
        }

        // Return 404, not 403, to avoid confirming the record's existence to a non-owner
        if (!record.getUserId().equals(requestingUserId)) {
            throw new ExportRecordNotFoundException("Export record not found");
        }

        return ExportRecordMapper.toDto(record);
    }

    public List<ExportResponseDto> getAllByUserId(UUID userId) {
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

    public ExportResponseDto retry(UUID id, ExportStatus newStatus, UUID requestingUserId) {
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

        ExportRecord saved = exportRepository.save(record);
        return ExportRecordMapper.toDto(saved);
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
