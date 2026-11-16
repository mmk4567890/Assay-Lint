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

class NumericValueRuleTest {

    private final NumericValueRule rule = new NumericValueRule();

    @ParameterizedTest
    @ValueSource(strings = {"95", "-1.5", "0.25", ".5", "7.", "+3", "1.2e3", "4E-2"})
    void acceptsDecimalNumbers(String value) {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", value)), CONFIG).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12abc", "1,5", "NaN", "Infinity", "-Infinity", "1e999", "0x10", "1d", "--1"})
    void rejectsNonNumbers(String value) {
        List<ValidationIssue> issues = rule.check(List.of(record(2, "S1", "glucose", value)), CONFIG);

        assertEquals(1, issues.size());
        assertEquals("Value '" + value + "' is not a finite number", issues.get(0).message());
    }

    @Test
    void leavesBlankValuesToRequiredFieldsRule() {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", "")), CONFIG).isEmpty());
    }
}
