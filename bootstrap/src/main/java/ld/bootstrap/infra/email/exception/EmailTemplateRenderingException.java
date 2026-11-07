package ld.bootstrap.infra.email.exception;

public class EmailTemplateRenderingException
        extends RuntimeException {
    public EmailTemplateRenderingException(Throwable cause) {
        super("Failed to render email template", cause);
    }
}
