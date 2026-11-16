package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.IOException;
import java.util.Map;

/**
 * A single self-contained HTML file with a run summary and a sortable issues
 * table. All CSS and script are inline so the report can be archived or emailed
 * as one file and still render offline.
 */
public final class HtmlReportWriter implements ReportWriter {

    private static final String STYLE = """
            body{font-family:system-ui,-apple-system,Segoe UI,sans-serif;margin:2rem;color:#1f2933;background:#fff}
            h1{font-size:1.5rem;margin:0 0 1rem}
            h2{font-size:1.1rem;margin:2rem 0 .5rem}
            dl{display:grid;grid-template-columns:max-content 1fr;gap:.25rem 1rem;margin:0}
            dt{font-weight:600}dd{margin:0;font-family:ui-monospace,Consolas,monospace;word-break:break-all}
            .cards{display:flex;gap:1rem;flex-wrap:wrap}
            .card{border:1px solid #d9e2ec;border-radius:6px;padding:.75rem 1rem;min-width:7rem}
            .card b{display:block;font-size:1.6rem}
            .ERROR{color:#b42318}.WARNING{color:#a15c07}.ok{color:#067647}
            table{border-collapse:collapse;width:100%;margin-top:.5rem}
            th,td{text-align:left;padding:.4rem .6rem;border-bottom:1px solid #e4e7eb;vertical-align:top}
            th{background:#f5f7fa;cursor:pointer;user-select:none;white-space:nowrap}
            th:after{content:" \\2195";color:#9aa5b1}
            td.num{text-align:right;font-variant-numeric:tabular-nums}
            """;

    // Sorts by the clicked column; columns marked data-type="num" compare numerically.
    private static final String SCRIPT = """
            document.querySelectorAll('th').forEach(function (th, col) {
              th.addEventListener('click', function () {
                var body = th.closest('table').tBodies[0];
                var asc = th.dataset.dir !== 'asc';
                th.parentNode.querySelectorAll('th').forEach(function (h) { delete h.dataset.dir; });
                th.dataset.dir = asc ? 'asc' : 'desc';
                var numeric = th.dataset.type === 'num';
                Array.from(body.rows).sort(function (a, b) {
                  var x = a.cells[col].textContent, y = b.cells[col].textContent;
                  var c = numeric ? Number(x) - Number(y) : x.localeCompare(y);
                  return asc ? c : -c;
                }).forEach(function (row) { body.appendChild(row); });
              });
            });
            """;

    @Override
    public void write(Report report, Appendable out) throws IOException {
        out.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>assay-lint report: ").append(escape(report.metadata().inputFile())).append("</title>\n")
                .append("<style>\n").append(STYLE).append("</style>\n</head>\n<body>\n")
                .append("<h1>assay-lint report</h1>\n");
        writeMetadata(report, out);
        writeSummary(report, out);
        writeIssues(report, out);
        out.append("<script>\n").append(SCRIPT).append("</script>\n</body>\n</html>\n");
    }

    private static void writeMetadata(Report report, Appendable out) throws IOException {
        RunMetadata metadata = report.metadata();
        out.append("<dl>\n");
        definition(out, "Input file", metadata.inputFile());
        definition(out, "SHA-256", metadata.inputSha256());
        definition(out, "Config file", metadata.configFile());
        definition(out, "Run at (UTC)", metadata.runAt().toString());
        definition(out, "Rows", metadata.rows().summary());
        out.append("</dl>\n");
    }

    private static void definition(Appendable out, String term, String value) throws IOException {
        out.append("<dt>").append(term).append("</dt><dd>").append(escape(value)).append("</dd>\n");
    }

    private static void writeSummary(Report report, Appendable out) throws IOException {
        out.append("<h2>Summary</h2>\n<div class=\"cards\">\n");
        card(out, "", "Issues", report.issues().size());
        for (Map.Entry<Severity, Long> entry : report.countsBySeverity().entrySet()) {
            card(out, entry.getKey().name(), entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<String, Long> entry : report.countsByRule().entrySet()) {
            card(out, "", escape(entry.getKey()), entry.getValue());
        }
        out.append("</div>\n");
    }

    private static void card(Appendable out, String cssClass, String label, long count) throws IOException {
        out.append("<div class=\"card\"><b class=\"").append(cssClass).append("\">").append(String.valueOf(count))
                .append("</b>").append(label).append("</div>\n");
    }

    private static void writeIssues(Report report, Appendable out) throws IOException {
        out.append("<h2>Issues</h2>\n");
        if (report.issues().isEmpty()) {
            out.append("<p class=\"ok\">No issues found.</p>\n");
            return;
        }
        out.append("<table>\n<thead><tr><th data-type=\"num\">Line</th><th>Sample ID</th><th>Rule</th>")
                .append("<th>Severity</th><th>Message</th></tr></thead>\n<tbody>\n");
        for (ValidationIssue issue : report.issues()) {
            out.append("<tr><td class=\"num\">").append(String.valueOf(issue.line())).append("</td><td>")
                    .append(escape(issue.sampleId())).append("</td><td>").append(escape(issue.rule()))
                    .append("</td><td class=\"").append(issue.severity().name()).append("\">")
                    .append(issue.severity().name()).append("</td><td>").append(escape(issue.message()))
                    .append("</td></tr>\n");
        }
        out.append("</tbody>\n</table>\n");
    }

    static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&#39;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
