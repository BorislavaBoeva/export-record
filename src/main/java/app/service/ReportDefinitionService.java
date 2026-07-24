package app.service;

import app.exception.NullArgumentException;
import app.model.ReportDefinition;
import app.repository.ReportDefinitionRepository;
import app.web.dto.reportDefinition.ReportDefinitionResponseDto;
import app.web.dto.reportDefinition.ReportDefinitionUpsertRequestDto;
import app.web.mapper.reportDefinition.ReportDefinitionMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReportDefinitionService {
    private final ReportDefinitionRepository reportDefinitionRepository;

    @Autowired
    public ReportDefinitionService(ReportDefinitionRepository reportDefinitionRepository) {
        this.reportDefinitionRepository = reportDefinitionRepository;
    }

    public ReportDefinitionResponseDto upsert(ReportDefinitionUpsertRequestDto dto) {
        validateDto(dto);

        Optional<ReportDefinition> existing = reportDefinitionRepository.findByUserId(dto.getUserId());

        if (existing.isPresent()) {
            ReportDefinition entity = existing.get();
            ReportDefinitionMapper.updateEntityFromDto(entity, dto);
            ReportDefinition updated = reportDefinitionRepository.save(entity);
            return ReportDefinitionMapper.toDto(updated);
        }

        ReportDefinition entity = ReportDefinitionMapper.toEntity(dto);
        ReportDefinition saved = reportDefinitionRepository.save(entity);
        return ReportDefinitionMapper.toDto(saved);
    }

    public List<ReportDefinitionResponseDto> getAll() {
        return reportDefinitionRepository.findAll()
                .stream()
                .map(ReportDefinitionMapper::toDto)
                .toList();
    }

    private void validateDto(ReportDefinitionUpsertRequestDto dto) {
        if (dto == null) {
            throw new NullArgumentException("Report definition request cannot be null");
        }
    }
}