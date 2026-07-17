package app.exception;

public class EntityNotFoundException extends ApplicationException{
    public EntityNotFoundException(String message) {
        super(message);
    }
}
