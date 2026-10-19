package ld.application.api;

import ld.application.problems.order.OrderErrorCode;
import ld.application.problems.product.ProductErrorCode;
import ld.spring.web.lib.ResourceKind;
import ld.standard.lib.validation.ErrorCode;

public enum ApplicationResourceKind implements ResourceKind {

    ORDER(OrderErrorCode.ORDER_NOT_FOUND,
            "Commande introuvable",
            "La commande %s est introuvable"),

    PRODUCT(ProductErrorCode.PRODUCT_NOT_FOUND,
            "Produit introuvable",
            "Le produit %s est introuvable");

    private final ErrorCode code;
    private final String title;
    private final String detailTemplate;

    ApplicationResourceKind(ErrorCode code, String title, String detailTemplate) {
        this.code = code;
        this.title = title;
        this.detailTemplate = detailTemplate;
    }

    @Override public ErrorCode code()        { return code; }
    @Override public String title()          { return title; }
    @Override public String detailTemplate() { return detailTemplate; }
}