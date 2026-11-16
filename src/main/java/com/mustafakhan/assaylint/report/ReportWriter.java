package com.mustafakhan.assaylint.report;

import java.io.IOException;

/** Renders a {@link Report} in one output format. */
public interface ReportWriter {

    /**
     * Writes the report.
     *
     * @param report the report to render
     * @param out    destination for the rendered text
     * @throws IOException if writing fails
     */
    void write(Report report, Appendable out) throws IOException;
}
