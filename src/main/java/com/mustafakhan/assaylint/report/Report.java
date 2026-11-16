package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The complete result of a validation run, ready to be rendered in any format.
 *
 * @param metadata traceability details for the run
 * @param issues   every issue found, sorted by line
 */
public record Report(RunMetadata metadata, List<ValidationIssue> issues) {

    /** Makes a defensive copy of the issues, sorted by line. */
    public Report {
        issues = issues.stream().sorted(ValidationIssue.BY_LINE).toList();
    }

    /**
     * Counts issues per severity, including severities with no issues.
     *
     * @return severity to count, most severe first
     */
    public Map<Severity, Long> countsBySeverity() {
        Map<Severity, Long> counts = new EnumMap<>(Severity.class);
        for (Severity severity : Severity.values()) {
            counts.put(severity, 0L);
        }
        issues.forEach(issue -> counts.merge(issue.severity(), 1L, Long::sum));
        return counts;
    }

    /**
     * Counts issues per rule, for rules that raised at least one.
     *
     * @return rule name to count, sorted by rule name
     */
    public Map<String, Long> countsByRule() {
        return issues.stream().collect(Collectors.groupingBy(ValidationIssue::rule, TreeMap::new,
                Collectors.counting()));
    }

    /**
     * Returns whether any issue meets the given severity threshold.
     *
     * @param threshold the minimum severity of interest
     * @return {@code true} if at least one issue is at or above {@code threshold}
     */
    public boolean hasIssuesAtLeast(Severity threshold) {
        return issues.stream().anyMatch(issue -> issue.severity().isAtLeast(threshold));
    }
}
