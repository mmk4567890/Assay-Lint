package com.mustafakhan.assaylint.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * A single data-integrity problem found in a sample file.
 *
 * @param line     1-based line number the issue refers to
 * @param sampleId sample identifier on that line, or an empty string if unknown
 * @param rule     name of the rule that raised the issue
 * @param severity how serious the issue is
 * @param message  human-readable description of the problem
 */
public record ValidationIssue(int line, String sampleId, String rule, Severity severity, String message) {

    /** Orders issues by line, then rule name, then message, giving stable report output. */
    public static final Comparator<ValidationIssue> BY_LINE = Comparator
            .comparingInt(ValidationIssue::line)
            .thenComparing(ValidationIssue::rule)
            .thenComparing(ValidationIssue::message);

    /**
     * Creates an issue for a parsed record, taking the line and sample ID from it.
     *
     * @param record   the record the issue refers to
     * @param rule     name of the rule raising the issue
     * @param severity how serious the issue is
     * @param message  human-readable description
     * @return the new issue
     */
    public static ValidationIssue of(SampleRecord record, String rule, Severity severity, String message) {
        return new ValidationIssue(record.line(), record.sampleId(), rule, severity, message);
    }

    /**
     * Returns the rule name as people read it, for example {@code "Duplicate sample ID"}
     * for the {@code DuplicateSampleId} rule.
     *
     * @return the human-readable rule name
     */
    public String ruleDisplayName() {
        return RuleNames.displayName(rule);
    }

    /** Rejects null fields so report writers never have to null-check. */
    public ValidationIssue {
        Objects.requireNonNull(sampleId, "sampleId");
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(message, "message");
    }
}
