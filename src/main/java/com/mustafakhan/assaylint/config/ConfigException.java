package com.mustafakhan.assaylint.config;

/** Thrown when a config file is missing, unreadable or does not describe valid assays. */
public final class ConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message a description of the problem, suitable for showing to the user
     */
    public ConfigException(String message) {
        super(message);
    }
}
