package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Warns about assays that are not defined in the config. Such rows cannot be
 * checked for unit or range, so the warning makes that gap visible.
 */
public final class UnknownAssayRule implements ValidationRule {

    @Override
    public String name() {
        return "UnknownAssay";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            if (!record.assay().isBlank() && config.find(record.assay()).isEmpty()) {
                issues.add(ValidationIssue.of(record, name(), Severity.WARNING,
                        "Assay '" + record.assay() + "' is not defined in the config; unit and range were not checked"));
            }
        }
        return issues;
    }
}
