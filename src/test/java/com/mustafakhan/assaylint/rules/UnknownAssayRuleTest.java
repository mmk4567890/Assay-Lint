package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static com.mustafakhan.assaylint.rules.Fixtures.record;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class UnknownAssayRuleTest {

    private final UnknownAssayRule rule = new UnknownAssayRule();

    @Test
    void acceptsConfiguredAssay() {
        assertTrue(rule.check(List.of(record(2, "S1", "glucose", "95")), CONFIG).isEmpty());
    }

    @Test
    void warnsAboutUnconfiguredAssay() {
        List<ValidationIssue> issues = rule.check(List.of(record(2, "S1", "Glucose", "95")), CONFIG);

        assertEquals(1, issues.size());
        assertEquals(Severity.WARNING, issues.get(0).severity());
        assertTrue(issues.get(0).message().startsWith("Assay 'Glucose' is not defined in the config"));
    }

    @Test
    void leavesBlankAssayToRequiredFieldsRule() {
        assertTrue(rule.check(List.of(record(2, "S1", "", "95")), CONFIG).isEmpty());
    }
}
