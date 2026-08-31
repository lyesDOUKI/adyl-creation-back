package ld.application.read;

import ld.application.infra.db.jooq.GetProductQueryRepository;
import ld.application.infra.db.jooq.ProductQuery;
import ld.application.response.GetProductResponse;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.validation.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class GetProductServiceImpl implements GetProductService {

    private final GetProductQueryRepository repository;
    private final ProductPhotoUrlResolver photoUrlResolver;

    public GetProductServiceImpl(
            GetProductQueryRepository repository,
            ProductPhotoUrlResolver photoUrlResolver
    ) {
        this.repository = repository;
        this.photoUrlResolver = photoUrlResolver;
    }

    @Override
    public Result<GetProductResponse> findById(UUID productId) {

        return repository.findById(productId)
                .map(this::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.resourceNotFound(
                        ProductErrorCode.PRODUCTS_NOT_FOUND,
                        "Produit introuvable",
                        String.format("Le produit %s est introuvable", productId)
                ));
    }

    @Override
    public Page<GetProductResponse> findAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    private GetProductResponse toResponse(ProductQuery product) {

        List<String> photosUri = product.photoStorageKeys()
                .stream()
                .map(storageKey ->
                        photoUrlResolver.resolve(
                                product.productId(),
                                storageKey
                        )
                )
                .toList();

        return new GetProductResponse(
                product.productId(),
                product.name(),
                product.unitPrice(),
                product.colors(),
                photosUri,
                product.numberOfOrders()
        );
    }
}