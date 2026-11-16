package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.record;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class DuplicateSampleIdRuleTest {

    private final DuplicateSampleIdRule rule = new DuplicateSampleIdRule();

    @Test
    void sameSampleWithDifferentAssaysIsFine() {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", "95"), record(3, "S1", "ph", "7")), CONFIG)
                .isEmpty());
    }

    @Test
    void flagsEveryRepeatAgainstTheFirstOccurrence() {
        List<ValidationIssue> issues = rule.check(List.of(
                record(2, "S1", "glucose", "95"),
                record(3, "S2", "glucose", "90"),
                record(4, "S1", "glucose", "96"),
                record(5, "S1", "glucose", "97")), CONFIG);

        assertEquals(List.of(4, 5), issues.stream().map(ValidationIssue::line).toList());
        assertEquals("Duplicate sample_id 'S1' for assay 'glucose' (first seen on line 2)",
                issues.get(0).message());
    }

    @Test
    void matchingIsCaseSensitive() {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", "95"), record(3, "s1", "glucose", "95")), CONFIG)
                .isEmpty());
    }

    @Test
    void ignoresRowsWithBlankKeyFields() {
        assertTrue(rule.check(List.of(record(2, "", "glucose", "95"), record(3, "", "glucose", "95")), CONFIG)
                .isEmpty());
    }
}
