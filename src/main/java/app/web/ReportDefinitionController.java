package app.web;

import app.service.ReportDefinitionService;
import app.web.dto.reportDefinition.ReportDefinitionResponseDto;
import app.web.dto.reportDefinition.ReportDefinitionUpsertRequestDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/reportDefinition")
public class ReportDefinitionController {
    private final ReportDefinitionService reportDefinitionService;

    @Autowired
    public ReportDefinitionController(ReportDefinitionService reportDefinitionService) {
        this.reportDefinitionService = reportDefinitionService;
    }

    @PostMapping
    public ResponseEntity<ReportDefinitionResponseDto> upsert(@Valid @RequestBody ReportDefinitionUpsertRequestDto dto) {
        ReportDefinitionResponseDto result = reportDefinitionService.upsert(dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping
    public ResponseEntity<List<ReportDefinitionResponseDto>> getAll() {
        return ResponseEntity.ok(reportDefinitionService.getAll());
    }
}
