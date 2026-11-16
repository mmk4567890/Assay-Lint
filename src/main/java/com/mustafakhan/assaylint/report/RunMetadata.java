package com.mustafakhan.assaylint.report;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Facts about a validation run that make its result traceable: exactly which
 * input and config were checked, what was in it, and when.
 *
 * @param inputFile   path of the validated CSV file, as given on the command line
 * @param inputSha256 SHA-256 of the input file's bytes, in lowercase hex
 * @param configFile  path of the assay config used
 * @param rows        how the file's data rows break down
 * @param runAt       when the run happened, truncated to whole seconds
 */
public record RunMetadata(
        String inputFile,
        String inputSha256,
        String configFile,
        RowCounts rows,
        Instant runAt) {

    /** Rejects null fields and truncates the run time to seconds. */
    public RunMetadata {
        Objects.requireNonNull(inputFile, "inputFile");
        Objects.requireNonNull(inputSha256, "inputSha256");
        Objects.requireNonNull(configFile, "configFile");
        Objects.requireNonNull(rows, "rows");
        runAt = runAt.truncatedTo(ChronoUnit.SECONDS);
    }
}
