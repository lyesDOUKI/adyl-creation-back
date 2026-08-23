package ld.application.api.resolver;

import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class LocalProductPhotoUrlResolver implements ProductPhotoUrlResolver {

    @Value("${app.base-url}:http://localhost:8080")
    private final String appBaseUrl;

    public LocalProductPhotoUrlResolver(String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    public String resolve(String storageKey) {
        return "%s/photos/%s".formatted(this.appBaseUrl, storageKey);
    }
}