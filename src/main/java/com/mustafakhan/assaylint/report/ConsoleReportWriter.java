package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.RuleNames;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Human-readable terminal report: run details, a summary by severity and by rule,
 * then every issue grouped under its line. ANSI colors can be turned off for
 * terminals and CI logs that do not render them.
 */
public final class ConsoleReportWriter implements ReportWriter {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String DIM = "\u001B[2m";
    private static final String NL = System.lineSeparator();

    private final boolean color;

    /**
     * Creates the writer.
     *
     * @param color whether to emit ANSI color codes
     */
    public ConsoleReportWriter(boolean color) {
        this.color = color;
    }

    @Override
    public void write(Report report, Appendable out) throws IOException {
        writeHeader(report.metadata(), out);
        if (report.issues().isEmpty()) {
            out.append(style(GREEN + BOLD, "No issues found.")).append(NL);
            return;
        }
        writeSummary(report, out);
        writeIssues(report.issues(), out);
    }

    private void writeHeader(RunMetadata metadata, Appendable out) throws IOException {
        out.append(style(BOLD, "assay-lint report")).append(NL)
                .append("  Input:   ").append(metadata.inputFile()).append(NL)
                .append("  SHA-256: ").append(metadata.inputSha256()).append(NL)
                .append("  Config:  ").append(metadata.configFile()).append(NL)
                .append("  Run at:  ").append(metadata.runAt().toString()).append(NL)
                .append("  Rows:    ").append(metadata.rows().summary()).append(NL)
                .append(NL);
    }

    private void writeSummary(Report report, Appendable out) throws IOException {
        Map<Severity, Long> bySeverity = report.countsBySeverity();
        out.append(style(BOLD, "Summary: " + report.issues().size() + " issue(s)")).append("  ")
                .append(style(RED, bySeverity.get(Severity.ERROR) + " error(s)")).append(", ")
                .append(style(YELLOW, bySeverity.get(Severity.WARNING) + " warning(s)")).append(NL);
        int width = report.countsByRule().keySet().stream().map(RuleNames::displayName).mapToInt(String::length)
                .max().orElse(0);
        for (Map.Entry<String, Long> entry : report.countsByRule().entrySet()) {
            out.append("  ").append(pad(RuleNames.displayName(entry.getKey()), width)).append("  ")
                    .append(String.valueOf(entry.getValue())).append(NL);
        }
        out.append(NL);
    }

    private void writeIssues(List<ValidationIssue> issues, Appendable out) throws IOException {
        Map<Integer, List<ValidationIssue>> byLine = issues.stream()
                .collect(Collectors.groupingBy(ValidationIssue::line, LinkedHashMap::new, Collectors.toList()));
        int ruleWidth = issues.stream().mapToInt(issue -> issue.ruleDisplayName().length()).max().orElse(0);
        for (Map.Entry<Integer, List<ValidationIssue>> group : byLine.entrySet()) {
            out.append(style(BOLD, lineLabel(group.getKey(), group.getValue().get(0).sampleId()))).append(NL);
            for (ValidationIssue issue : group.getValue()) {
                out.append("  ").append(severityLabel(issue.severity())).append("  ")
                        .append(pad(issue.ruleDisplayName(), ruleWidth)).append("  ")
                        .append(issue.message()).append(NL);
            }
        }
    }

    private String lineLabel(int line, String sampleId) {
        String label = line > 0 ? "Line " + line : "File";
        return sampleId.isEmpty() ? label : label + style(DIM, " (" + sampleId + ")");
    }

    private String severityLabel(Severity severity) {
        String text = pad(severity.name(), Severity.WARNING.name().length());
        return style(severity == Severity.ERROR ? RED : YELLOW, text);
    }

    private String style(String codes, String text) {
        return color ? codes + text + RESET : text;
    }

    private static String pad(String text, int width) {
        return String.format("%-" + Math.max(width, 1) + "s", text);
    }
}
