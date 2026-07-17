package app.exception;

public class UnauthorizedActionException extends ApplicationException{
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
