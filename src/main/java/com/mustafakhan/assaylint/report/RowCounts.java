package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.ParseResult;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * How the data rows of a file break down. Rows that could not be parsed are
 * counted separately from parsed rows with issues, so neither number is inflated
 * by the other.
 *
 * @param read             non-blank data lines in the input, including malformed ones
 * @param parsed           data lines that could be parsed into records
 * @param parsedWithIssues parsed rows with at least one issue
 */
public record RowCounts(int read, int parsed, int parsedWithIssues) {

    /** Rejects counts that cannot describe the same file. */
    public RowCounts {
        if (parsedWithIssues < 0 || parsedWithIssues > parsed || parsed > read) {
            throw new IllegalArgumentException("inconsistent row counts: read=" + read + ", parsed=" + parsed
                    + ", parsedWithIssues=" + parsedWithIssues);
        }
    }

    /**
     * Counts rows for a parsed file and the issues found in it.
     *
     * @param result the parser output
     * @param issues every issue found, from parsing and from the rules
     * @return the row breakdown
     */
    public static RowCounts of(ParseResult result, List<ValidationIssue> issues) {
        Set<Integer> parsedLines = result.records().stream().map(SampleRecord::line).collect(Collectors.toSet());
        int withIssues = (int) issues.stream().map(ValidationIssue::line).filter(parsedLines::contains)
                .distinct().count();
        return new RowCounts(result.dataRows(), result.records().size(), withIssues);
    }

    /**
     * Returns the number of parsed rows without any issue.
     *
     * @return parsed rows that passed every check
     */
    public int parsedClean() {
        return parsed - parsedWithIssues;
    }

    /**
     * Returns the number of data lines that could not be parsed.
     *
     * @return rows lost to structural problems
     */
    public int unparsed() {
        return read - parsed;
    }

    /**
     * Describes the breakdown in one line, as shown in the console and HTML reports.
     *
     * @return for example {@code "14 read, 12 parsed (10 with issues, 2 clean), 2 could not be parsed"}
     */
    public String summary() {
        return read + " read, " + parsed + " parsed (" + parsedWithIssues + " with issues, " + parsedClean()
                + " clean), " + unparsed() + " could not be parsed";
    }
}
