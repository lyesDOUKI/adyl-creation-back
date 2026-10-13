package ld.domain.features.order;

import ld.domain.features.order.validation.OrderErrorCode;
import ld.domain.features.product.model.ProductColor;
import ld.standard.lib.validation.DomainError;
import ld.standard.lib.validation.FailureDetail;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public sealed interface OrderCreateError extends DomainError {


    record UnknownProducts(Set<UUID> productIds) implements OrderCreateError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.resourceNotFound(
                    OrderErrorCode.PRODUCTS_NOT_FOUND,
                    "Produits introuvables",
                    "Des produits de la commande sont introuvables (identifiants : " + productIds + ")");
        }
    }


    record UnavailableProducts(List<UUID> productIds) implements OrderCreateError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.PRODUCT_NOT_AVAILABLE,
                    "Produits indisponibles",
                    "Les produits suivants sont indisponibles (identifiants : %s)".formatted(productIds));
        }
    }


    record ColorNotAvailable(UUID productId, String color, List<ProductColor> availableColors)
            implements OrderCreateError {
        @Override
        public FailureDetail detail() {
            return FailureDetail.businessRule(
                    OrderErrorCode.PRODUCT_COLOR_NOT_AVAILABLE,
                    "Couleur demandée non valide",
                    "La couleur '%s' n'est pas disponible pour le produit identifiant : %s. Couleurs disponibles : %s"
                            .formatted(color, productId, availableColors));
        }
    }
}
