package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.withTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChronologyRuleTest {

    private final ChronologyRule rule = new ChronologyRule();

    @Test
    void acceptsIncreasingAndEqualTimestamps() {
        assertTrue(rule.check(List.of(
                withTimestamp(2, "S1", "2026-01-05T10:00:00Z"),
                withTimestamp(3, "S1", "2026-01-05T10:00:00Z"),
                withTimestamp(4, "S1", "2026-01-05T11:00:00Z")), CONFIG).isEmpty());
    }

    @Test
    void ordersEachSampleIndependently() {
        assertTrue(rule.check(List.of(
                withTimestamp(2, "S1", "2026-01-05T12:00:00Z"),
                withTimestamp(3, "S2", "2026-01-05T08:00:00Z")), CONFIG).isEmpty());
    }

    @Test
    void warnsOnceForASingleBackDatedEntry() {
        List<ValidationIssue> issues = rule.check(List.of(
                withTimestamp(2, "S1", "2026-01-05T10:00:00Z"),
                withTimestamp(3, "S1", "2026-01-05T09:00:00Z"),
                withTimestamp(4, "S1", "2026-01-05T11:00:00Z")), CONFIG);

        assertEquals(1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(3, issue.line());
        assertEquals(Severity.WARNING, issue.severity());
        assertEquals("Timestamp 2026-01-05T09:00:00Z is earlier than 2026-01-05T10:00:00Z recorded for sample 'S1'"
                + " on line 2", issue.message());
    }

    @Test
    void comparesInstantsAcrossOffsets() {
        assertEquals(1, rule.check(List.of(
                withTimestamp(2, "S1", "2026-01-05T10:00:00Z"),
                withTimestamp(3, "S1", "2026-01-05T11:00:00+02:00")), CONFIG).size());
    }

    @Test
    void skipsInvalidTimestampsAndBlankSampleIds() {
        assertTrue(rule.check(List.of(
                withTimestamp(2, "S1", "2026-01-05T10:00:00Z"),
                withTimestamp(3, "S1", "not-a-date"),
                withTimestamp(4, "", "2026-01-01T00:00:00Z"),
                withTimestamp(5, "", "2025-01-01T00:00:00Z")), CONFIG).isEmpty());
    }
}
