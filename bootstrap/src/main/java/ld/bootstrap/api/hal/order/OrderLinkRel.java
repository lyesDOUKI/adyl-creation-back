package ld.bootstrap.api.hal.order;

import ld.bootstrap.api.hal.conf.ApiLinkRel;

import java.util.Map;

public enum OrderLinkRel implements ApiLinkRel {

    CANCEL("cancel", "cancelOrder",
            "Présent uniquement si la commande est annulable (état PENDING).");

    private final String rel;
    private final String operationId;
    private final String description;

    OrderLinkRel(String rel, String operationId, String description) {
        this.rel = rel;
        this.operationId = operationId;
        this.description = description;
    }

    @Override public String rel() { return rel; }
    @Override public String operationId() { return operationId; }
    @Override public String description() { return description; }

    @Override
    public Map<String, String> parameters() {
        return Map.of("id", "$response.body#/orderId");
    }
}