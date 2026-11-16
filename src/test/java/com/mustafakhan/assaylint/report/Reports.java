package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Instant;
import java.util.List;

/** Sample reports shared by the report writer tests. */
final class Reports {

    static final String FULL_HASH = "9350be05f3b862cfa290661a7326c43fe1c638c6013625dd0b465231838e5974";
    static final RunMetadata METADATA = metadata("data/samples.csv", "ab12cd34", Severity.ERROR);

    private Reports() {
    }

    static RunMetadata metadata(String inputFile, String sha256, Severity failOn) {
        return new RunMetadata(inputFile, sha256, "config/assays.json", new RowCounts(3, 2, 1), failOn, "9.9.9",
                Instant.parse("2026-02-01T09:30:00.750Z"));
    }

    static Report withIssues() {
        return new Report(METADATA, List.of(
                new ValidationIssue(4, "S2", "Unit", Severity.ERROR, "Unit 'mg/dl' does not match"),
                new ValidationIssue(2, "S1", "Range", Severity.ERROR, "Value 700 is outside the range"),
                new ValidationIssue(2, "S1", "UnknownAssay", Severity.WARNING, "Assay <x> & \"y\" is unknown"),
                new ValidationIssue(0, "", "Parse", Severity.WARNING, "File contains no data rows")));
    }

    static Report warningsOnly(Severity failOn) {
        return new Report(metadata("data/samples.csv", "ab12cd34", failOn), List.of(
                new ValidationIssue(2, "S1", "UnknownAssay", Severity.WARNING, "Assay 'x' is unknown"),
                new ValidationIssue(3, "S1", "Chronology", Severity.WARNING, "Timestamp is earlier")));
    }

    static Report clean() {
        return new Report(METADATA, List.of());
    }
}
