package java.lang;

public class IllegalArgumentException extends Exception {
    public IllegalArgumentException(String message) {
        super(message);
    }

    public IllegalArgumentException(Throwable cause) {
        super(cause);
    }
}
