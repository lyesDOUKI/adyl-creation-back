package ld.domain.features.product.photos;

import ld.domain.features.product.lifecycle.ProductEditor;
import ld.domain.features.product.model.Product;
import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.validation.PhotoRule;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.UnitOfWork;
import ld.standard.lib.validation.BusinessGuard;
import ld.standard.lib.validation.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductPhotosUseCaseImpl implements AddProductPhotosUseCase {

    private final ProductEditor productEditor;
    private final ProductPhotoStoragePort productPhotoStoragePort;
    private final BusinessGuard<AddProductPhotosCommand> addProductPhotosCommandBusinessGuard;
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
    public Result<ProductPhotoSnapshot> execute(AddProductPhotosCommand command) {
        return this.addProductPhotosCommandBusinessGuard.validate(command)
                .flatMap(_ -> this.productEditor.findById(command.productId())
                        .map(Result::success)
                        .orElseGet(() -> Result.resourceNotFound(
                                ProductErrorCode.PRODUCTS_NOT_FOUND,
                                "Produit introuvable",
                                String.format("Le produit %s est introuvable", command.productId())
                        ))
                        .flatMap(snapshot -> {
                            Product product = Product.from(snapshot);
                            return this.storePhotos(command)
                                    .flatMap(product::addPhotos)
                                    .map(_ -> product);
                        })
                        .flatMap(product -> this.unitOfWork.executeInTransaction(() -> {
                            // On sauvegarde l'agrégat Product complet via son port d'édition
                            this.productEditor.save(product.toSnapshot());

                            var productPhotoSnapshot = new ProductPhotoSnapshot(
                                    product.toSnapshot(),
                                    product.getPhotos()
                            );
                            return Result.success(productPhotoSnapshot);
                        }))
                );
    }

    private Result<List<ProductPhoto>> storePhotos(AddProductPhotosCommand command) {
        List<ProductPhoto> stored = new ArrayList<>();
        for (var photo : command.photos()) {
            String storageKey = this.productPhotoStoragePort.store(
                    command.productId(), photo.fileName(), photo.content()
            );
            stored.add(ProductPhoto.create(UUID.randomUUID(), storageKey));
        }
        return Result.success(stored);
    }
}