package app.exception;

public class ExportRecordNotFoundException extends ApplicationException {
    public ExportRecordNotFoundException(String exportRecordNotFound) {
        super(exportRecordNotFound);
    }
}
