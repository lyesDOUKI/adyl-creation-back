package ld.application.problems.product.photos;

import ld.application.problems.product.photos.AddProductPhotosProblem.EmptyPhoto;
import ld.application.problems.product.photos.AddProductPhotosProblem.ExtensionNotAllowed;
import ld.application.problems.product.photos.AddProductPhotosProblem.NoPhotos;
import ld.application.problems.product.photos.AddProductPhotosProblem.PhotoTooLarge;
import ld.application.problems.product.photos.AddProductPhotosProblem.ProductNotFound;
import ld.spring.web.lib.ApiErrorCode;
import ld.spring.web.lib.ApiProblem;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

public enum AddProductPhotosErrorCode implements ApiErrorCode {

    PRODUCT_NOT_FOUND(NOT_FOUND, "Produit introuvable", ProductNotFound.class),
    PHOTO_LIST_EMPTY(UNPROCESSABLE_CONTENT, "Aucune image", NoPhotos.class),
    PHOTO_SIZE_ZERO(UNPROCESSABLE_CONTENT, "Image vide", EmptyPhoto.class),
    PHOTO_MAX_SIZE_EXCEEDED(UNPROCESSABLE_CONTENT, "Image trop volumineuse", PhotoTooLarge.class),
    PHOTO_EXTENSION_NOT_ALLOWED(UNPROCESSABLE_CONTENT, "Extension incorrecte", ExtensionNotAllowed.class);

    private final HttpStatus status;
    private final String title;
    private final Class<? extends ApiProblem> payload;

    AddProductPhotosErrorCode(HttpStatus status, String title, Class<? extends ApiProblem> payload) {
        this.status = status;
        this.title = title;
        this.payload = payload;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String title() { return title; }
    @Override public Class<? extends ApiProblem> payload() { return payload; }
}