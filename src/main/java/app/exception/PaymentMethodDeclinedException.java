package app.exception;

public class PaymentMethodDeclinedException extends RuntimeException {
    public PaymentMethodDeclinedException(String message) {
        super(message);
    }
}
