package com.mustafakhan.assaylint.rules;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Pattern;

/**
 * Shared interpretation of raw field values, so that every rule agrees on what
 * counts as a number or a timestamp.
 */
final class Values {

    // Double.parseDouble also accepts "NaN", "Infinity", hex floats and type
    // suffixes like "1d"; none of those belong in a lab result.
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?");

    private Values() {
    }

    /**
     * Parses a plain decimal number.
     *
     * @param raw the field value
     * @return the number, or empty if it is not a finite decimal
     */
    static OptionalDouble number(String raw) {
        if (!DECIMAL.matcher(raw).matches()) {
            return OptionalDouble.empty();
        }
        double value = Double.parseDouble(raw);
        return Double.isFinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
    }

    /**
     * Parses an ISO-8601 date-time. A value without an offset is read as UTC.
     *
     * @param raw the field value
     * @return the instant, or empty if the value is not an ISO-8601 date-time
     */
    static Optional<Instant> timestamp(String raw) {
        try {
            return Optional.of(OffsetDateTime.parse(raw).toInstant());
        } catch (DateTimeParseException withoutOffset) {
            try {
                return Optional.of(LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC));
            } catch (DateTimeParseException invalid) {
                return Optional.empty();
            }
        }
    }

    /**
     * Formats a configured bound without a trailing {@code .0}.
     *
     * @param value the number to format
     * @return a plain decimal string
     */
    static String format(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
