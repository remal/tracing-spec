package utils.okhttp;

public class ErroneousResponseException extends RuntimeException {

    ErroneousResponseException(String message) {
        super(message);
    }

    ErroneousResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
