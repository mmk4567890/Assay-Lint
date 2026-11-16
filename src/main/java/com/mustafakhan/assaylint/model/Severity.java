package com.mustafakhan.assaylint.model;

/**
 * How serious a validation issue is. Declared from least to most severe so that
 * the natural enum order can be used for threshold comparisons.
 */
public enum Severity {
    WARNING,
    ERROR;

    /**
     * Returns whether this severity meets or exceeds the given threshold.
     *
     * @param threshold the minimum severity of interest
     * @return {@code true} if this severity is at least as severe as {@code threshold}
     */
    public boolean isAtLeast(Severity threshold) {
        return compareTo(threshold) >= 0;
    }
}
