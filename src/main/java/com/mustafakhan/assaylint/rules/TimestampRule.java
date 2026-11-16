package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Reports timestamps that are not ISO-8601 date-times, or that lie in the future.
 * Timestamps without an offset are read as UTC. "Now" comes from an injected
 * {@link Clock} so that the future check is deterministic in tests.
 */
public final class TimestampRule implements ValidationRule {

    private final Clock clock;

    /**
     * Creates the rule.
     *
     * @param clock source of the current time for the future check
     */
    public TimestampRule(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String name() {
        return "Timestamp";
    }

    @Override
    public List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config) {
        Instant now = clock.instant();
        List<ValidationIssue> issues = new ArrayList<>();
        for (SampleRecord record : records) {
            if (record.timestamp().isBlank()) {
                continue;
            }
            Optional<Instant> timestamp = Values.timestamp(record.timestamp());
            if (timestamp.isEmpty()) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Timestamp '" + record.timestamp() + "' is not a valid ISO-8601 date-time"));
            } else if (timestamp.get().isAfter(now)) {
                issues.add(ValidationIssue.of(record, name(), Severity.ERROR,
                        "Timestamp " + record.timestamp() + " is in the future"));
            }
        }
        return issues;
    }
}
