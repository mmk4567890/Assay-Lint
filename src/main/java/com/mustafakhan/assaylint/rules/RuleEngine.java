package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/** Runs a fixed set of rules over a file's records and collects their issues in line order. */
public final class RuleEngine {

    private final List<ValidationRule> rules;

    /**
     * Creates an engine for the given rules.
     *
     * @param rules the rules to run, in the order they should be applied
     */
    public RuleEngine(List<ValidationRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Returns the standard rule set used by the command-line tool.
     *
     * @param clock source of the current time, used by time-dependent rules
     * @return all built-in rules
     */
    public static List<ValidationRule> defaultRules(Clock clock) {
        return List.of(new RequiredFieldsRule(), new DuplicateSampleIdRule(), new NumericValueRule(), new RangeRule(), new UnitRule(), new TimestampRule(clock), new ChronologyRule());
    }

    /**
     * Runs every rule and returns the combined issues sorted by line.
     *
     * @param records the parsed records
     * @param config  assay definitions to validate against
     * @return all issues found, sorted by {@link ValidationIssue#BY_LINE}
     */
    public List<ValidationIssue> run(List<SampleRecord> records, RuleConfig config) {
        List<ValidationIssue> issues = new ArrayList<>();
        for (ValidationRule rule : rules) {
            issues.addAll(rule.check(records, config));
        }
        issues.sort(ValidationIssue.BY_LINE);
        return issues;
    }
}
