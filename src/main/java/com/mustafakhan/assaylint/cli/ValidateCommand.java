package com.mustafakhan.assaylint.cli;

import com.mustafakhan.assaylint.config.ConfigException;
import com.mustafakhan.assaylint.config.ConfigLoader;
import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.ParseResult;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import com.mustafakhan.assaylint.parse.CsvParser;
import com.mustafakhan.assaylint.parse.InvalidHeaderException;
import com.mustafakhan.assaylint.report.FileHasher;
import com.mustafakhan.assaylint.report.Report;
import com.mustafakhan.assaylint.report.ReportFormat;
import com.mustafakhan.assaylint.report.RunMetadata;
import com.mustafakhan.assaylint.rules.RuleEngine;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/** The {@code validate} subcommand: parse a sample file, run every rule and write a report. */
@Command(name = "validate", description = "Validate a sample CSV file against an assay config.",
        sortOptions = false)
public final class ValidateCommand implements Callable<Integer> {

    private final Clock clock;

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", paramLabel = "<file.csv>", description = "Sample data file to validate.")
    private Path input;

    @Option(names = "--config", required = true, paramLabel = "<assays.json>",
            description = "Assay definitions (JSON).")
    private Path config;

    @Option(names = "--format", defaultValue = "console", paramLabel = "<format>",
            description = "Report format: console, json or html (default: ${DEFAULT-VALUE}).")
    private ReportFormat format;

    @Option(names = "--out", paramLabel = "<path>", description = "Write the report to a file instead of stdout.")
    private Path out;

    @Option(names = "--no-color", description = "Disable ANSI colors in console output.")
    private boolean noColor;

    @Option(names = "--fail-on", defaultValue = "error", paramLabel = "<level>",
            description = "Lowest severity that causes exit code 1: warning or error (default: ${DEFAULT-VALUE}).")
    private Severity failOn;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help and exit.")
    private boolean helpRequested;

    /**
     * Creates the command.
     *
     * @param clock source of the current time for timestamp checks and report metadata
     */
    public ValidateCommand(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Integer call() {
        PrintWriter err = spec.commandLine().getErr();
        try {
            RuleConfig rules = new ConfigLoader().load(config);
            if (!Files.isRegularFile(input)) {
                err.println("Error: input file not found: " + input);
                return ExitCodes.BAD_INPUT;
            }
            Report report = validate(rules);
            writeReport(report);
            return report.hasIssuesAtLeast(failOn) ? ExitCodes.ISSUES_FOUND : ExitCodes.OK;
        } catch (ConfigException e) {
            err.println("Error: " + e.getMessage());
            return ExitCodes.BAD_INPUT;
        } catch (InvalidHeaderException e) {
            err.println("Error: " + input + ": " + e.getMessage());
            return ExitCodes.BAD_INPUT;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return ExitCodes.BAD_INPUT;
        }
    }

    private Report validate(RuleConfig rules) throws IOException, InvalidHeaderException {
        String sha256 = FileHasher.sha256(input);
        ParseResult parsed = new CsvParser().parse(input);
        List<ValidationIssue> issues = new ArrayList<>(parsed.issues());
        issues.addAll(new RuleEngine(RuleEngine.defaultRules(clock)).run(parsed.records(), rules));
        RunMetadata metadata = new RunMetadata(input.toString(), sha256, config.toString(),
                parsed.dataRows(), parsed.records().size(), clock.instant());
        return new Report(metadata, issues);
    }

    private void writeReport(Report report) throws IOException {
        boolean color = !noColor && out == null;
        if (out == null) {
            PrintWriter stdout = spec.commandLine().getOut();
            format.writer(color).write(report, stdout);
            stdout.flush();
            return;
        }
        try (Writer file = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            format.writer(color).write(report, file);
        }
        spec.commandLine().getOut().println("Report written to " + out);
    }
}
