package ld.application.api.resolver;

import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class LocalProductPhotoUrlResolver implements ProductPhotoUrlResolver {

    private final String appBaseUrl;

    public LocalProductPhotoUrlResolver(@Value("${app.base-url:http://localhost:8080}") String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public String resolve(UUID productId, String storageKey) {
        return "%s/%s/photos/%s".formatted(this.appBaseUrl, productId, storageKey);
    }
}