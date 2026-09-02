package ld.application.infra.email.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ld.application.infra.email.exception.EmailTemplateRenderingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

@Component
public class FreeMarkerEmailTemplateRenderer
        implements EmailTemplateRenderer {

    public static final String INLINE = "inline";
    private final Configuration configuration;

    public FreeMarkerEmailTemplateRenderer(
            Configuration configuration
    ) {
        this.configuration = configuration;
    }

    @Override
    public String render(
            String templateName,
            Object model
    ) {
        try {
            var template = configuration.getTemplate(templateName);
            var writer = new StringWriter();
            template.process(toTemplateModel(model), writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new EmailTemplateRenderingException(e);
        }
    }

    @Override
    public String renderString(
            String content,
            Object model
    ) {
        try {
            var template = new Template(INLINE, new StringReader(content), configuration);
            var writer = new StringWriter();
            template.process(toTemplateModel(model), writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new EmailTemplateRenderingException(e);
        }
    }

    private Object toTemplateModel(Object model) {
        if (model == null || !model.getClass().isRecord()) {
            return model;
        }
        var result = new HashMap<String, Object>();
        for (var component : model.getClass().getRecordComponents()) {
            try {
                result.put(component.getName(), component.getAccessor().invoke(model));
            } catch (ReflectiveOperationException e) {
                throw new EmailTemplateRenderingException(e);
            }
        }
        return result;
    }
}