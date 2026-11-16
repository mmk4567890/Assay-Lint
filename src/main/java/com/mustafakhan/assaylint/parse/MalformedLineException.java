package com.mustafakhan.assaylint.parse;

/** Signals that a CSV line cannot be split into fields. */
final class MalformedLineException extends Exception {

    private static final long serialVersionUID = 1L;

    MalformedLineException(String message) {
        super(message);
    }
}
