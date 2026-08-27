package ld.application.api.resolver;

import ld.application.api.PhotoController;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.UUID;


@Component
public class LocalProductPhotoUrlResolver implements ProductPhotoUrlResolver {
    @Override
    public String resolve(UUID productId, String storageKey) {
        return MvcUriComponentsBuilder
                .fromMethodCall(MvcUriComponentsBuilder.on(PhotoController.class).get(productId, storageKey))
                .build()
                .toUriString();
    }
}