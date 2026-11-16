package com.mustafakhan.assaylint.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class ValidateCommandTest {

    private static final Clock NOW = Clock.fixed(Instant.parse("2026-03-01T12:00:00Z"), ZoneOffset.UTC);
    private static final String HEADER = "sample_id,assay,value,unit,timestamp,analyst\n";

    @TempDir
    Path dir;

    private Path config;
    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();

    @BeforeEach
    void writeConfig() throws IOException {
        config = Files.writeString(dir.resolve("assays.json"),
                "{\"assays\": {\"glucose\": {\"unit\": \"mg/dL\", \"min\": 0, \"max\": 600}}}");
    }

    private Path csv(String rows) throws IOException {
        return Files.writeString(dir.resolve("samples.csv"), HEADER + rows);
    }

    private int run(String... args) {
        CommandLine commandLine = AssayLint.commandLine(NOW);
        commandLine.setOut(new PrintWriter(out));
        commandLine.setErr(new PrintWriter(err));
        return commandLine.execute(args);
    }

    private int validate(Path file, String... options) {
        String[] args = new String[options.length + 4];
        args[0] = "validate";
        args[1] = file.toString();
        args[2] = "--config";
        args[3] = config.toString();
        System.arraycopy(options, 0, args, 4, options.length);
        return run(args);
    }

    @Test
    void cleanFileExitsZero() throws IOException {
        int code = validate(csv("S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe\n"));

        assertEquals(ExitCodes.OK, code);
        assertTrue(out.toString().contains("No issues found."));
    }

    @Test
    void errorsExitOne() throws IOException {
        int code = validate(csv("S1,glucose,9000,mg/dL,2026-01-05T10:00:00Z,jdoe\n"), "--no-color");

        assertEquals(ExitCodes.ISSUES_FOUND, code);
        assertTrue(out.toString().contains("Range"));
    }

    @Test
    void warningsOnlyPassByDefaultButFailWithFailOnWarning() throws IOException {
        Path file = csv("S1,albumin,4,g/dL,2026-01-05T10:00:00Z,jdoe\n");

        assertEquals(ExitCodes.OK, validate(file));
        assertEquals(ExitCodes.ISSUES_FOUND, validate(file, "--fail-on", "warning"));
    }

    @Test
    void usesInjectedClockForFutureCheckAndMetadata() throws IOException {
        int code = validate(csv("S1,glucose,95,mg/dL,2026-03-02T00:00:00Z,jdoe\n"), "--format", "json");

        assertEquals(ExitCodes.ISSUES_FOUND, code);
        assertTrue(out.toString().contains("\"runAt\" : \"2026-03-01T12:00:00Z\""), out.toString());
        assertTrue(out.toString().contains("is in the future"));
    }

    @Test
    void jsonFormatIsParseableAndCaseInsensitive() throws IOException {
        validate(csv("S1,glucose,9000,mg/dL,2026-01-05T10:00:00Z,jdoe\n"), "--format", "JSON");

        JsonNode root = new ObjectMapper().readTree(out.toString());
        assertEquals(1, root.at("/summary/total").asInt());
        assertEquals(64, root.at("/metadata/inputSha256").asText().length());
    }

    @Test
    void htmlReportIsWrittenToOutFile() throws IOException {
        Path report = dir.resolve("report.html");

        int code = validate(csv("S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe\n"),
                "--format", "html", "--out", report.toString());

        assertEquals(ExitCodes.OK, code);
        assertTrue(Files.readString(report).startsWith("<!DOCTYPE html>"));
        assertTrue(out.toString().contains("Report written to " + report));
    }

    @Test
    void consoleUsesColorUnlessDisabled() throws IOException {
        Path file = csv("S1,glucose,9000,mg/dL,2026-01-05T10:00:00Z,jdoe\n");

        validate(file);
        assertTrue(out.toString().contains("\u001B["));

        out.getBuffer().setLength(0);
        validate(file, "--no-color");
        assertFalse(out.toString().contains("\u001B["));
    }

    @Test
    void consoleReportWrittenToFileHasNoColor() throws IOException {
        Path report = dir.resolve("report.txt");

        validate(csv("S1,glucose,9000,mg/dL,2026-01-05T10:00:00Z,jdoe\n"), "--out", report.toString());

        assertFalse(Files.readString(report).contains("\u001B["));
    }

    @Test
    void missingConfigExitsTwo() throws IOException {
        Path file = csv("");

        int code = run("validate", file.toString(), "--config", dir.resolve("missing.json").toString());

        assertEquals(ExitCodes.BAD_INPUT, code);
        assertTrue(err.toString().contains("Config file not found"));
    }

    @Test
    void invalidConfigExitsTwo() throws IOException {
        Files.writeString(config, "{\"assays\": {\"glucose\": {\"unit\": \"mg/dL\", \"min\": 9, \"max\": 1}}}");

        assertEquals(ExitCodes.BAD_INPUT, validate(csv("")));
        assertTrue(err.toString().contains("min (9) is greater than max (1)"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "sample_id,assay,value,unit,timestamp",
        "sample_id,sample_id,assay,value,unit,timestamp,analyst"
    })
    void badHeaderExitsTwo(String header) throws IOException {
        Path file = Files.writeString(dir.resolve("bad-header.csv"),
                header + "\nS1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe\n");

        assertEquals(ExitCodes.BAD_INPUT, validate(file));
        assertTrue(err.toString().contains("Invalid header on line 1"), err.toString());
        assertEquals("", out.toString());
    }

    @Test
    void semicolonSeparatedFileExitsTwo() throws IOException {
        Path file = Files.writeString(dir.resolve("semicolons.csv"),
                "sample_id;assay;value;unit;timestamp;analyst\nS1;glucose;95;mg/dL;2026-01-05T10:00:00Z;jdoe\n");

        assertEquals(ExitCodes.BAD_INPUT, validate(file));
        assertTrue(err.toString().contains("File does not appear to be comma-separated (found ';')"), err.toString());
        assertEquals("", out.toString());
    }

    @Test
    void unknownColumnOnlyWarnsSoTheRunContinues() throws IOException {
        Path file = Files.writeString(dir.resolve("extra-column.csv"),
                "sample_id,assay,value,unit,timestamp,analyst,comment\n"
                        + "S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe,rerun\n");

        assertEquals(ExitCodes.OK, validate(file, "--no-color"));
        assertTrue(out.toString().contains("Ignoring unknown column 'comment'"), out.toString());
        assertEquals(ExitCodes.ISSUES_FOUND, validate(file, "--fail-on", "warning"));
    }

    @Test
    void missingInputExitsTwo() {
        assertEquals(ExitCodes.BAD_INPUT, validate(dir.resolve("missing.csv")));
        assertTrue(err.toString().contains("input file not found"));
    }

    @Test
    void unwritableOutputExitsTwo() throws IOException {
        Path report = dir.resolve("no-such-dir").resolve("report.html");

        assertEquals(ExitCodes.BAD_INPUT, validate(csv(""), "--out", report.toString()));
    }

    @Test
    void usageErrorsExitTwo() throws IOException {
        Path file = csv("");

        assertEquals(ExitCodes.BAD_INPUT, run("validate", file.toString()));
        assertEquals(ExitCodes.BAD_INPUT, validate(file, "--format", "xml"));
        assertEquals(ExitCodes.BAD_INPUT, validate(file, "--bogus"));
    }

    @Test
    void noSubcommandPrintsUsageAndExitsTwo() {
        assertEquals(ExitCodes.BAD_INPUT, run());
        assertTrue(err.toString().contains("validate"));
    }

    @Test
    void helpExitsZero() {
        assertEquals(ExitCodes.OK, run("validate", "--help"));
        assertTrue(out.toString().contains("--fail-on"));
    }
}
