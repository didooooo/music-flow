package app.exception;

public class NotificationUpdateFailedException extends RuntimeException {
    public NotificationUpdateFailedException(String message) {
        super(message);
    }
}
