package com.mustafakhan.assaylint.parse;

/**
 * Thrown when a file's header row is present but unusable because a required
 * column is missing or repeated. Without a trustworthy column
 * mapping no row can be read safely, so this is treated as bad input rather than
 * as a data issue.
 */
public final class InvalidHeaderException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message a description of the problem, suitable for showing to the user
     */
    public InvalidHeaderException(String message) {
        super(message);
    }
}
