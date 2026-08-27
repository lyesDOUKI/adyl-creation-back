package ld.domain.features.product.photos;


import ld.domain.features.product.model.ProductPhoto;
import ld.domain.features.product.model.ProductPhotoSnapshot;
import ld.domain.features.product.photos.validation.PhotoErrorCode;
import ld.domain.features.product.photos.validation.PhotoRule;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.domain.features.shared.ProductSnapshotTestBuilder;
import ld.standard.lib.validation.FailureType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static ld.standard.lib.helper.test.ResultTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddProductPhotosUseCaseImplTest {

    private final AddProductPhotosUseCase addProductPhotosUseCase;
    private final InMemoryAddProductPhotosRepository addProductPhotosRepository = new InMemoryAddProductPhotosRepository();
    private final InMemoryProductPhotoStoragePort productPhotoStoragePort = new InMemoryProductPhotoStoragePort();

    AddProductPhotosUseCaseImplTest() {
        this.addProductPhotosUseCase = new AddProductPhotosUseCaseImpl(
                addProductPhotosRepository,
                productPhotoStoragePort
        );
    }

    private AddProductPhotosCommand.PhotoToUpload photo(String fileName, byte[] content) {
        return new AddProductPhotosCommand.PhotoToUpload(fileName, content);
    }

    private AddProductPhotosCommand.PhotoToUpload defaultPhoto(String fileName) {
        return photo(fileName, "content".getBytes());
    }

    private AddProductPhotosCommand defaultCommand(UUID productId) {
        return new AddProductPhotosCommand(productId, List.of(defaultPhoto("photo1.jpg")));
    }

    private AddProductPhotosCommand withPhotos(UUID productId, List<AddProductPhotosCommand.PhotoToUpload> photos) {
        return new AddProductPhotosCommand(productId, photos);
    }

    private void registerProduct(UUID productId, List<ProductPhoto> existingPhotos) {
        var productSnapshot = ProductSnapshotTestBuilder.aProduct()
                .withId(productId)
                .build();

        addProductPhotosRepository.addProduct(
                new ProductPhotoSnapshot(productSnapshot, existingPhotos)
        );
    }

    @Nested
    @DisplayName("Quand le produit demandé n'est pas trouvé")
    class WhenProductNotFound {

        @Test
        @DisplayName("l'ajout échoue")
        void shouldFailToAddPhotos() {
            var productId = UUID.randomUUID();
            var command = defaultCommand(productId);

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.RESOURCE_NOT_FOUND, ProductErrorCode.PRODUCTS_NOT_FOUND);
        }

        @Test
        @DisplayName("Rien n'est persisté et aucun fichier n'est stocké")
        void shouldNotPersistNorStore() {
            var productId = UUID.randomUUID();
            var command = defaultCommand(productId);

            assertFailure(addProductPhotosUseCase.execute(command));

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand la liste de photos est vide")
    class WhenPhotosListIsEmpty {

        @Test
        @DisplayName("une erreur business PHOTO_LIST_EMPTY est remontée, rien n'est persisté ni stocké")
        void shouldFailWithEmptyList() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, List.of());

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_LIST_EMPTY);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
        @Test
        void shouldFailWithNullList() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, null);

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_LIST_EMPTY);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand une photo a une taille de 0")
    class WhenPhotoSizeIsZero {

        @Test
        @DisplayName("une erreur business PHOTO_SIZE_ZERO est remontée")
        void shouldFailWithZeroSize() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, List.of(photo("photo1.jpg", new byte[0])));

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_SIZE_ZERO);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
        @Test
        void shouldFailWithNullSize() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, List.of(photo("photo1.jpg", null)));

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_SIZE_ZERO);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand une photo dépasse la taille maximum autorisée")
    class WhenPhotoExceedsMaxSize {

        @Test
        @DisplayName("une erreur business PHOTO_MAX_SIZE_EXCEEDED est remontée")
        void shouldFailWithMaxSizeExceeded() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            byte[] tooLarge = new byte[(int) PhotoRule.MAX_FILE_SIZE_BYTES + 1];
            var command = withPhotos(productId, List.of(photo("photo1.jpg", tooLarge)));

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_MAX_SIZE_EXCEEDED);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand l'extension de la photo n'est pas autorisée")
    class WhenExtensionNotAllowed {

        @Test
        @DisplayName("une erreur business PHOTO_EXTENSION_NOT_ALLOWED est remontée")
        void shouldFailWithExtensionNotAllowed() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, List.of(defaultPhoto("photo1.gif")));

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }

        @Test
        void shouldFailWithExtensionNoneExtension() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            var command = withPhotos(productId, List.of(defaultPhoto("photo1")));

            var result = addProductPhotosUseCase.execute(command);

            assertFailure(result, FailureType.BUSINESS_RULE, PhotoErrorCode.PHOTO_EXTENSION_NOT_ALLOWED);

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isZero();
        }
    }

    @Nested
    @DisplayName("Quand la commande est valide")
    class WhenCommandIsValid {

        @Test
        @DisplayName("Les photos sont stockées et persistées avec la bonne position")
        void shouldStoreAndPersistPhotosWithCorrectPosition() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of(new ProductPhoto(UUID.randomUUID(), "photoxistante.jpg", 0)));

            var command = withPhotos(productId, List.of(
                    defaultPhoto("photo1.jpg"),
                    defaultPhoto("photo2.png")
            ));

            var result = addProductPhotosUseCase.execute(command);
            assertSuccess(result);

            assertThat(productPhotoStoragePort.countStored()).isEqualTo(2);
            assertThat(addProductPhotosRepository.countUpdates()).isOne();

            var persisted = extractValue(result);

            assertThat(persisted.photos())
                    .hasSize(3)
                    .extracting(ProductPhoto::position)
                    .containsExactly(0, 1, 2);

            assertThat(persisted.photos())
                    .extracting(ProductPhoto::storageKey)
                    .allSatisfy(key -> assertThat(key).isNotBlank());
        }

        @Test
        @DisplayName("Les photos ajoutées viennent s'ajouter aux photos existantes du produit")
        void shouldAppendToExistingPhotos() {
            var productId = UUID.randomUUID();
            var existingPhoto = new ProductPhoto(UUID.randomUUID(), "existing-key.jpg", 0);
            registerProduct(productId, List.of(existingPhoto));

            var command = withPhotos(productId, List.of(defaultPhoto("new-photo.jpg")));

            var result = addProductPhotosUseCase.execute(command);
            assertSuccess(result);

            var persisted = extractValue(result);

            assertThat(persisted.photos())
                    .hasSize(2)
                    .extracting(ProductPhoto::storageKey)
                    .contains("existing-key.jpg");
        }
    }

    @Nested
    @DisplayName("Quand le stockage d'une photo échoue")
    class WhenPhotoStorageFails {

        @Test
        @DisplayName("une exception technique est levée, rien n'est persisté, la photo déjà enregistré est prise en compte")
        void shouldThrowAndNotPersist() {
            var productId = UUID.randomUUID();
            registerProduct(productId, List.of());

            productPhotoStoragePort.failOnFileName();

            var command = withPhotos(productId, List.of(
                    defaultPhoto("photo1.jpg"),
                    defaultPhoto("broken.jpg")
            ));

            assertThrows(ProductPhotoStorageException.class, () -> addProductPhotosUseCase.execute(command));

            assertThat(addProductPhotosRepository.countUpdates()).isZero();
            assertThat(productPhotoStoragePort.countStored()).isOne();
        }
    }
}