package com.mustafakhan.assaylint.cli;

/** Process exit codes, chosen so the tool can gate a CI pipeline. */
public final class ExitCodes {

    /** No issues at or above the {@code --fail-on} level. */
    public static final int OK = 0;

    /** At least one issue at or above the {@code --fail-on} level. */
    public static final int ISSUES_FOUND = 1;

    /** The command line, input file or config could not be used. */
    public static final int BAD_INPUT = 2;

    private ExitCodes() {
    }
}
