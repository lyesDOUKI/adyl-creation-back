package ld.domain.features.order.accept;

public interface CustomerOrderHistoryFinder {
    boolean hasEffectiveOrder(String customerIdentitySubject);
}
