package ld.domain.features.product.photos;

import ld.domain.features.product.model.Product;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.validation.PhotoRule;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductPhotosUseCaseImpl implements AddProductPhotosUseCase {

    private final AddProductPhotosRepository addProductPhotosRepository;
    private final ProductPhotoStoragePort productPhotoStoragePort;
    private final BusinessGuard<AddProductPhotosCommand> addProductPhotosCommandBusinessGuard;

    public AddProductPhotosUseCaseImpl(AddProductPhotosRepository addProductPhotosRepository, ProductPhotoStoragePort productPhotoStoragePort) {
        this.addProductPhotosRepository = addProductPhotosRepository;
        this.productPhotoStoragePort = productPhotoStoragePort;
        this.addProductPhotosCommandBusinessGuard = BusinessGuard.of(new PhotoRule());
    }

    @Override
    public Result<ProductPhotoSnapshot> execute(AddProductPhotosCommand command) {
        return this.addProductPhotosCommandBusinessGuard.validate(command)
                .flatMap(_ -> this.addProductPhotosRepository.findById(command.productId())
                        .map(Result::success)
                        .orElseGet(() -> Result.resourceNotFound(ProductErrorCode.PRODUCTS_NOT_FOUND, "Produit introuvable",
                                        String.format("Le produit %s est introuvable", command.productId())))
                                .flatMap(snapshot -> {
                                    Product product = Product.from(snapshot);
                                    return this.storePhotos(command).flatMap(product::addPhotos).map(_ -> product);
                                })
                                .map(product -> {
                                    var productPhotoSnapshot = new ProductPhotoSnapshot(product.toSnapshot(),
                                            product.getPhotos());
                                    this.addProductPhotosRepository.execute(
                                            productPhotoSnapshot);
                                            return productPhotoSnapshot;
                                        }
                                )
                );
    }

    private Result<List<ProductPhoto>> storePhotos(AddProductPhotosCommand command) {
        List<ProductPhoto> stored = new ArrayList<>();
        int position = 0;
        for (var photo : command.photos()) {
            String storageKey = this.productPhotoStoragePort.store(
                    command.productId(), photo.fileName(), photo.content()
            );
            stored.add(new ProductPhoto(UUID.randomUUID(), storageKey, position));
            position++;
        }
        return Result.success(stored);
    }
}
