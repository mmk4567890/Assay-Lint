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
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/** Guards the shipped example files: the clean one must pass and the other must trip every rule. */
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
}
