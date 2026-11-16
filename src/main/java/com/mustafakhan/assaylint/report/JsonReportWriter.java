package com.mustafakhan.assaylint.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.IOException;

/**
 * Machine-readable report for CI pipelines and downstream tooling. The JSON shape
 * is built explicitly rather than derived from the Java types, so refactoring the
 * model cannot silently change the output contract.
 */
public final class JsonReportWriter implements ReportWriter {

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void write(Report report, Appendable out) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.set("metadata", metadata(report));
        root.set("summary", summary(report));
        root.set("issues", issues(report));
        out.append(mapper.writeValueAsString(root)).append(System.lineSeparator());
    }

    private ObjectNode metadata(Report report) {
        RunMetadata metadata = report.metadata();
        ObjectNode node = mapper.createObjectNode();
        node.put("inputFile", metadata.inputFile());
        node.put("inputSha256", metadata.inputSha256());
        node.put("configFile", metadata.configFile());
        node.put("runAt", metadata.runAt().toString());
        ObjectNode rows = node.putObject("rows");
        rows.put("read", metadata.rowsRead());
        rows.put("parsed", metadata.rowsParsed());
        rows.put("withIssues", report.rowsWithIssues());
        return node;
    }

    private ObjectNode summary(Report report) {
        ObjectNode node = mapper.createObjectNode();
        node.put("total", report.issues().size());
        ObjectNode bySeverity = node.putObject("bySeverity");
        report.countsBySeverity().forEach((severity, count) -> bySeverity.put(severity.name(), count));
        ObjectNode byRule = node.putObject("byRule");
        report.countsByRule().forEach(byRule::put);
        return node;
    }

    private ArrayNode issues(Report report) {
        ArrayNode array = mapper.createArrayNode();
        for (ValidationIssue issue : report.issues()) {
            ObjectNode node = array.addObject();
            node.put("line", issue.line());
            node.put("sampleId", issue.sampleId());
            node.put("rule", issue.rule());
            node.put("severity", issue.severity().name());
            node.put("message", issue.message());
        }
        return array;
    }
}
