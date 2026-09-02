package ld.application.infra.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(
        String from,
        Admin admin,
        Map<String, Template> templates,
        boolean enabled
) {

    public record Admin(
            String address
    ) {
    }

    public record Template(
            String subject,
            String template
    ) {
    }
}