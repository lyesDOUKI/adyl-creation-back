package ld.application.problems.order.create;

import ld.application.problems.order.OrderProblem;
import ld.application.problems.order.OrderErrorCode;
import ld.spring.web.lib.ApiProblem;
import ld.spring.web.lib.ConflictProblem;
import ld.spring.web.lib.UnprocessableProblem;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public sealed interface CreateOrderProblem extends ApiProblem {

    @OrderProblem(OrderErrorCode.PRODUCTS_NOT_FOUND)
    record unknownProducts(Set<UUID> products) implements CreateOrderProblem, UnprocessableProblem {

        public String title() {
            return "Produits introuvables";
        }

        public String detail() {
            return "Des produits de la commande sont introuvables (identifiants : %s)"
                    .formatted(products);
        }

        public Map<String, ?> properties() {
            return Map.of("productIds", products);
        }
    }

    @OrderProblem(OrderErrorCode.PRODUCT_NOT_AVAILABLE)
    record unavailableProducts(Set<UUID> products) implements CreateOrderProblem, ConflictProblem {

        public String title() {
            return "Produits indisponibles";
        }

        public String detail() {
            return "Les produits suivants sont indisponibles (identifiants : %s)"
                    .formatted(products);
        }

        public Map<String, ?> properties() {
            return Map.of("productIds", products);
        }
    }

    @OrderProblem(OrderErrorCode.PRODUCT_COLOR_NOT_AVAILABLE)
    record colorNotAvailable(
            UUID productId,
            String color,
            Set<String> availableColors
    ) implements CreateOrderProblem, UnprocessableProblem {

        public String title() {
            return "Couleur demandée non valide";
        }

        public String detail() {
            return "La couleur '%s' n'est pas disponible pour le produit identifiant : %s. Couleurs disponibles : %s"
                    .formatted(color, productId, availableColors);
        }

        public Map<String, ?> properties() {
            return Map.of(
                    "productId", productId,
                    "requestedColor", color,
                    "availableColors", availableColors
            );
        }
    }
}