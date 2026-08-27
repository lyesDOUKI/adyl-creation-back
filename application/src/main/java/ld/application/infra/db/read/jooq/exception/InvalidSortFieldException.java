package ld.application.infra.db.read.jooq.exception;

public class InvalidSortFieldException extends RuntimeException {
    public InvalidSortFieldException(String message) {
        super(message);
    }
}
