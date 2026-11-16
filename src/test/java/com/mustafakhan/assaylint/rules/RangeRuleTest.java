package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.record;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RangeRuleTest {

    private final RangeRule rule = new RangeRule();

    @ParameterizedTest
    @ValueSource(strings = {"0", "95", "600", "6e2"})
    void acceptsValuesWithinInclusiveBounds(String value) {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", value)), CONFIG).isEmpty());
    }

    @Test
    void flagsValueAboveMax() {
        List<ValidationIssue> issues = rule.check(List.of(record(3, "S1", "glucose", "600.5")), CONFIG);

        assertEquals(1, issues.size());
        assertEquals("Value 600.5 is outside the allowed range [0, 600] for assay 'glucose'",
                issues.get(0).message());
    }

    @Test
    void flagsValueBelowMin() {
        assertEquals(1, rule.check(List.of(record(3, "S1", "ph", "-0.1")), CONFIG).size());
    }

    @Test
    void skipsUnknownAssaysAndNonNumericValues() {
        assertTrue(rule.check(List.of(record(2, "S1", "mystery", "9999"), record(3, "S2", "glucose", "high")),
                CONFIG).isEmpty());
    }
}
