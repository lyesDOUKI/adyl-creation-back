package ld.bootstrap.api.hal.conf;

import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@NullMarked
@Component
class RelativeLinksProcessor implements RepresentationModelProcessor<RepresentationModel<?>> {

    @Override
    public RepresentationModel<?> process(RepresentationModel<?> model) {
        List<Link> relative = model.getLinks().stream()
                .map(l -> l.isTemplated()
                        ? l
                        : l.withHref(toRelative(l.getHref())))
                .toList();
        model.removeLinks();
        model.add(relative);
        return model;
    }

    private static String toRelative(String href) {
        URI uri = URI.create(href);
        return uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
    }
}