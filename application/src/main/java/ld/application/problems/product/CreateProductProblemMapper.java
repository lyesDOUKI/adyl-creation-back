package ld.application.problems.product;

import ld.domain.features.product.validation.CreateProductError;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.ProblemSpecs;

public class CreateProductProblemMapper implements ProblemMapper<CreateProductError> {

    @Override
    public ProblemDetailResponse toProblem(CreateProductError error) {
        return ProblemSpecs.toResponse(toApi(error));
    }

    private CreateProductProblem toApi(CreateProductError error) {
        return switch (error) {
            case CreateProductError.ProductNameAlreadyExists e ->
                    new CreateProductProblem.productNameAlreadyExists(e.existingName());
        };
    }
}