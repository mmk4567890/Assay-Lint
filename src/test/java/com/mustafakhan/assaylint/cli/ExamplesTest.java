package com.mustafakhan.assaylint.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/** Guards the shipped example files so the README and real behaviour cannot drift apart. */
class ExamplesTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC);

    private final StringWriter out = new StringWriter();

    private int validateAsJson(String file) {
        CommandLine commandLine = AssayLint.commandLine(NOW);
        commandLine.setOut(new PrintWriter(out));
        return commandLine.execute("validate", "examples/" + file, "--config", "examples/assays.json",
                "--format", "json", "--fail-on", "warning");
    }

    @Test
    void cleanExamplePasses() {
        assertEquals(ExitCodes.OK, validateAsJson("samples_clean.csv"));
    }

    @Test
    void errorExampleTriggersEveryRule() throws IOException {
        assertEquals(ExitCodes.ISSUES_FOUND, validateAsJson("samples_with_errors.csv"));

        JsonNode byRule = new ObjectMapper().readTree(out.toString()).at("/summary/byRule");
        Set<String> rules = new TreeSet<>();
        byRule.fieldNames().forEachRemaining(rules::add);
        assertEquals(Set.of("Chronology", "DuplicateSampleId", "NumericValue", "Parse", "Range", "RequiredFields",
                "Timestamp", "Unit", "UnknownAssay"), rules);
    }

    @Test
    void instrumentExportReportsExactlyItsKnownProblems() throws IOException {
        assertEquals(ExitCodes.ISSUES_FOUND, validateAsJson("instrument_export.csv"));

        JsonNode root = new ObjectMapper().readTree(out.toString());
        List<String> issues = new ArrayList<>();
        for (JsonNode issue : root.get("issues")) {
            issues.add(issue.get("line").asInt() + " " + issue.get("severity").asText() + " "
                    + issue.get("rule").asText() + " " + issue.get("message").asText());
        }
        assertEquals(List.of(
                "1 WARNING Parse Ignoring unknown column 'instrument_id'",
                "1 WARNING Parse Ignoring unknown column 'operator_comment'",
                "1 WARNING Parse Ignoring unknown column 'qc_flag'",
                "1 WARNING Parse Ignoring unknown column 'run_id'",
                "8 ERROR Range Value 11.2 is outside the allowed range [1.5, 10] for assay 'potassium'",
                "14 ERROR Unit Unit 'mEq/L' does not match expected unit 'mmol/L' for assay 'sodium'",
                "19 ERROR DuplicateSampleId Duplicate sample_id 'P-1006' for assay 'glucose' (first seen on line 17)",
                "25 ERROR Timestamp Timestamp 2031-03-01T08:31:15 is in the future"), issues);
        assertEquals(25, root.at("/metadata/rows/read").asInt());
        assertEquals(25, root.at("/metadata/rows/parsed").asInt());
    }
}
