package app.web.mapper.exportRecord;

import app.model.ExportRecord;
import app.web.dto.exportRecord.ExportCreateRequestDto;
import app.web.dto.exportRecord.ExportResponseDto;
import app.web.dto.exportRecord.ExportUpdateRequestDto;
import org.junit.jupiter.api.Test;

import static app.util.ExportTestFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExportRecordMapperTest {
    @Test
    public void testToEntity_mapsAllFields() {
        ExportCreateRequestDto dto = getExportCreateRequestDto();

        ExportRecord result = ExportRecordMapper.toEntity(dto);
        assertEquals(dto.getUserId(), result.getUserId());
        assertEquals(dto.getFileName(), result.getFileName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getExportType(), result.getExportType());
        assertEquals(dto.getExportStatus(), result.getExportStatus());
    }

    @Test
    public void testToDto_mapsAllFields() {
        ExportRecord entity = getExportRecord();

        ExportResponseDto result = ExportRecordMapper.toDto(entity);
        assertEquals(entity.getId(), result.getId());
        assertEquals(entity.getExportType(), result.getExportType());
        assertEquals(entity.getFileName(), result.getFileName());
        assertEquals(entity.getExportDate(), result.getExportDate());
        assertEquals(entity.getDescription(), result.getDescription());
        assertEquals(entity.getUserId(), result.getUserId());
        assertEquals(entity.getUpdatedOn(), result.getUpdatedOn());
        assertEquals(entity.getExportStatus(), result.getExportStatus());
        assertEquals(entity.isDeleted(), result.isDeleted());
    }

    @Test
    public void testUpdateEntityFromDto_updatesFields() {
        ExportRecord entity = getExportRecord();
        ExportUpdateRequestDto dto = getExportUpdateRequestDto();

        ExportRecordMapper.updateEntityFromDto(entity, dto);
        assertEquals(dto.getFileName(), entity.getFileName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getExportType(), entity.getExportType());
    }
}