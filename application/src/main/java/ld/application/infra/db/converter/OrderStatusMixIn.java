package ld.application.infra.db.converter;

import com.fasterxml.jackson.annotation.*;
import ld.domain.features.order.model.OrderStatus;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderStatus.State.class, name = "PENDING"),
        @JsonSubTypes.Type(value = OrderStatus.State.class, name = "REJECTED"),
        @JsonSubTypes.Type(value = OrderStatus.State.class, name = "DELIVERED"),
        @JsonSubTypes.Type(value = OrderStatus.Accepted.class, name = "ACCEPTED")
})
public interface OrderStatusMixIn {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    interface StateMixIn {
        @JsonCreator
        static OrderStatus.State fromJson(@JsonProperty("type") String type) {
            return type == null ? OrderStatus.State.PENDING : OrderStatus.State.valueOf(type);
        }
    }
}
