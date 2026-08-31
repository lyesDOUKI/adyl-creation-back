package ld.application.read;

import ld.application.infra.db.read.GetProductQueryRepository;
import ld.application.infra.db.read.ProductQuery;
import ld.application.response.GetProductResponse;
import ld.domain.features.product.photos.ProductPhotoUrlResolver;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.validation.FailureType;
import ld.standard.lib.validation.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static ld.application.shared.ProductQueryTestBuilder.aProduct;
import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class GetProductEntityServiceImplTest {

    private final FakeGetProductQueryRepository repository = new FakeGetProductQueryRepository();
    private final FakeProductPhotoUrlResolver photoUrlResolver = new FakeProductPhotoUrlResolver();
    private final GetProductServiceImpl service =
            new GetProductServiceImpl(repository, photoUrlResolver);

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    @Test
    void findById_returns_success_with_mapped_fields_when_product_exists() {
        repository.add(aProduct()
                .withProductId(productId)
                .withName("T-shirt")
                .withPrice(BigDecimal.valueOf(19.90))
                .withColors("rouge", "bleu")
                .withPhotos("photo1.jpg", "photo2.jpg")
                .withNumberOfOrders(3)
                .build());

        Result<GetProductResponse> result = service.findById(productId);

        assertSuccess(result);
        GetProductResponse response = extractValue(result);
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.name()).isEqualTo("T-shirt");
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(19.90));
        assertThat(response.colors()).containsExactly("rouge", "bleu");
        assertThat(response.numberOfOrders()).isEqualTo(3);
    }

    @Test
    void findById_resolves_each_photo_url_via_the_resolver() {
        repository.add(aProduct()
                .withProductId(productId)
                .withName("T-shirt")
                .withPrice(BigDecimal.TEN)
                .withPhotos("photo1.jpg", "photo2.jpg")
                .build());

        Result<GetProductResponse> result = service.findById(productId);

        assertSuccess(result);
        GetProductResponse response = extractValue(result);
        assertThat(response.photosUri()).containsExactly(
                "https://fake-cdn/" + productId + "/photo1.jpg",
                "https://fake-cdn/" + productId + "/photo2.jpg"
        );
    }

    @Test
    void findById_returns_empty_photo_list_when_product_has_no_photos() {
        repository.add(aProduct()
                .withProductId(productId)
                .withName("Mug")
                .withPrice(BigDecimal.ONE)
                .build());

        Result<GetProductResponse> result = service.findById(productId);

        assertSuccess(result);
        assertThat(extractValue(result).photosUri()).isEmpty();
    }

    @Test
    void findById_returns_resource_not_found_when_product_does_not_exist() {
        Result<GetProductResponse> result = service.findById(productId);

        assertFailure(result, FailureType.RESOURCE_NOT_FOUND, ProductErrorCode.PRODUCTS_NOT_FOUND);
    }

    @Test
    void findAll_maps_every_element_of_the_page_and_preserves_paging_metadata() {
        UUID idA = UUID.randomUUID();
        UUID idB = UUID.randomUUID();
        repository.add(aProduct()
                .withProductId(idA)
                .withName("A")
                .withPrice(BigDecimal.ONE)
                .build());
        repository.add(aProduct()
                .withProductId(idB)
                .withName("B")
                .withPrice(BigDecimal.TEN)
                .withPhotos("p.jpg")
                .withNumberOfOrders(5)
                .build());

        Pageable pageable = PageRequest.of(0, 10);
        Page<GetProductResponse> page = service.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(GetProductResponse::name)
                .containsExactly("A", "B");
        assertThat(page.getContent().get(1).photosUri())
                .containsExactly("https://fake-cdn/" + idB + "/p.jpg");
    }

    @Test
    void findAll_returns_empty_page_when_repository_is_empty() {
        Page<GetProductResponse> page = service.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }


    static class FakeGetProductQueryRepository implements GetProductQueryRepository {

        private final Map<UUID, ProductQuery> data = new LinkedHashMap<>();

        void add(ProductQuery product) {
            data.put(product.productId(), product);
        }

        @Override
        public Optional<ProductQuery> findById(UUID productId) {
            return Optional.ofNullable(data.get(productId));
        }

        @Override
        public Page<ProductQuery> findAll(Pageable pageable) {
            List<ProductQuery> all = new ArrayList<>(data.values());
            int start = Math.min((int) pageable.getOffset(), all.size());
            int end = Math.min(start + pageable.getPageSize(), all.size());
            return new PageImpl<>(all.subList(start, end), pageable, all.size());
        }
    }

    static class FakeProductPhotoUrlResolver implements ProductPhotoUrlResolver {
        @Override
        public String resolve(UUID productId, String storageKey) {
            return "https://fake-cdn/" + productId + "/" + storageKey;
        }
    }
}