package ld.application.bridge;

import ld.domain.features.product.CreateProductUseCase;
import ld.domain.features.product.CreateProductUseCaseImpl;
import ld.domain.features.product.ProductChecker;
import ld.domain.features.product.ProductCreator;
import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.ProductEvent;
import ld.domain.features.product.photos.AddProductPhotosUseCase;
import ld.domain.features.product.photos.AddProductPhotosUseCaseImpl;
import ld.domain.features.product.photos.ProductPhotoStoragePort;
import ld.standard.lib.AggregateEventDispatcher;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.helper.test.InMemoryAggregateEventDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfiguration {

    @Bean
    public CreateProductUseCase createProductUseCase(ProductCreator productCreator,
                                                     ProductChecker productChecker,
                                                     AggregateEventDispatcher<ProductEvent> productEventAggregateEventDispatcher,
                                                     UnitOfWork unitOfWork) {
        return new CreateProductUseCaseImpl(productCreator, productChecker,
                productEventAggregateEventDispatcher, unitOfWork);
    }

    @Bean
    public AddProductPhotosUseCase addProductPhotosUseCase(ProductEditor productEditor,
                                                           ProductPhotoStoragePort productPhotoStoragePort,
                                                           UnitOfWork unitOfWork) {
        return new AddProductPhotosUseCaseImpl(productEditor, productPhotoStoragePort, unitOfWork);
    }

    @Bean
    AggregateEventDispatcher<ProductEvent> productEventAggregateEventDispatcher() {
        return new InMemoryAggregateEventDispatcher<>();
    }
}
