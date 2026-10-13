package ld.standard.lib.validation;

import java.util.List;

public final class BusinessGuard<T, E extends DomainError> {

    private final List<BusinessRule<T, E>> rules;

    private BusinessGuard(List<BusinessRule<T, E>> rules) {
        this.rules = rules;
    }

    @SafeVarargs
    public static <T, E extends DomainError> BusinessGuard<T, E> of(BusinessRule<T, E>... rules) {
        return new BusinessGuard<>(List.of(rules));
    }

    public Result<Void, E> validate(T context) {
        for (BusinessRule<T, E> rule : rules) {
            Result<Void, E> result = rule.apply(context);
            if (result.isFailure()) {
                return result;
            }
        }
        return Result.ok();
    }
}
