package com.mustafakhan.assaylint.config;

import java.util.Objects;

/**
 * Expected unit and inclusive value range for one assay.
 *
 * @param unit expected unit, compared exactly against the data
 * @param min  smallest acceptable value, inclusive
 * @param max  largest acceptable value, inclusive
 */
public record AssayDefinition(String unit, double min, double max) {

    /** Rejects a missing unit and an inverted range. */
    public AssayDefinition {
        Objects.requireNonNull(unit, "unit");
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") is greater than max (" + max + ")");
        }
    }

    /**
     * Returns whether a value lies inside this assay's range.
     *
     * @param value the measured value
     * @return {@code true} if {@code min <= value <= max}
     */
    public boolean contains(double value) {
        return value >= min && value <= max;
    }
}
