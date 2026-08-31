package ld.application.infra.db.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ld.domain.features.order.model.OrderStatus;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .addMixIn(OrderStatus.class, OrderStatusMixIn.class)
            .addMixIn(OrderStatus.State.class, OrderStatusMixIn.StateMixIn.class);

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur de sérialisation JSONB pour OrderStatus", e);
        }
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || dbData.equals("{}")) {
            return OrderStatus.PENDING;
        }
        try {
            return MAPPER.readValue(dbData, OrderStatus.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur de désérialisation JSONB pour OrderStatus : " + dbData, e);
        }
    }
}
