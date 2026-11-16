package com.mustafakhan.assaylint.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RuleNamesTest {

    @ParameterizedTest
    @CsvSource({
        "DuplicateSampleId, Duplicate sample ID",
        "RequiredFields, Required fields",
        "NumericValue, Numeric value",
        "Range, Range",
        "Unit, Unit",
        "Timestamp, Timestamp",
        "Chronology, Chronology",
        "UnknownAssay, Unknown assay",
        "Parse, Parse"
    })
    void convertsRuleIdentifiersToSentenceCase(String ruleId, String displayName) {
        assertEquals(displayName, RuleNames.displayName(ruleId));
    }

    @Test
    void issueExposesItsRuleDisplayName() {
        ValidationIssue issue = new ValidationIssue(2, "S1", "DuplicateSampleId", Severity.ERROR, "message");

        assertEquals("Duplicate sample ID", issue.ruleDisplayName());
    }
}
