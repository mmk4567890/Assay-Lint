package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ReportFormatTest {

    @Test
    void createsWriterForEachFormat() {
        assertInstanceOf(ConsoleReportWriter.class, ReportFormat.CONSOLE.writer(true));
        assertInstanceOf(JsonReportWriter.class, ReportFormat.JSON.writer(true));
        assertInstanceOf(HtmlReportWriter.class, ReportFormat.HTML.writer(true));
    }
}
