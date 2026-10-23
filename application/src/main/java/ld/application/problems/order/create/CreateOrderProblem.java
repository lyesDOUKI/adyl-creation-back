package ld.application.problems.order.create;

import io.swagger.v3.oas.annotations.media.Schema;
import ld.domain.features.order.OrderCreateError;
import ld.domain.features.product.model.ProductColor;
import ld.spring.web.lib.ApiProblem;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.stream.Collectors.toCollection;
import static ld.application.problems.order.create.CreateOrderErrorCode.PRODUCTS_NOT_FOUND;
import static ld.application.problems.order.create.CreateOrderErrorCode.PRODUCT_COLOR_NOT_AVAILABLE;
import static ld.application.problems.order.create.CreateOrderErrorCode.PRODUCT_NOT_AVAILABLE;

public sealed interface CreateOrderProblem extends ApiProblem {

    record UnknownProducts(
            @Schema(description = "Identifiants des produits introuvables") SortedSet<UUID> productIds
    ) implements CreateOrderProblem {
        @Override public CreateOrderErrorCode getCode() { return PRODUCTS_NOT_FOUND; }
        @Override public String getDetail() { return "Des produits de la commande sont introuvables."; }
    }

    record UnavailableProducts(
            @Schema(description = "Identifiants des produits indisponibles") SortedSet<UUID> productIds
    ) implements CreateOrderProblem {
        @Override public CreateOrderErrorCode getCode() { return PRODUCT_NOT_AVAILABLE; }
        @Override public String getDetail() { return "Certains produits de la commande sont indisponibles."; }
    }

    record ColorNotAvailable(
            @Schema(description = "Produit concerné") UUID productId,
            @Schema(description = "Couleur demandée") String requestedColor,
            @Schema(description = "Couleurs disponibles pour ce produit") SortedSet<String> availableColors
    ) implements CreateOrderProblem {
        @Override public CreateOrderErrorCode getCode() { return PRODUCT_COLOR_NOT_AVAILABLE; }
        @Override public String getDetail() {
            return "La couleur '%s' n'est pas disponible pour le produit %s.".formatted(requestedColor, productId);
        }
    }

    static CreateOrderProblem from(OrderCreateError error) {
        return switch (error) {
            case OrderCreateError.UnknownProducts e -> new UnknownProducts(new TreeSet<>(e.productIds()));
            case OrderCreateError.UnavailableProducts e -> new UnavailableProducts(new TreeSet<>(e.productIds()));
            case OrderCreateError.ColorNotAvailable e -> new ColorNotAvailable(
                    e.productId(),
                    e.color(),
                    e.availableColors().stream().map(ProductColor::value)
                            .collect(toCollection(TreeSet::new)));
        };
    }
}