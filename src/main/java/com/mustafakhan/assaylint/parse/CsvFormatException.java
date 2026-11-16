package com.mustafakhan.assaylint.parse;

/**
 * Thrown when a file's structure is too broken to read any row safely: a header
 * that misses or repeats a required column, or a file that is not comma-separated
 * at all. These are treated as bad input rather than as data issues, because
 * every row would otherwise be reported for the same single cause.
 */
public final class CsvFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message a description of the problem, suitable for showing to the user
     */
    public CsvFormatException(String message) {
        super(message);
    }
}
