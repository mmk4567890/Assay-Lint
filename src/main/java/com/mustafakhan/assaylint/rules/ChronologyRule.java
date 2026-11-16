package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Warns when a sample's measurements are not in chronological order in the file.
 * Each row is compared with the latest timestamp seen so far for the same sample,
 * so a single back-dated entry is reported once rather than making every later
 * row look out of order. Equal timestamps are allowed. Invalid timestamps are
 * left to {@link TimestampRule}.
 */
public final class ChronologyRule implements ValidationRule {

    @Override
    public String name() {
        return "Chronology";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        Map<String, SampleRecord> latestBySample = new HashMap<>();
        Map<String, Instant> latestTime = new HashMap<>();
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            Optional<Instant> time = Values.timestamp(record.timestamp());
            if (record.sampleId().isBlank() || time.isEmpty()) {
                continue;
            }
            Instant previous = latestTime.get(record.sampleId());
            if (previous != null && time.get().isBefore(previous)) {
                SampleRecord earlier = latestBySample.get(record.sampleId());
                issues.add(ValidationIssue.of(record, name(), Severity.WARNING,
                        "Timestamp " + record.timestamp() + " is earlier than " + earlier.timestamp()
                                + " recorded for sample '" + record.sampleId() + "' on line " + earlier.line()));
            } else {
                latestTime.put(record.sampleId(), time.get());
                latestBySample.put(record.sampleId(), record);
            }
        }
        return issues;
    }
}
