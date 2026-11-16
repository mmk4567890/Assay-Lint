package com.mustafakhan.assaylint.model;

import java.util.List;

/**
 * Outcome of parsing a sample file: the rows that could be read, plus any
 * structural problems found along the way.
 *
 * @param records  rows that were parsed into records
 * @param issues   structural problems such as wrong column counts
 * @param dataRows number of non-blank data lines seen, including malformed ones
 */
public record ParseResult(List<SampleRecord> records, List<ValidationIssue> issues, int dataRows) {

    /** Makes defensive, unmodifiable copies of both lists. */
    public ParseResult {
        records = List.copyOf(records);
        issues = List.copyOf(issues);
    }
}
