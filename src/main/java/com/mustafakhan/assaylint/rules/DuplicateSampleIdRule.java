package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports rows that repeat a sample ID and assay combination already seen earlier
 * in the file. The first occurrence is treated as the original and every later
 * one is flagged with a pointer back to it.
 */
public final class DuplicateSampleIdRule implements ValidationRule {

    @Override
    public String name() {
        return "DuplicateSampleId";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        Map<List<String>, Integer> firstSeen = new HashMap<>();
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            if (record.sampleId().isBlank() || record.assay().isBlank()) {
                continue;
            }
            List<String> key = List.of(record.sampleId(), record.assay());
            Integer firstLine = firstSeen.putIfAbsent(key, record.line());
            if (firstLine != null) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Duplicate sample_id '" + record.sampleId() + "' for assay '" + record.assay()
                                + "' (first seen on line " + firstLine + ")"));
            }
        }
        return issues;
    }
}
