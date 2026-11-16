package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mustafakhan.assaylint.model.ParseResult;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.util.List;
import org.junit.jupiter.api.Test;

class RowCountsTest {

    private static SampleRecord row(int line) {
        return new SampleRecord(line, "S" + line, "glucose", "95", "mg/dL", "2026-01-05T10:00:00Z", "jdoe");
    }

    private static ValidationIssue issue(int line) {
        return new ValidationIssue(line, "", "Rule", Severity.ERROR, "problem");
    }

    @Test
    void countsOnlyParsedRowsAsRowsWithIssues() {
        ParseResult parsed = new ParseResult(List.of(row(2), row(3), row(4)), List.of(), 4);

        RowCounts counts = RowCounts.of(parsed, List.of(
                issue(1),    // header warning: not a data row
                issue(2),
                issue(2),    // a second issue on the same row
                issue(5)));  // a row that could not be parsed

        assertEquals(new RowCounts(4, 3, 1), counts);
        assertEquals(2, counts.parsedClean());
        assertEquals(1, counts.unparsed());
    }

    @Test
    void summarisesTheBreakdown() {
        assertEquals("14 read, 12 parsed (10 with issues, 2 clean), 2 could not be parsed",
                new RowCounts(14, 12, 10).summary());
    }

    @Test
    void rejectsInconsistentCounts() {
        assertThrows(IllegalArgumentException.class, () -> new RowCounts(2, 3, 0));
        assertThrows(IllegalArgumentException.class, () -> new RowCounts(3, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> new RowCounts(3, 2, -1));
    }
}
