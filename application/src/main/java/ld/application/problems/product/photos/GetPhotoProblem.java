package ld.application.problems.product.photos;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.spring.web.lib.ApiProblem;

import java.util.UUID;

import static ld.application.problems.product.photos.GetPhotoErrorCode.PHOTO_NOT_FOUND;

public sealed interface GetPhotoProblem extends ApiProblem {
    record PhotoNotFound(
            @Schema(description = "La photo demandée est introuvable") UUID photoId
    ) implements GetPhotoProblem {
        @Override public GetPhotoErrorCode getCode() { return PHOTO_NOT_FOUND; }
        @Override public String getDetail() { return "La photo demandée est introuvable"; }
    }
}
