package com.mustafakhan.assaylint.report;

import com.mustafakhan.assaylint.model.RuleNames;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * A single self-contained HTML file: a pass/fail banner, run details, summary
 * counts and a sortable issues table. All CSS and script are inline so the report
 * can be archived or emailed as one file and still render offline.
 */
public final class HtmlReportWriter implements ReportWriter {

    private static final int SHORT_HASH_LENGTH = 12;
    private static final String SEPARATOR = " \u00B7";
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT).withZone(ZoneOffset.UTC);

    // Decorative flask-with-checkmark icon; hidden from assistive technology because the
    // wordmark next to it already names the tool. No xmlns: inline SVG in HTML needs none.
    private static final String BRAND_MARK = "<svg class=\"brand-mark\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\""
            + " fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\""
            + " stroke-linejoin=\"round\" aria-hidden=\"true\" focusable=\"false\">"
            + "<path d=\"M8.5 3h7\"/>"
            + "<path d=\"M10 3v6.2L4.6 18.4A1.8 1.8 0 0 0 6.2 21h11.6a1.8 1.8 0 0 0 1.6-2.6L14 9.2V3\"/>"
            + "<path d=\"M9 15.5l2 2 4-4\"/>"
            + "</svg>";

    private static final String STYLE = """
            :root{--accent:#4338ca;--text:#111827;--muted:#4b5563;--border:#e5e7eb;--surface:#fff;--page:#f9fafb;
            --stripe:#f9fafb;--hover:#f1f5f9;--error:#b42318;--error-bg:#fef3f2;--error-border:#fecdca;
            --warning:#93370d;--warning-bg:#fffaeb;--warning-border:#fedf89;--ok:#067647;--ok-bg:#ecfdf3;
            --ok-border:#abefc6;--radius:8px;--s1:8px;--s2:16px;--s3:24px;--s4:32px;
            --font:system-ui,-apple-system,"Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif;
            --mono:ui-monospace,SFMono-Regular,Menlo,Consolas,"Liberation Mono",monospace}
            *{box-sizing:border-box}
            body{margin:0;font:15px/1.5 var(--font);color:var(--text);background:var(--page);
            -webkit-print-color-adjust:exact;print-color-adjust:exact}
            .wrap{max-width:1100px;margin:0 auto;padding:0 var(--s2)}
            .topbar{background:var(--surface);border-bottom:1px solid var(--border)}
            .topbar .wrap{display:flex;justify-content:space-between;align-items:center;gap:var(--s2);
            min-height:64px;padding-top:var(--s1);padding-bottom:var(--s1)}
            .brand{display:flex;align-items:center;gap:12px;min-width:0}
            .brand-logo{flex:none;display:grid;place-items:center;width:36px;height:36px;border-radius:8px;
            background:var(--accent);color:#fff}
            .brand-mark{display:block}
            .brand-text{display:flex;flex-direction:column;min-width:0}
            .wordmark{font-size:22px;line-height:1.2;font-weight:700;letter-spacing:-.02em;color:var(--text)}
            .brand-subtitle{margin-top:2px;font-size:13px;line-height:1.3;color:var(--muted)}
            .version{flex:none;padding:2px var(--s1);border:1px solid var(--border);border-radius:999px;background:var(--page);
            color:var(--muted);font-size:12px;font-weight:600;line-height:16px;white-space:nowrap}
            main.wrap{display:grid;gap:var(--s3);padding-top:var(--s3);padding-bottom:var(--s4)}
            h2{font-size:16px;font-weight:600;margin:0 0 var(--s2)}
            .card{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:var(--s3)}
            .banner{display:flex;align-items:center;gap:var(--s2);padding:var(--s3);border:1px solid;border-radius:var(--radius)}
            .banner-icon{flex:none;display:grid;place-items:center;width:40px;height:40px;border-radius:50%;
            color:#fff;font-size:20px;font-weight:700}
            .banner-title{margin:0;font-size:24px;line-height:1.25;font-weight:700}
            .banner-detail{margin:0;color:var(--muted)}
            .banner-failed{background:var(--error-bg);border-color:var(--error-border)}
            .banner-failed .banner-title{color:var(--error)}.banner-failed .banner-icon{background:var(--error)}
            .banner-warning{background:var(--warning-bg);border-color:var(--warning-border)}
            .banner-warning .banner-title{color:var(--warning)}.banner-warning .banner-icon{background:var(--warning)}
            .banner-passed{background:var(--ok-bg);border-color:var(--ok-border)}
            .banner-passed .banner-title{color:var(--ok)}.banner-passed .banner-icon{background:var(--ok)}
            .details{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:var(--s2) var(--s3);margin:0}
            .details div{min-width:0}.details .wide{grid-column:1/-1}
            dt{font-size:13px;color:var(--muted)}
            dd{margin:0;overflow-wrap:anywhere}
            code{font:13px var(--mono);background:var(--hover);padding:2px 6px;border-radius:4px}
            .stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--s2)}
            .stat{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:var(--s2) var(--s3)}
            .stat-label{display:block;font-size:13px;color:var(--muted)}
            .stat-value{display:block;font-size:32px;line-height:1.25;font-weight:700;font-variant-numeric:tabular-nums}
            .stat-error .stat-value{color:var(--error)}.stat-warning .stat-value{color:var(--warning)}
            .bars{list-style:none;margin:0;padding:0;display:grid;gap:var(--s1)}
            .bars li{display:grid;grid-template-columns:minmax(120px,200px) 1fr 4ch;gap:var(--s2);align-items:center;font-size:14px}
            .bar-track{height:8px;background:var(--hover);border-radius:4px;overflow:hidden}
            .bar{display:block;height:100%;background:var(--accent);border-radius:4px}
            .bar-count{text-align:right;color:var(--muted);font-variant-numeric:tabular-nums}
            .table-card{padding:0;overflow:hidden}
            .table-head{display:flex;flex-wrap:wrap;justify-content:space-between;align-items:baseline;gap:var(--s1) var(--s2);
            padding:var(--s3) var(--s3) var(--s2)}
            .table-head h2{margin:0}
            .muted{color:var(--muted);font-size:13px}
            .table-wrap{overflow:auto;max-height:70vh;border-top:1px solid var(--border)}
            table{width:100%;min-width:680px;border-collapse:separate;border-spacing:0;font-size:14px}
            th,td{text-align:left;vertical-align:top;padding:var(--s1) var(--s2)}
            th{position:sticky;top:0;z-index:1;background:var(--surface);border-bottom:1px solid var(--border);
            color:var(--muted);font-size:13px;font-weight:600;white-space:nowrap;cursor:pointer;user-select:none}
            th:hover{color:var(--text)}
            th:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
            th[data-dir]{color:var(--text)}
            th[data-dir]::after{color:var(--accent);font-size:10px;margin-left:4px}
            th[data-dir=asc]::after{content:"\\25B2"}th[data-dir=desc]::after{content:"\\25BC"}
            th[data-type=num],td.num{text-align:right}
            td{border-bottom:1px solid var(--border)}
            td.num{font-variant-numeric:tabular-nums;white-space:nowrap}
            td.rule,.segment{white-space:nowrap}
            tbody tr:last-child td{border-bottom:0}
            tbody tr:nth-child(even){background:var(--stripe)}
            tbody tr:hover{background:var(--hover)}
            .pill{display:inline-block;padding:0 var(--s1);border:1px solid;border-radius:999px;font-size:12px;
            font-weight:600;line-height:20px;white-space:nowrap}
            .pill-error{color:var(--error);background:var(--error-bg);border-color:var(--error-border)}
            .pill-warning{color:var(--warning);background:var(--warning-bg);border-color:var(--warning-border)}
            .none{color:var(--muted)}
            .empty{margin:0;padding:var(--s3);color:var(--ok);font-weight:600}
            footer{border-top:1px solid var(--border);color:var(--muted);font-size:13px}
            footer .wrap{display:flex;flex-wrap:wrap;justify-content:space-between;gap:var(--s1);padding-top:var(--s2);
            padding-bottom:var(--s2)}
            @media (max-width:640px){
            .details{grid-template-columns:1fr}
            .banner{align-items:flex-start}.banner-title{font-size:20px}
            .stat{padding:var(--s2)}.stat-value{font-size:24px}
            .bars li{grid-template-columns:minmax(0,1fr) minmax(0,1fr) 4ch}}
            @media print{
            body{background:#fff}
            th[data-dir]::after{content:none}
            th{position:static;cursor:default}
            .table-wrap{max-height:none;overflow:visible}
            tbody tr:hover{background:inherit}
            .banner,.card,.stat{break-inside:avoid}}
            """;

    private static final String SCRIPT = """
            (function () {
              var table = document.querySelector('table.issues');
              if (!table) { return; }
              var headers = table.tHead.rows[0].cells;
              function value(cell) {
                return cell.hasAttribute('data-value') ? cell.getAttribute('data-value') : cell.textContent;
              }
              function sortBy(index) {
                var th = headers[index];
                var asc = th.getAttribute('data-dir') !== 'asc';
                Array.prototype.forEach.call(headers, function (h) {
                  h.removeAttribute('data-dir');
                  h.removeAttribute('aria-sort');
                });
                th.setAttribute('data-dir', asc ? 'asc' : 'desc');
                th.setAttribute('aria-sort', asc ? 'ascending' : 'descending');
                var numeric = th.getAttribute('data-type') === 'num';
                var body = table.tBodies[0];
                Array.prototype.slice.call(body.rows).sort(function (a, b) {
                  var x = value(a.cells[index]), y = value(b.cells[index]);
                  var c = numeric ? Number(x) - Number(y) : x.localeCompare(y);
                  return asc ? c : -c;
                }).forEach(function (row) { body.appendChild(row); });
              }
              Array.prototype.forEach.call(headers, function (th, index) {
                th.addEventListener('click', function () { sortBy(index); });
                th.addEventListener('keydown', function (event) {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    sortBy(index);
                  }
                });
              });
            })();
            """;

    @Override
    public void write(Report report, Appendable out) throws IOException {
        RunMetadata metadata = report.metadata();
        out.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>").append(ProductName.DISPLAY).append(" report: ").append(escape(displayPath(metadata.inputFile())))
                .append("</title>\n<style>\n").append(STYLE).append("</style>\n</head>\n<body>\n");
        writeTopBar(metadata, out);
        out.append("<main class=\"wrap\">\n");
        writeBanner(report, out);
        writeRunDetails(metadata, out);
        writeOverview(report, out);
        if (!report.issues().isEmpty()) {
            writeRuleBreakdown(report, out);
        }
        writeIssues(report, out);
        out.append("</main>\n");
        writeFooter(metadata, out);
        out.append("<script>\n").append(SCRIPT).append("</script>\n</body>\n</html>\n");
    }

    private static void writeTopBar(RunMetadata metadata, Appendable out) throws IOException {
        out.append("<header class=\"topbar\"><div class=\"wrap\">\n")
                .append("<div class=\"brand\"><span class=\"brand-logo\">").append(BRAND_MARK).append("</span>")
                .append("<div class=\"brand-text\"><span class=\"wordmark\">").append(ProductName.DISPLAY).append("</span>")
                .append("<span class=\"brand-subtitle\">Lab data validation report</span></div></div>\n")
                .append("<span class=\"version\">v").append(escape(metadata.toolVersion())).append("</span>\n")
                .append("</div></header>\n");
    }

    private static void writeBanner(Report report, Appendable out) throws IOException {
        Map<Severity, Long> counts = report.countsBySeverity();
        long errors = counts.get(Severity.ERROR);
        long warnings = counts.get(Severity.WARNING);
        String failOn = report.metadata().failOn().name().toLowerCase(Locale.ROOT);
        String kind;
        String icon;
        String title;
        String detail;
        if (!report.passed()) {
            kind = "failed";
            icon = "\u2715";
            title = "Failed \u2014 " + plural(errors, "error") + ", " + plural(warnings, "warning");
            detail = "Issues were found at or above the fail-on level (" + failOn + ").";
        } else if (warnings > 0) {
            kind = "warning";
            icon = "!";
            title = "Passed with warnings";
            detail = plural(warnings, "warning") + ", none at or above the fail-on level (" + failOn + ").";
        } else {
            kind = "passed";
            icon = "\u2713";
            title = "Passed";
            detail = "No issues found.";
        }
        out.append("<section class=\"banner banner-").append(kind).append("\" role=\"status\">")
                .append("<span class=\"banner-icon\" aria-hidden=\"true\">").append(icon).append("</span>")
                .append("<div><p class=\"banner-title\">").append(title).append("</p>")
                .append("<p class=\"banner-detail\">").append(detail).append("</p></div></section>\n");
    }

    private static void writeRunDetails(RunMetadata metadata, Appendable out) throws IOException {
        RowCounts rows = metadata.rows();
        String hash = metadata.inputSha256();
        String shortHash = hash.length() > SHORT_HASH_LENGTH ? hash.substring(0, SHORT_HASH_LENGTH) + "\u2026" : hash;
        out.append("<section class=\"card\"><h2>Run details</h2>\n<dl class=\"details\">\n");
        detail(out, "", "Input file", escape(displayPath(metadata.inputFile())));
        detail(out, "", "Config file", escape(displayPath(metadata.configFile())));
        detail(out, "", "Run at", TIMESTAMP.format(metadata.runAt()));
        detail(out, "", "SHA-256", "<code title=\"" + escape(hash) + "\">" + escape(shortHash) + "</code>");
        detail(out, "wide", "Rows", String.join(" ",
                segment(rows.read() + " read" + SEPARATOR),
                segment(rows.parsed() + " parsed" + SEPARATOR),
                segment(rows.parsedWithIssues() + " with issues" + SEPARATOR),
                segment(rows.parsedClean() + " clean" + SEPARATOR),
                segment(rows.unparsed() + " unparsed")));
        out.append("</dl></section>\n");
    }

    // Keeps each count together with its trailing separator, so a wrapped line never starts with a dot.
    private static String segment(String text) {
        return "<span class=\"segment\">" + text + "</span>";
    }

    private static void detail(Appendable out, String cssClass, String term, String html) throws IOException {
        out.append(cssClass.isEmpty() ? "<div>" : "<div class=\"" + cssClass + "\">")
                .append("<dt>").append(term).append("</dt><dd>").append(html).append("</dd></div>\n");
    }

    private static void writeOverview(Report report, Appendable out) throws IOException {
        Map<Severity, Long> counts = report.countsBySeverity();
        out.append("<section aria-labelledby=\"overview\"><h2 id=\"overview\">Overview</h2>\n<div class=\"stats\">\n");
        stat(out, "", "Total issues", report.issues().size());
        long errors = counts.get(Severity.ERROR);
        long warnings = counts.get(Severity.WARNING);
        // Zero counts stay neutral: a red "0" reads as an alarm.
        stat(out, errors > 0 ? "stat-error" : "", "Errors", errors);
        stat(out, warnings > 0 ? "stat-warning" : "", "Warnings", warnings);
        out.append("</div></section>\n");
    }

    private static void stat(Appendable out, String cssClass, String label, long value) throws IOException {
        out.append("<div class=\"stat ").append(cssClass).append("\"><span class=\"stat-label\">").append(label)
                .append("</span><span class=\"stat-value\">").append(String.valueOf(value)).append("</span></div>\n");
    }

    private static void writeRuleBreakdown(Report report, Appendable out) throws IOException {
        Map<String, Long> byRule = report.countsByRule();
        long max = byRule.values().stream().mapToLong(Long::longValue).max().orElse(1);
        out.append("<section class=\"card\"><h2>Issues by rule</h2>\n<ul class=\"bars\">\n");
        for (Map.Entry<String, Long> entry : byRule.entrySet()) {
            long percent = Math.max(1, Math.round(100.0 * entry.getValue() / max));
            out.append("<li><span>").append(escape(RuleNames.displayName(entry.getKey()))).append("</span>")
                    .append("<span class=\"bar-track\"><span class=\"bar\" style=\"width:").append(String.valueOf(percent))
                    .append("%\"></span></span><span class=\"bar-count\">").append(String.valueOf(entry.getValue()))
                    .append("</span></li>\n");
        }
        out.append("</ul></section>\n");
    }

    private static void writeIssues(Report report, Appendable out) throws IOException {
        out.append("<section class=\"card table-card\">\n<div class=\"table-head\"><h2>Issues</h2>");
        if (report.issues().isEmpty()) {
            out.append("</div>\n<p class=\"empty\">No issues found.</p>\n</section>\n");
            return;
        }
        out.append("<span class=\"muted\">Select a column heading to sort</span></div>\n")
                .append("<div class=\"table-wrap\">\n<table class=\"issues\">\n<thead><tr>")
                .append("<th scope=\"col\" tabindex=\"0\" data-type=\"num\" data-dir=\"asc\" aria-sort=\"ascending\">Line</th>")
                .append("<th scope=\"col\" tabindex=\"0\">Sample</th>")
                .append("<th scope=\"col\" tabindex=\"0\">Severity</th>")
                .append("<th scope=\"col\" tabindex=\"0\">Rule</th>")
                .append("<th scope=\"col\" tabindex=\"0\">Message</th>")
                .append("</tr></thead>\n<tbody>\n");
        for (ValidationIssue issue : report.issues()) {
            writeIssueRow(issue, out);
        }
        out.append("</tbody>\n</table>\n</div>\n</section>\n");
    }

    private static void writeIssueRow(ValidationIssue issue, Appendable out) throws IOException {
        String line = issue.line() > 0 ? String.valueOf(issue.line()) : "File";
        String sample = issue.sampleId().isEmpty()
                ? "<span class=\"none\" aria-label=\"none\">\u2014</span>"
                : escape(issue.sampleId());
        String severity = issue.severity().name().toLowerCase(Locale.ROOT);
        out.append("<tr><td class=\"num\" data-value=\"").append(String.valueOf(issue.line())).append("\">")
                .append(line).append("</td>")
                .append("<td data-value=\"").append(escape(issue.sampleId())).append("\">").append(sample).append("</td>")
                .append("<td><span class=\"pill pill-").append(severity).append("\">")
                .append(sentenceCase(severity)).append("</span></td>")
                .append("<td class=\"rule\">").append(escape(issue.ruleDisplayName())).append("</td>")
                .append("<td>").append(escape(issue.message())).append("</td></tr>\n");
    }

    private static void writeFooter(RunMetadata metadata, Appendable out) throws IOException {
        out.append("<footer><div class=\"wrap\"><span>Generated by ").append(ProductName.DISPLAY).append(" v")
                .append(escape(metadata.toolVersion())).append("</span><span>")
                .append(TIMESTAMP.format(metadata.runAt())).append("</span></div></footer>\n");
    }

    private static String plural(long count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    private static String sentenceCase(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    // Reports are often produced on Windows and read elsewhere; forward slashes read the same everywhere.
    private static String displayPath(String path) {
        return path.replace('\\', '/');
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
