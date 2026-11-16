package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.withTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimestampRuleTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC);

    private final TimestampRule rule = new TimestampRule(NOW);

    @ParameterizedTest
    @ValueSource(strings = {
        "2026-01-05T10:00:00Z",
        "2026-01-05T10:00:00.123Z",
        "2026-01-05T10:00:00+02:00",
        "2026-01-05T10:00:00",
        "2026-01-05T10:00",
        "2026-03-01T12:00:00Z"
    })
    void acceptsIsoDateTimesUpToNow(String timestamp) {
        assertTrue(rule.check(List.of(withTimestamp(2, "S1", timestamp)), CONFIG).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"yesterday", "2026-01-05", "05/01/2026 10:00", "2026-13-01T10:00:00Z",
        "2026-01-05 10:00:00"})
    void rejectsInvalidTimestamps(String timestamp) {
        List<ValidationIssue> issues = rule.check(List.of(withTimestamp(2, "S1", timestamp)), CONFIG);

        assertEquals(1, issues.size());
        assertEquals("Timestamp '" + timestamp + "' is not a valid ISO-8601 date-time", issues.get(0).message());
    }

    @Test
    void flagsFutureTimestamp() {
        List<ValidationIssue> issues = rule.check(List.of(withTimestamp(2, "S1", "2026-03-01T12:00:01Z")), CONFIG);

        assertEquals("Timestamp 2026-03-01T12:00:01Z is in the future", issues.get(0).message());
    }

    @Test
    void offsetIsTakenIntoAccountForFutureCheck() {
        assertTrue(rule.check(List.of(withTimestamp(2, "S1", "2026-03-01T13:30:00+02:00")), CONFIG).isEmpty());
    }

    @Test
    void leavesBlankTimestampToRequiredFieldsRule() {
        assertTrue(rule.check(List.of(withTimestamp(2, "S1", "")), CONFIG).isEmpty());
    }
}
