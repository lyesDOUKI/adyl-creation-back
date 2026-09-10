package ld.domain.features.order.model;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Pattern;

public record OrderReference(String value) {

    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd
    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int SUFFIX_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Pattern PATTERN =
            Pattern.compile("^%s-\\d{8}-[%s]{%d}$".formatted(PREFIX, ALPHABET, SUFFIX_LENGTH));

    public OrderReference {
        Objects.requireNonNull(value, "value ne peut pas être null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Référence de commande invalide : " + value);
        }
    }

    public static OrderReference generate(Clock clock) {
        String datePart = LocalDate.now(clock).format(DATE_FMT);
        String suffix = randomSuffix();
        return new OrderReference("%s-%s-%s".formatted(PREFIX, datePart, suffix));
    }

    private static String randomSuffix() {
        var sb = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return value;
    }
}
