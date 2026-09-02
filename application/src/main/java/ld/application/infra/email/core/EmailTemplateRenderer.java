package ld.application.infra.email.core;

public interface EmailTemplateRenderer {
    String render(String templateName, Object model);
    String renderString(String content, Object model);
}
