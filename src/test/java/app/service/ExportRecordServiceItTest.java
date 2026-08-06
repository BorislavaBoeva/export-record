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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static app.util.ExportTestFactory.getExportCreateRequestDto;
import static app.util.ExportTestFactory.getExportUpdateRequestDto;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@SpringBootTest
public class ExportRecordServiceItTest {
    @Autowired
    private ExportRecordService underTest;
    @Autowired
    private ExportRecordRepository exportRecordRepository;

    @Test
    public void testCreate_shouldPersistExportRecord() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto result = underTest.create(request);
        ExportRecord record = exportRecordRepository.findById(result.getId()).orElseThrow();

        assertEquals(request.getUserId(), record.getUserId());
        assertEquals(request.getFileName(), record.getFileName());
        assertEquals(request.getDescription(), record.getDescription());
        assertEquals(request.getExportType(), record.getExportType());
        assertEquals(request.getExportStatus(), record.getExportStatus());
        assertFalse(record.isDeleted());
        assertNotNull(record.getExportDate());
    }

    @Test
    public void testCreate_whenDuplicateWithin5Seconds_throwsDuplicateExportException() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        underTest.create(request);
        assertThrows(DuplicateExportException.class, () -> underTest.create(request));
    }

    @Test
    public void testGetById_whenOwnedRecord_returnsDto() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        ExportResponseDto result = underTest.getById(created.getId(), request.getUserId());

        assertEquals(created.getId(), result.getId());
        assertEquals(request.getFileName(), result.getFileName());
    }

    @Test
    public void testGetById_whenRecordNotFound_throwsEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class,
                () -> underTest.getById(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    public void testGetById_whenDifferentUser_throwsEntityNotFoundException() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        assertThrows(EntityNotFoundException.class,
                () -> underTest.getById(created.getId(), UUID.randomUUID()));
    }

    @Test
    public void testGetById_whenDeleted_throwsEntityNotFoundException() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        underTest.delete(created.getId(), request.getUserId());

        assertThrows(EntityNotFoundException.class,
                () -> underTest.getById(created.getId(), request.getUserId()));
    }

    @Test
    public void testGetHistory_returnsNonDeletedForUser() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        List<ExportResponseDto> history = underTest.getHistory(request.getUserId());

        assertEquals(1, history.size());
        assertEquals(created.getId(), history.getFirst().getId());
    }

    @Test
    public void testGetFailedByUserId_returnsOnlyFailed() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        request.setExportStatus(ExportStatus.FAILED);
        ExportResponseDto created = underTest.create(request);
        List<ExportResponseDto> failed = underTest.getFailedByUserId(request.getUserId());

        assertEquals(1, failed.size());
        assertEquals(created.getId(), failed.getFirst().getId());
        assertEquals(ExportStatus.FAILED, failed.getFirst().getExportStatus());
    }

    @Test
    public void testUpdate_whenValid_updatesAndPersists() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        ExportUpdateRequestDto updateDto = getExportUpdateRequestDto();
        ExportResponseDto result = underTest.update(created.getId(), updateDto, request.getUserId());

        assertEquals(updateDto.getFileName(), result.getFileName());
        assertEquals(updateDto.getDescription(), result.getDescription());
        assertEquals(updateDto.getExportType(), result.getExportType());

        ExportRecord record = exportRecordRepository.findById(created.getId()).orElseThrow();
        assertEquals(updateDto.getFileName(), record.getFileName());
    }

    @Test
    public void testRetry_updatesStatus() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        request.setExportStatus(ExportStatus.FAILED);
        ExportResponseDto created = underTest.create(request);
        underTest.retry(created.getId(), ExportStatus.SUCCEEDED, request.getUserId());

        ExportRecord record = exportRecordRepository.findById(created.getId()).orElseThrow();
        assertEquals(ExportStatus.SUCCEEDED, record.getExportStatus());
    }

    @Test
    public void testDelete_softDeletesRecord() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        underTest.delete(created.getId(), request.getUserId());

        ExportRecord record = exportRecordRepository.findById(created.getId()).orElseThrow();
        assertTrue(record.isDeleted());
    }

    @Test
    public void testPurgeExpiredSoftDeleted_doesNotDeleteRecentlyDeleted() {
        ExportCreateRequestDto request = getExportCreateRequestDto();
        ExportResponseDto created = underTest.create(request);
        underTest.delete(created.getId(), request.getUserId());
        underTest.purgeExpiredSoftDeleted();

        assertTrue(exportRecordRepository.findById(created.getId()).isPresent());
    }

    @Test
    public void testCreate_whenNullDto_throwsNullArgumentException() {
        assertThrows(NullArgumentException.class, () -> underTest.create(null));
    }

    @Test
    public void testGetById_whenNullUserId_throwsNullArgumentException() {
        assertThrows(NullArgumentException.class, () -> underTest.getById(UUID.randomUUID(), null));
    }

    @Test
    public void testGetHistory_whenNullUserId_throwsNullArgumentException() {
        assertThrows(NullArgumentException.class, () -> underTest.getHistory(null));
    }

    @Test
    public void testUpdate_whenNullDto_throwsNullArgumentException() {
        assertThrows(NullArgumentException.class, () -> underTest.update(UUID.randomUUID(), null, UUID.randomUUID()));
    }

    @Test
    public void testRetry_whenNullStatus_throwsNullArgumentException() {
        assertThrows(NullArgumentException.class, () -> underTest.retry(UUID.randomUUID(), null, UUID.randomUUID()));
    }
}