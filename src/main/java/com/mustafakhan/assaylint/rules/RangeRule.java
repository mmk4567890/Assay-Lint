package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.AssayDefinition;
import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Reports values outside the configured range for their assay. Bounds are
 * inclusive. Unknown assays and non-numeric values are left to their own rules.
 */
public final class RangeRule implements ValidationRule {

    @Override
    public String name() {
        return "Range";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            Optional<AssayDefinition> assay = config.find(record.assay());
            OptionalDouble value = Values.number(record.value());
            if (assay.isPresent() && value.isPresent() && !assay.get().contains(value.getAsDouble())) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR, outOfRange(record, assay.get())));
            }
        }
        return issues;
    }

    private static String outOfRange(SampleRecord record, AssayDefinition assay) {
        return "Value " + record.value() + " is outside the allowed range [" + Values.format(assay.min())
                + ", " + Values.format(assay.max()) + "] for assay '" + record.assay() + "'";
    }
}
