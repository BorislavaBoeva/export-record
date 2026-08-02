package app.scheduler;

import app.service.ExportRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@EnableScheduling
@Component
public class ExportRecordCleanupJob {
    private final ExportRecordService exportRecordService;

    @Autowired
    public ExportRecordCleanupJob(ExportRecordService exportRecordService) {
        this.exportRecordService = exportRecordService;
    }
    @Scheduled(fixedDelay = 2592000000L)
    public void purgeOldSoftDeletedRecords() {
        log.info("Export record cleanup job started");
        exportRecordService.purgeExpiredSoftDeleted();
    }
}
