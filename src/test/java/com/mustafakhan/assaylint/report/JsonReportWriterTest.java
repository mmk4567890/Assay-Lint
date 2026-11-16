package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class JsonReportWriterTest {

    private static JsonNode render(Report report) throws IOException {
        StringBuilder out = new StringBuilder();
        new JsonReportWriter().write(report, out);
        return new ObjectMapper().readTree(out.toString());
    }

    @Test
    void writesMetadataForTraceability() throws IOException {
        JsonNode metadata = render(Reports.withIssues()).get("metadata");

        assertEquals("data/samples.csv", metadata.get("inputFile").asText());
        assertEquals("ab12cd34", metadata.get("inputSha256").asText());
        assertEquals("config/assays.json", metadata.get("configFile").asText());
        assertEquals("2026-02-01T09:30:00Z", metadata.get("runAt").asText());
        assertEquals(3, metadata.at("/rows/read").asInt());
        assertEquals(2, metadata.at("/rows/parsed").asInt());
        assertEquals(1, metadata.at("/rows/parsedWithIssues").asInt());
        assertEquals(1, metadata.at("/rows/parsedClean").asInt());
        assertEquals(1, metadata.at("/rows/unparsed").asInt());
    }

    @Test
    void writesSummaryCounts() throws IOException {
        JsonNode summary = render(Reports.withIssues()).get("summary");

        assertEquals(4, summary.get("total").asInt());
        assertEquals(2, summary.at("/bySeverity/ERROR").asInt());
        assertEquals(2, summary.at("/bySeverity/WARNING").asInt());
        assertEquals(1, summary.at("/byRule/Range").asInt());
    }

    @Test
    void writesEveryIssueWithAllFields() throws IOException {
        JsonNode issues = render(Reports.withIssues()).get("issues");

        assertEquals(4, issues.size());
        JsonNode first = issues.get(1);
        assertEquals(2, first.get("line").asInt());
        assertEquals("S1", first.get("sampleId").asText());
        assertEquals("Range", first.get("rule").asText());
        assertEquals("ERROR", first.get("severity").asText());
        assertEquals("Value 700 is outside the range", first.get("message").asText());
    }

    @Test
    void cleanReportHasEmptyIssueArray() throws IOException {
        JsonNode root = render(Reports.clean());

        assertEquals(0, root.get("issues").size());
        assertEquals(0, root.at("/summary/bySeverity/ERROR").asInt());
    }
}
