package ld.domain.features.product.photos;

import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.Product;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.validation.AddProductPhotosError;
import ld.domain.features.product.photos.validation.PhotoRule;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.util.List;
import java.util.UUID;

public class AddProductPhotosUseCaseImpl implements AddProductPhotosUseCase {

    private final ProductEditor productEditor;
    private final ProductPhotoStoragePort productPhotoStoragePort;
    private final BusinessGuard<AddProductPhotosCommand, AddProductPhotosError> addProductPhotosCommandBusinessGuard;
    private final UnitOfWork unitOfWork;

    public AddProductPhotosUseCaseImpl(
            ProductEditor productEditor,
            ProductPhotoStoragePort productPhotoStoragePort,
            UnitOfWork unitOfWork) {
        this.productEditor = productEditor;
        this.productPhotoStoragePort = productPhotoStoragePort;
        this.unitOfWork = unitOfWork;
        this.addProductPhotosCommandBusinessGuard = BusinessGuard.of(new PhotoRule());
    }

    @Override
    public Result<ProductPhotoSnapshot, AddProductPhotosError> execute(AddProductPhotosCommand command) {
        return this.addProductPhotosCommandBusinessGuard.validate(command)
                .andThen(_ -> this.findProduct(command.productId()))
                .map(product -> this.attachPhotos(product, command))
                .andThen(this::save);
    }

    private Result<Product, AddProductPhotosError> findProduct(UUID productId) {
        return this.productEditor.findById(productId)
                .map(snapshot -> Result.<Product, AddProductPhotosError>success(Product.from(snapshot)))
                .orElseGet(() -> Result.failure(new AddProductPhotosError.ProductNotFound(productId)));
    }

    private Product attachPhotos(Product product, AddProductPhotosCommand command) {
        product.addPhotos(this.storePhotos(command));
        return product;
    }

    private List<ProductPhoto> storePhotos(AddProductPhotosCommand command) {
        return command.photos().stream()
                .map(photo -> {
                    String storageKey = this.productPhotoStoragePort.store(
                            command.productId(), photo.fileName(), photo.content());
                    return ProductPhoto.create(UUID.randomUUID(), storageKey);
                })
                .toList();
    }

    private Result<ProductPhotoSnapshot, AddProductPhotosError> save(Product product) {
        return this.unitOfWork.executeInTransaction(() -> {
            var snapshot = product.toSnapshot();
            this.productEditor.save(snapshot);
            return Result.success(new ProductPhotoSnapshot(snapshot, product.getPhotos()));
        });
    }
}