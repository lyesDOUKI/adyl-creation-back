package ld.standard.lib.validation;

import java.util.List;

public final class BusinessGuard<T> {

    private final List<BusinessRule<T>> rules;

    private BusinessGuard(List<BusinessRule<T>> rules) {
        this.rules = rules;
    }

    @SafeVarargs
    public static <T> BusinessGuard<T> of(BusinessRule<T>... rules) {
        return new BusinessGuard<>(List.of(rules));
    }

    public Result<Void> validate(T context) {
        for (BusinessRule<T> rule : rules) {
            Result<Void> result = rule.apply(context);
            if (result.isFailure()) {
                return result;
            }
        }
        return Result.ok();
    }
}
