package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.AssayDefinition;
import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reports units that differ from the configured unit for the assay. The match is
 * exact and case-sensitive: unit symbols such as {@code mg/dL} and {@code mmol/L}
 * use case meaningfully, so a case difference is treated as a real mismatch.
 */
public final class UnitRule implements ValidationRule {

    @Override
    public String name() {
        return "Unit";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            Optional<AssayDefinition> assay = config.find(record.assay());
            if (assay.isPresent() && !record.unit().isBlank() && !record.unit().equals(assay.get().unit())) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Unit '" + record.unit() + "' does not match expected unit '" + assay.get().unit()
                                + "' for assay '" + record.assay() + "'"));
            }
        }
        return issues;
    }
}
