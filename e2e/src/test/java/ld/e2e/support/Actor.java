package ld.e2e.support;

import java.util.UUID;

public enum Actor {
    ADMIN("00000000-0000-0000-0000-0000000000a1", "ADMIN"),
    CUSTOMER("00000000-0000-0000-0000-0000000000c1", "USER"),
    OTHER_CUSTOMER("00000000-0000-0000-0000-0000000000c2", "USER");

    private final UUID subject;
    private final String role;

    Actor(String subject, String role) {
        this.subject = UUID.fromString(subject);
        this.role = role;
    }

    public UUID subject() {
        return subject;
    }

    public String role() {
        return role;
    }
}