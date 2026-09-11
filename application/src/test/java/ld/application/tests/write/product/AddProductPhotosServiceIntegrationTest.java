package ld.application.tests.write.product;

import ld.application.config.common.SharedPostgresContainer;
import ld.application.config.product.SwitchableProductEditor;
import ld.application.context.ProductIntegrationTest;
import ld.application.shared.product.AddProductPhotosCommandFixture;
import ld.application.shared.product.ProductTestFixture;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.AddProductPhotosUseCase;
import ld.domain.features.product.photos.ProductPhotoStoragePort;
import ld.standard.lib.helper.test.ResultTestSupport;
import ld.standard.lib.validation.Result;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import static ld.application.jooq.tables.ProductColors.PRODUCT_COLORS;
import static ld.application.jooq.tables.ProductPhotos.PRODUCT_PHOTOS;
import static ld.application.jooq.tables.Products.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ProductIntegrationTest
class AddProductPhotosServiceIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES = SharedPostgresContainer.INSTANCE;

    @Autowired
    private AddProductPhotosUseCase addProductPhotosUseCase;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ProductTestFixture productTestFixture;

    @MockitoBean
    private ProductPhotoStoragePort productPhotoStoragePort;

    @BeforeEach
    void setUp() {
        dsl.deleteFrom(PRODUCT_COLORS).execute();
        dsl.deleteFrom(PRODUCT_PHOTOS).execute();
        dsl.deleteFrom(PRODUCTS).execute();
    }

    @Test
    void should_persist_photos_and_return_snapshot_when_upload_succeeds() {
        var product = productTestFixture.createExistingProduct();
        var command = AddProductPhotosCommandFixture.aValidCommand(product.id());

        given(productPhotoStoragePort.store(eq(product.id()), any(), any()))
                .willReturn("storage-key-1", "storage-key-2");

        Result<ProductPhotoSnapshot> result = addProductPhotosUseCase.execute(command);

        assertThat(result.isSuccess()).isTrue();
        var snapshot = ResultTestSupport.extractValue(result);
        assertThat(snapshot.photos()).hasSize(command.photos().size());

        int photoCount = dsl.fetchCount(
                PRODUCT_PHOTOS, PRODUCT_PHOTOS.PRODUCT_ID.eq(product.id())
        );
        assertThat(photoCount).isEqualTo(command.photos().size());
    }

    @Test
    void should_not_call_storage_when_product_does_not_exist() {
        var command = AddProductPhotosCommandFixture.aValidCommandWithUnknownProduct();

        Result<ProductPhotoSnapshot> result = addProductPhotosUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();
        verifyNoInteractions(productPhotoStoragePort);
        assertThat(dsl.fetchCount(PRODUCT_PHOTOS)).isZero();
    }

    @Test
    void should_not_call_storage_when_business_rule_is_violated() {

        var product = productTestFixture.createExistingProduct();
        var command = AddProductPhotosCommandFixture.aCommandWithDisallowedExtension(product.id());

        Result<ProductPhotoSnapshot> result = addProductPhotosUseCase.execute(command);

        assertThat(result.isFailure()).isTrue();
        verifyNoInteractions(productPhotoStoragePort);
        assertThat(dsl.fetchCount(PRODUCT_PHOTOS)).isZero();
    }

    @Nested
    class RollbackScenario {

        @Autowired
        SwitchableProductEditor switchableProductEditor;
        @Test
        void should_rollback_persistence_when_save_fails_after_photos_uploaded() {
            switchableProductEditor.failAfterCreateWith(new RuntimeException("exception after persistence"));
            var product = productTestFixture.createExistingProduct();
            var command = AddProductPhotosCommandFixture.aValidCommand(product.id());

            given(productPhotoStoragePort.store(any(), any(), any()))
                    .willReturn("storage-key-1");

            assertThatThrownBy(() -> addProductPhotosUseCase.execute(command))
                    .isInstanceOf(RuntimeException.class);

            int photoCount = dsl.fetchCount(
                    PRODUCT_PHOTOS, PRODUCT_PHOTOS.PRODUCT_ID.eq(product.id())
            );
            assertThat(photoCount).isZero();
        }
    }
}
