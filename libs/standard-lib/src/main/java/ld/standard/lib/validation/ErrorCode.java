package ld.standard.lib.validation;

public interface ErrorCode {
    default String code() {
        if (!(this instanceof Enum<?> e)) {
            throw new IllegalStateException(
                    "ErrorCode must be implemented by an enum."
            );
        }
        return e.name();
    }
}
