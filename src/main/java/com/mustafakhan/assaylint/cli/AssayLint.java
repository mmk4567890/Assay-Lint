package com.mustafakhan.assaylint.cli;

import java.time.Clock;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/** Top-level {@code assay-lint} command. Its only job is to dispatch to subcommands. */
@Command(
        name = "assay-lint",
        description = "Validates laboratory sample data files and reports data-integrity problems.",
        exitCodeListHeading = "%nExit codes:%n",
        exitCodeList = {
            "0:No issues at or above the --fail-on level",
            "1:Issues found at or above the --fail-on level",
            "2:Invalid arguments, input file or config"
        })
public final class AssayLint implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help and exit.")
    private boolean helpRequested;

    /**
     * Runs the tool and exits with its exit code.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(commandLine(Clock.systemUTC()).execute(args));
    }

    /**
     * Builds the configured command line. Exposed so tests can supply a fixed clock
     * and capture output.
     *
     * @param clock source of the current time for timestamp checks and report metadata
     * @return a ready-to-execute command line
     */
    public static CommandLine commandLine(Clock clock) {
        CommandLine commandLine = new CommandLine(new AssayLint());
        commandLine.addSubcommand("validate", new ValidateCommand(clock));
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        // An unexpected failure must not look like "issues found" to a CI gate.
        commandLine.setExecutionExceptionHandler((exception, failed, parseResult) -> {
            failed.getErr().println("Error: " + exception);
            return ExitCodes.BAD_INPUT;
        });
        return commandLine;
    }

    @Override
    public Integer call() {
        spec.commandLine().usage(spec.commandLine().getErr());
        return ExitCodes.BAD_INPUT;
    }
}
