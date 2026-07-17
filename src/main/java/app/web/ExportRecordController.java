package app.web;

import app.model.ExportStatus;
import app.service.ExportRecordService;
import app.web.dto.ExportCreateRequestDto;
import app.web.dto.ExportResponseDto;
import app.web.dto.ExportUpdateRequestDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
public class ExportRecordController {
    private final ExportRecordService exportRecordService;

    @Autowired
    public ExportRecordController(ExportRecordService exportRecordService) {
        this.exportRecordService = exportRecordService;
    }

    @PostMapping
    public ResponseEntity<ExportResponseDto> create(@Valid @RequestBody ExportCreateRequestDto createDto) {
        ExportResponseDto created = exportRecordService.create(createDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ExportResponseDto> getById(@PathVariable UUID id,
                                                     @RequestParam UUID userId) {
        return ResponseEntity.ok(exportRecordService.getById(id, userId));
    }
    @GetMapping
    public ResponseEntity<List<ExportResponseDto>> getAllByUser(@RequestParam UUID userId) {
        return ResponseEntity.ok(exportRecordService.getAllByUserId(userId));
    }

    @GetMapping("/failed")
    public ResponseEntity<List<ExportResponseDto>> getFailedByUser(@RequestParam UUID userId) {
        return ResponseEntity.ok(exportRecordService.getFailedByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExportResponseDto> update(@PathVariable UUID id,
                                                    @Valid @RequestBody ExportUpdateRequestDto updateDto,
                                                    @RequestParam UUID userId) {
        return ResponseEntity.ok(exportRecordService.update(id, updateDto, userId));
    }

    @PutMapping("/{id}/retry")
    public ResponseEntity<ExportResponseDto> retry(@PathVariable UUID id,
                                                   @RequestParam ExportStatus status,
                                                   @RequestParam UUID userId) {
        return ResponseEntity.ok(exportRecordService.retry(id, status, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam UUID userId) {
        exportRecordService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
