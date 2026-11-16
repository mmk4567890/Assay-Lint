package com.mustafakhan.assaylint.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationIssueTest {

    @Test
    void sortsByLineThenRule() {
        ValidationIssue late = new ValidationIssue(5, "S1", "A", Severity.ERROR, "m");
        ValidationIssue earlyB = new ValidationIssue(2, "S1", "B", Severity.ERROR, "m");
        ValidationIssue earlyA = new ValidationIssue(2, "S1", "A", Severity.WARNING, "m");
        List<ValidationIssue> issues = new ArrayList<>(List.of(late, earlyB, earlyA));

        issues.sort(ValidationIssue.BY_LINE);

        assertEquals(List.of(earlyA, earlyB, late), issues);
    }
}
