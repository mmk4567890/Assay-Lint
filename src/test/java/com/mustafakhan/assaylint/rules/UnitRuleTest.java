package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.withUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class UnitRuleTest {

    private final UnitRule rule = new UnitRule();

    @Test
    void acceptsExpectedUnit() {
        assertTrue(rule.check(List.of(withUnit(2, "glucose", "mg/dL")), CONFIG).isEmpty());
    }

    @Test
    void flagsWrongUnit() {
        List<ValidationIssue> issues = rule.check(List.of(withUnit(2, "glucose", "mmol/L")), CONFIG);

        assertEquals(1, issues.size());
        assertEquals("Unit 'mmol/L' does not match expected unit 'mg/dL' for assay 'glucose'",
                issues.get(0).message());
    }

    @Test
    void caseDifferenceIsAMismatch() {
        assertEquals(1, rule.check(List.of(withUnit(2, "glucose", "mg/dl")), CONFIG).size());
    }

    @Test
    void skipsUnknownAssayAndBlankUnit() {
        assertTrue(rule.check(List.of(withUnit(2, "mystery", "x"), withUnit(3, "glucose", "")), CONFIG).isEmpty());
    }
}
