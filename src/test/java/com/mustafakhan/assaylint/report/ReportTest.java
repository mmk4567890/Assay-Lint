package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.Severity;
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
    void countsDistinctDataRowsWithIssues() {
        assertEquals(2, report.rowsWithIssues());
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
}
