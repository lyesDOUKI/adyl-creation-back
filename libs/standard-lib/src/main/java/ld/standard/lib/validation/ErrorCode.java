package ld.standard.lib.validation;

public interface ErrorCode {
    String name();

    default String code() {
        return name();
    }
}
