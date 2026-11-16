package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Reports values that are not finite decimal numbers. Blank values are left to
 * {@link RequiredFieldsRule}.
 */
public final class NumericValueRule implements ValidationRule {

    @Override
    public String name() {
        return "NumericValue";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            if (!record.value().isBlank() && Values.number(record.value()).isEmpty()) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Value '" + record.value() + "' is not a finite number"));
            }
        }
        return issues;
    }
}
