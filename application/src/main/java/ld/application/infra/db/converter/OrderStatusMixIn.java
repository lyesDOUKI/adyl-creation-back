package ld.application.infra.db.converter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ld.domain.features.order.model.OrderStatus;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderStatus.Pending.class, name = "PENDING"),
        @JsonSubTypes.Type(value = OrderStatus.Rejected.class, name = "REJECTED"),
        @JsonSubTypes.Type(value = OrderStatus.Delivered.class, name = "DELIVERED"),
        @JsonSubTypes.Type(value = OrderStatus.Accepted.class, name = "ACCEPTED")
})
public interface OrderStatusMixIn {
}