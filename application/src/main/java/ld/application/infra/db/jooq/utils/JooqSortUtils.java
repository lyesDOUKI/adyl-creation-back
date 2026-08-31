package ld.application.infra.db.jooq.utils;

import ld.application.infra.db.jooq.exception.InvalidSortFieldException;
import org.jooq.Field;
import org.jooq.SortField;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public class JooqSortUtils {

    private JooqSortUtils() {}

    public static List<SortField<?>> toOrderFields(
            Sort sort,
            Map<String, Field<?>> sortableFields,
            SortField<?> defaultSort
    ) {
        if (sort.isUnsorted()) {
            return List.of(defaultSort);
        }
        return sort.stream()
                .map(order -> {
                    Field<?> field = sortableFields.get(order.getProperty());
                    if (field == null) {
                        throw new InvalidSortFieldException(
                                "Tri non supporté sur le champ: " + order.getProperty());
                    }
                    return order.isAscending() ? field.asc() : field.desc();
                })
                .toList();
    }
}
