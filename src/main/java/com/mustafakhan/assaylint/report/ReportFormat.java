package com.mustafakhan.assaylint.report;

/** The output formats the tool can produce. */
public enum ReportFormat {
    CONSOLE,
    JSON,
    HTML;

    /**
     * Creates the writer for this format.
     *
     * @param color whether console output may use ANSI colors; ignored by other formats
     * @return a writer for this format
     */
    public ReportWriter writer(boolean color) {
        return switch (this) {
            case CONSOLE -> new ConsoleReportWriter(color);
            case JSON -> new JsonReportWriter();
            case HTML -> new HtmlReportWriter();
        };
    }
}
