package ld.application.problems.product;

import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;

import java.util.Map;

public sealed interface CreateProductProblem extends ApiProblem {

    @ProductProblem(ProductErrorCode.PRODUCT_ALREADY_EXISTS)
    record productNameAlreadyExists(String existingName)
            implements CreateProductProblem, ConflictProblem {

        public String title() {
            return "Produit déjà existant";
        }

        public String detail() {
            return "Le produit %s existe déjà".formatted(existingName);
        }

        public Map<String, ?> properties() {
            return Map.of("name", existingName);
        }
    }
}