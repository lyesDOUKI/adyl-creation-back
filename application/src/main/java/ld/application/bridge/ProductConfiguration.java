package ld.application.bridge;

import ld.domain.features.product.CreateProductRepository;
import ld.domain.features.product.CreateProductUseCase;
import ld.domain.features.product.CreateProductUseCaseImpl;
import ld.domain.features.product.photos.AddProductPhotosRepository;
import ld.domain.features.product.photos.AddProductPhotosUseCase;
import ld.domain.features.product.photos.AddProductPhotosUseCaseImpl;
import ld.domain.features.product.photos.ProductPhotoStoragePort;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfiguration {

    @Bean
    public CreateProductUseCase createProductUseCase(CreateProductRepository createProductRepository) {
        return new CreateProductUseCaseImpl(createProductRepository, new InMemoryAggregateEventDispatcher<>());
    }

    @Bean
    public AddProductPhotosUseCase addProductPhotosUseCase(AddProductPhotosRepository addProductPhotosRepository,
                                                           ProductPhotoStoragePort productPhotoStoragePort) {
        return new AddProductPhotosUseCaseImpl(addProductPhotosRepository, productPhotoStoragePort);
    }
}
