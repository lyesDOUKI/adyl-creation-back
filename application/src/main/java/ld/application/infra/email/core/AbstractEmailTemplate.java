package ld.application.infra.email.core;

import ld.application.infra.email.config.EmailProperties;

public abstract class AbstractEmailTemplate<E, M> {

    private final EmailProperties properties;
    private final EmailTemplateRenderer renderer;

    protected AbstractEmailTemplate(
            EmailProperties properties,
            EmailTemplateRenderer renderer
    ) {
        this.properties = properties;
        this.renderer = renderer;
    }

    public Email create(E event) {

        var template = properties.templates()
                .get(templateName());

        var model = createModel(event);

        var subject = renderer.renderString(
                template.subject(),
                model
        );

        var content = renderer.render(
                template.template(),
                model
        );

        return new Email(
                properties.admin().address(),
                subject,
                content
        );
    }

    protected abstract String templateName();

    protected abstract M createModel(E event);
}
