package ld.bootstrap.infra.email.core;

public record Email(
        String recipient,
        String subject,
        String content
) {
}
