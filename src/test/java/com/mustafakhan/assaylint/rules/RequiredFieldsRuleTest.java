package com.mustafakhan.assaylint.rules;

import static com.mustafakhan.assaylint.rules.Fixtures.CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class RequiredFieldsRuleTest {

    private final RequiredFieldsRule rule = new RequiredFieldsRule();

    @Test
    void passesCompleteRecord() {
        assertTrue(rule.check(List.of(Fixtures.record(2, "S1", "glucose", "95")), CONFIG).isEmpty());
    }

    @Test
    void listsAllMissingFieldsInOneIssue() {
        SampleRecord record = new SampleRecord(4, "S1", "glucose", "", "  ", "2026-01-05T10:00:00Z", "");

        List<ValidationIssue> issues = rule.check(List.of(record), CONFIG);

        assertEquals(1, issues.size());
        ValidationIssue issue = issues.get(0);
        assertEquals(4, issue.line());
        assertEquals("S1", issue.sampleId());
        assertEquals("RequiredFields", issue.rule());
        assertEquals(Severity.ERROR, issue.severity());
        assertEquals("Missing required field(s): value, unit, analyst", issue.message());
    }

    @Test
    void reportsMissingSampleId() {
        SampleRecord record = new SampleRecord(3, "", "glucose", "95", "mg/dL", "2026-01-05T10:00:00Z", "jdoe");

        assertEquals("Missing required field(s): sample_id", rule.check(List.of(record), CONFIG).get(0).message());
    }
}
