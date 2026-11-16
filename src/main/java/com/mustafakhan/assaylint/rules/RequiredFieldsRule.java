package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reports rows where any required field is empty, listing every missing field in one issue. */
public final class RequiredFieldsRule implements ValidationRule {

    @Override
    public String name() {
        return "RequiredFields";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            List<String> missing = missingFields(record);
            if (!missing.isEmpty()) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Missing required field(s): " + String.join(", ", missing)));
            }
        }
        return issues;
    }

    private static List<String> missingFields(SampleRecord record) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("sample_id", record.sampleId());
        fields.put("assay", record.assay());
        fields.put("value", record.value());
        fields.put("unit", record.unit());
        fields.put("timestamp", record.timestamp());
        fields.put("analyst", record.analyst());

        List<String> missing = new ArrayList<>();
        fields.forEach((column, value) -> {
            if (value.isBlank()) {
                missing.add(column);
            }
        });
        return missing;
    }
}
