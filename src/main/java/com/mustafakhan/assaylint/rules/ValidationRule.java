package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;

/**
 * A single data-integrity check over the records of a sample file.
 *
 * <p>Rules receive every record at once so that cross-row checks (duplicates,
 * ordering) fit the same contract as per-row checks. A rule should report one
 * issue per root cause and skip values another rule is responsible for, such
 * as blank fields, so that one bad cell does not produce a cascade of issues.
 */
public interface ValidationRule {

    /**
     * Returns the short name shown in reports, for example {@code "Range"}.
     *
     * @return the rule name
     */
    String name();

    /**
     * Checks the records and returns any problems found.
     *
     * @param records the parsed records, in file order
     * @param config  assay definitions to validate against
     * @return the issues found, empty if the records pass
     */
    List<ValidationIssue> check(List<SampleRecord> records, RuleConfig config);
}
