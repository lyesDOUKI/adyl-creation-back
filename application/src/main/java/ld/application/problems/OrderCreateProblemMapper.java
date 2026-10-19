package ld.application.problems;

import ld.domain.features.order.OrderCreateError;
import ld.domain.features.order.validation.OrderErrorCode;
import ld.spring.web.lib.ProblemDetailResponse;
import ld.spring.web.lib.ProblemMapper;
import ld.spring.web.lib.Problems;

import java.util.Map;

public class OrderCreateProblemMapper implements ProblemMapper<OrderCreateError> {

    @Override
    public ProblemDetailResponse toProblem(OrderCreateError error) {
        return switch (error) {

            case OrderCreateError.UnknownProducts e -> Problems.unprocessable(
                    OrderErrorCode.PRODUCTS_NOT_FOUND,
                    "Produits introuvables",
                    "Des produits de la commande sont introuvables (identifiants : %s)"
                            .formatted(e.productIds()),
                    Map.of("productIds", e.productIds()));

            case OrderCreateError.UnavailableProducts e -> Problems.conflict(
                    OrderErrorCode.PRODUCT_NOT_AVAILABLE,
                    "Produits indisponibles",
                    "Les produits suivants sont indisponibles (identifiants : %s)"
                            .formatted(e.productIds()),
                    Map.of("productIds", e.productIds()));

            case OrderCreateError.ColorNotAvailable e -> Problems.unprocessable(
                    OrderErrorCode.PRODUCT_COLOR_NOT_AVAILABLE,
                    "Couleur demandée non valide",
                    "La couleur '%s' n'est pas disponible pour le produit identifiant : %s. Couleurs disponibles : %s"
                            .formatted(e.color(), e.productId(), e.availableColors()),
                    Map.of("productId", e.productId(),
                            "requestedColor", e.color(),
                            "availableColors", e.availableColors()));
        };
    }
}
