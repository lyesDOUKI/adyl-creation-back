package ld.application.tests.read;

import ld.application.api.resolver.LocalProductPhotoUrlResolver;
import ld.application.config.common.SharedPostgresContainer;
import ld.application.context.ProductIntegrationTest;
import ld.application.read.GetProductService;
import ld.application.response.GetProductResponse;
import ld.application.response.ProductCategoryResponse;
import ld.application.shared.product.CreateProductCommandFixture;
import ld.domain.features.product.CreateProductUseCase;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.FailureType;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static ld.standard.lib.helper.test.ResultTestSupport.assertFailure;
import static org.assertj.core.api.Assertions.assertThat;

@ProductIntegrationTest
class GetProductServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private GetProductService getProductService;

    @Autowired
    private CreateProductUseCase createProductUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private LocalProductPhotoUrlResolver photoUrlResolver;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(PRODUCT_COLORS).execute();
        dsl.deleteFrom(PRODUCT_PHOTOS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    private void insertPhoto(UUID productId, String storageKey) {
        dsl.insertInto(PRODUCT_PHOTOS)
                .columns(PRODUCT_PHOTOS.ID, PRODUCT_PHOTOS.PRODUCT_ID, PRODUCT_PHOTOS.STORAGE_KEY, PRODUCT_PHOTOS.POSITION)
                .values(UUID.randomUUID(), productId, storageKey, 0)
                .execute();
    }

    @Test
    void findById_returns_success_with_mapped_fields_when_product_exists() {
        var created = ResultTestSupport.extractValue(
                createProductUseCase.execute(CreateProductCommandFixture.aValidCommand()));

        Result<GetProductResponse> result = getProductService.findById(created.productId());

        assertThat(result.isSuccess()).isTrue();
        GetProductResponse response = ResultTestSupport.extractValue(result);
        assertThat(response.productId()).isEqualTo(created.productId());
        assertThat(response.name()).isEqualTo("T-shirt basique");
        assertThat(response.productCategory()).isEqualTo(ProductCategoryResponse.ACCESSORIES);
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(response.colors()).containsExactlyInAnyOrder("Rouge".toUpperCase(), "Bleu".toUpperCase());
    }

    @Test
    void findById_resolves_each_photo_url_via_the_real_resolver() {
        var created = ResultTestSupport.extractValue(
                createProductUseCase.execute(CreateProductCommandFixture.aValidCommand()));

        insertPhoto(created.productId(), "photo1.jpg");
        insertPhoto(created.productId(), "photo2.jpg");

        Result<GetProductResponse> result = getProductService.findById(created.productId());

        assertThat(result.isSuccess()).isTrue();
        GetProductResponse response = ResultTestSupport.extractValue(result);

        String expectedPhoto1 = photoUrlResolver.resolve(created.productId(), "photo1.jpg");
        String expectedPhoto2 = photoUrlResolver.resolve(created.productId(), "photo2.jpg");

        assertThat(response.photosUri()).containsExactlyInAnyOrder(expectedPhoto1, expectedPhoto2);
    }

    @Test
    void findById_returns_empty_photo_list_when_product_has_no_photos() {
        var created = ResultTestSupport.extractValue(
                createProductUseCase.execute(CreateProductCommandFixture.aValidCommand()));

        Result<GetProductResponse> result = getProductService.findById(created.productId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(ResultTestSupport.extractValue(result).photosUri()).isEmpty();
    }

    @Test
    void findById_returns_resource_not_found_when_product_does_not_exist() {
        Result<GetProductResponse> result = getProductService.findById(UUID.randomUUID());

        assertThat(result.isFailure()).isTrue();
        assertFailure(result, FailureType.RESOURCE_NOT_FOUND, ProductErrorCode.PRODUCTS_NOT_FOUND);
    }

    @Test
    void findAll_maps_every_element_of_the_page_and_preserves_paging_metadata() {
        ResultTestSupport.extractValue(
                createProductUseCase.execute(CreateProductCommandFixture.aCommandWithName("A")));
        var productB = ResultTestSupport.extractValue(
                createProductUseCase.execute(CreateProductCommandFixture.aCommandWithName("B")));

        insertPhoto(productB.productId(), "p.jpg");

        Page<GetProductResponse> page = getProductService.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(GetProductResponse::name)
                .containsExactlyInAnyOrder("A", "B");

        var responseB = page.getContent().stream()
                .filter(r -> r.productId().equals(productB.productId()))
                .findFirst()
                .orElseThrow();

        String expectedPhoto = photoUrlResolver.resolve(productB.productId(), "p.jpg");
        assertThat(responseB.photosUri()).containsExactly(expectedPhoto);
    }

    @Test
    void findAll_returns_empty_page_when_repository_is_empty() {
        Page<GetProductResponse> page = getProductService.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}