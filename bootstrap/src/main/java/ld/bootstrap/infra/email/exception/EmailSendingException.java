package ld.bootstrap.infra.email.exception;

public class EmailSendingException
        extends RuntimeException {

    public EmailSendingException(
            Throwable cause
    ) {
        super("Failed to send email", cause);
    }
}
