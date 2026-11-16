package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class HtmlReportWriterTest {

    private static String render(Report report) throws IOException {
        StringBuilder out = new StringBuilder();
        new HtmlReportWriter().write(report, out);
        return out.toString();
    }

    @Test
    void isSelfContained() throws IOException {
        String html = render(Reports.withIssues());

        assertTrue(html.startsWith("<!DOCTYPE html>"));
        assertTrue(html.contains("<style>"));
        assertFalse(html.contains("<link"));
        assertFalse(html.contains("src="));
        assertFalse(html.contains("http://"));
        assertFalse(html.contains("https://"));
    }

    @Test
    void includesMetadataAndSummary() throws IOException {
        String html = render(Reports.withIssues());

        assertTrue(html.contains("<dd>ab12cd34</dd>"));
        assertTrue(html.contains("<dd>config/assays.json</dd>"));
        assertTrue(html.contains("<dd>2026-02-01T09:30:00Z</dd>"));
        assertTrue(html.contains("<dd>3 read, 2 parsed, 2 with issues</dd>"));
        assertTrue(html.contains("<b class=\"ERROR\">2</b>ERROR"));
    }

    @Test
    void rendersSortableIssueTable() throws IOException {
        String html = render(Reports.withIssues());

        assertTrue(html.contains("<th data-type=\"num\">Line</th>"));
        assertTrue(html.contains("addEventListener('click'"));
        assertEquals(4, html.split("<tr><td", -1).length - 1);
    }

    @Test
    void escapesUserControlledText() throws IOException {
        String html = render(Reports.withIssues());

        assertTrue(html.contains("Assay &lt;x&gt; &amp; &quot;y&quot; is unknown"));
        assertFalse(html.contains("Assay <x>"));
    }

    @Test
    void cleanReportSaysSo() throws IOException {
        String html = render(Reports.clean());

        assertTrue(html.contains("No issues found."));
        assertFalse(html.contains("<table>"));
    }

    @Test
    void escapeHandlesQuotes() {
        assertEquals("it&#39;s", HtmlReportWriter.escape("it's"));
    }
}
