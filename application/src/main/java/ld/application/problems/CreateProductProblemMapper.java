package ld.application.problems;

import ld.domain.features.product.validation.CreateProductError;
import ld.domain.features.product.validation.ProductErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class CreateProductProblemMapper implements ProblemMapper<CreateProductError> {

    @Override
    public ProblemDetailResponse toProblem(CreateProductError error) {
        return switch (error) {

            case CreateProductError.ProductNameAlreadyExists e -> Problems.conflict(
                    ProductErrorCode.PRODUCT_ALREADY_EXISTS,
                    "Produit déjà existant",
                    "Le produit %s existe déjà".formatted(e.existingName()),
                    Map.of("name", e.existingName()));
        };
    }
}
