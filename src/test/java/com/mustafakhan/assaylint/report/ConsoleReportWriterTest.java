package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConsoleReportWriterTest {

    private static String render(Report report, boolean color) throws IOException {
        StringBuilder out = new StringBuilder();
        new ConsoleReportWriter(color).write(report, out);
        return out.toString();
    }

    @Test
    void includesTraceabilityDetails() throws IOException {
        String text = render(Reports.withIssues(), false);

        assertTrue(text.startsWith("Assay Lint report" + System.lineSeparator()), text);
        assertTrue(text.contains("Input:   data/samples.csv"));
        assertTrue(text.contains("SHA-256: ab12cd34"));
        assertTrue(text.contains("Config:  config/assays.json"));
        assertTrue(text.contains("Run at:  2026-02-01T09:30:00Z"));
        assertTrue(text.contains("Rows:    3 read, 2 parsed (1 with issues, 1 clean), 1 could not be parsed"), text);
    }

    @Test
    void summarisesBySeverityAndRule() throws IOException {
        String text = render(Reports.withIssues(), false);

        assertTrue(text.contains("Summary: 4 issue(s)  2 error(s), 2 warning(s)"), text);
        assertTrue(text.contains("  Unknown assay  1"), text);
    }

    @Test
    void groupsIssuesUnderTheirLine() throws IOException {
        String text = render(Reports.withIssues(), false);
        String nl = System.lineSeparator();

        assertTrue(text.contains("Line 2 (S1)" + nl
                + "  ERROR    Range          Value 700 is outside the range" + nl
                + "  WARNING  Unknown assay  Assay <x> & \"y\" is unknown" + nl), text);
        assertTrue(text.contains("File" + nl + "  WARNING  Parse"), text);
    }

    @Test
    void noColorOutputHasNoEscapeCodes() throws IOException {
        assertFalse(render(Reports.withIssues(), false).contains("\u001B["));
    }

    @Test
    void colorOutputHighlightsSeverities() throws IOException {
        String text = render(Reports.withIssues(), true);

        assertTrue(text.contains("\u001B[31mERROR  "));
        assertTrue(text.contains("\u001B[33mWARNING"));
    }

    @Test
    void cleanReportSaysSo() throws IOException {
        String text = render(Reports.clean(), false);

        assertTrue(text.contains("No issues found."));
        assertFalse(text.contains("Summary"));
    }
}
