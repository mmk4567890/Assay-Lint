package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Instant;
import java.util.List;

/** Sample reports shared by the report writer tests. */
final class Reports {

    static final RunMetadata METADATA = new RunMetadata("data/samples.csv", "ab12cd34", "config/assays.json", 3, 2,
            Instant.parse("2026-02-01T09:30:00.750Z"));

    private Reports() {
    }

    static Report withIssues() {
        return new Report(METADATA, List.of(
                new ValidationIssue(4, "S2", "Unit", Severity.ERROR, "Unit 'mg/dl' does not match"),
                new ValidationIssue(2, "S1", "Range", Severity.ERROR, "Value 700 is outside the range"),
                new ValidationIssue(2, "S1", "UnknownAssay", Severity.WARNING, "Assay <x> & \"y\" is unknown"),
                new ValidationIssue(0, "", "Parse", Severity.WARNING, "File contains no data rows")));
    }

    static Report clean() {
        return new Report(METADATA, List.of());
    }
}
