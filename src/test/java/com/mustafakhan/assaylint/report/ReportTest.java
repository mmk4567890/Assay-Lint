package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportTest {

    private final Report report = Reports.withIssues();

    @Test
    void sortsIssuesByLine() {
        assertEquals(List.of(0, 2, 2, 4), report.issues().stream().map(i -> i.line()).toList());
    }

    @Test
    void countsBySeverityAndRule() {
        assertEquals(Map.of(Severity.ERROR, 2L, Severity.WARNING, 2L), report.countsBySeverity());
        assertEquals(List.of("Parse", "Range", "Unit", "UnknownAssay"), List.copyOf(report.countsByRule().keySet()));
    }

    @Test
    void checksSeverityThreshold() {
        assertTrue(report.hasIssuesAtLeast(Severity.ERROR));
        assertFalse(Reports.clean().hasIssuesAtLeast(Severity.WARNING));
    }

    @Test
    void cleanReportHasZeroCountsForEverySeverity() {
        assertEquals(Map.of(Severity.ERROR, 0L, Severity.WARNING, 0L), Reports.clean().countsBySeverity());
    }

    @Test
    void runTimeIsTruncatedToSeconds() {
        assertEquals(Instant.parse("2026-02-01T09:30:00Z"), report.metadata().runAt());
    }

    @Test
    void passOrFailFollowsTheFailOnLevel() {
        assertFalse(report.passed());
        assertTrue(Reports.warningsOnly(Severity.ERROR).passed());
        assertFalse(Reports.warningsOnly(Severity.WARNING).passed());
        assertTrue(Reports.clean().passed());
    }

    @Test
    void ordersRuleCountsByFrequency() {
        Report mixed = new Report(Reports.METADATA, List.of(
                new ValidationIssue(2, "S1", "Range", Severity.ERROR, "a"),
                new ValidationIssue(3, "S2", "Unit", Severity.ERROR, "b"),
                new ValidationIssue(4, "S3", "Unit", Severity.ERROR, "c")));

        assertEquals(List.of("Unit", "Range"), List.copyOf(mixed.countsByRule().keySet()));
    }
}
