package com.mustafakhan.assaylint.parse;

import com.mustafakhan.assaylint.model.ParseResult;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parses lab sample CSV files into {@link SampleRecord}s.
 *
 * <p>The parser is deliberately forgiving about layout and strict about structure.
 * Blank lines are skipped, the header may list columns in any order, and a file
 * without a header is read in the default column order with a warning. Rows that
 * cannot be read (wrong column count, broken quoting) are reported as issues with
 * their line number rather than aborting the run. Extra header columns, common in
 * instrument exports, are skipped with a warning. A header that misses or repeats a
 * required column is the exception: no row can be mapped safely, so parsing stops
 * with an {@link InvalidHeaderException}. Field values are not interpreted here;
 * that is left to the validation rules.
 *
 * <p>Each physical line is one record, so quoted fields cannot span lines.
 */
public final class CsvParser {

    /** Rule name used for structural issues found while parsing. */
    public static final String RULE_NAME = "Parse";

    /** The required columns, in the order assumed when a file has no header. */
    public static final List<String> COLUMNS =
            List.of("sample_id", "assay", "value", "unit", "timestamp", "analyst");

    private static final char BYTE_ORDER_MARK = '﻿';
    private static final int FILE_LEVEL = 0;

    /**
     * Parses a file. Bytes that are not valid UTF-8 are replaced rather than
     * rejected, so an encoding problem shows up as a bad value, not a crash.
     *
     * @param file the CSV file to read
     * @return the parsed records and any structural issues
     * @throws IOException            if the file cannot be read
     * @throws InvalidHeaderException if the header row names the wrong columns
     */
    public ParseResult parse(Path file) throws IOException, InvalidHeaderException {
        try (Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    /**
     * Parses CSV content from a reader.
     *
     * @param reader source of CSV text; not closed by this method
     * @return the parsed records and any structural issues
     * @throws IOException            if reading fails
     * @throws InvalidHeaderException if the header row names the wrong columns
     */
    public ParseResult parse(Reader reader) throws IOException, InvalidHeaderException {
        return new Session().run(new BufferedReader(reader));
    }

    /** Mutable state for a single parse, kept out of the reusable parser. */
    private static final class Session {
        private final List<SampleRecord> records = new ArrayList<>();
        private final List<ValidationIssue> issues = new ArrayList<>();
        private Map<String, Integer> columnIndex;
        private int columnCount;
        private int dataRows;

        ParseResult run(BufferedReader reader) throws IOException, InvalidHeaderException {
            int lineNumber = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && !line.isEmpty() && line.charAt(0) == BYTE_ORDER_MARK) {
                    line = line.substring(1);
                }
                if (line.isBlank()) {
                    continue;
                }
                handleLine(lineNumber, line);
            }
            addFileLevelIssues();
            return new ParseResult(records, issues, dataRows);
        }

        private void handleLine(int lineNumber, String line) throws InvalidHeaderException {
            List<String> fields;
            try {
                fields = CsvLineSplitter.split(line);
            } catch (MalformedLineException e) {
                if (columnIndex == null) {
                    useDefaultColumns(lineNumber);
                }
                dataRows++;
                issues.add(error(lineNumber, "", "Malformed row: " + e.getMessage()));
                return;
            }
            if (columnIndex == null) {
                if (looksLikeHeader(fields)) {
                    readHeader(lineNumber, fields);
                    return;
                }
                useDefaultColumns(lineNumber);
            }
            readRow(lineNumber, fields);
        }

        private boolean looksLikeHeader(List<String> fields) {
            return fields.stream().map(Session::normalize).anyMatch(COLUMNS::contains);
        }

        private void readHeader(int lineNumber, List<String> fields) throws InvalidHeaderException {
            Map<String, Integer> index = new HashMap<>();
            Set<String> problems = new LinkedHashSet<>();
            for (int i = 0; i < fields.size(); i++) {
                String name = normalize(fields.get(i));
                if (!COLUMNS.contains(name)) {
                    issues.add(new ValidationIssue(lineNumber, "", RULE_NAME, Severity.WARNING,
                            ignoredColumnMessage(fields.get(i).trim(), i)));
                } else if (index.putIfAbsent(name, i) != null) {
                    problems.add("duplicate column '" + name + "'");
                }
            }
            for (String column : COLUMNS) {
                if (!index.containsKey(column)) {
                    problems.add("missing column '" + column + "'");
                }
            }
            if (!problems.isEmpty()) {
                throw new InvalidHeaderException("Invalid header on line " + lineNumber + ": "
                        + String.join(", ", problems));
            }
            columnIndex = index;
            columnCount = fields.size();
        }

        private static String ignoredColumnMessage(String name, int index) {
            return name.isEmpty()
                    ? "Ignoring unnamed column " + (index + 1)
                    : "Ignoring unknown column '" + name + "'";
        }

        private void useDefaultColumns(int lineNumber) {
            columnIndex = new HashMap<>();
            for (int i = 0; i < COLUMNS.size(); i++) {
                columnIndex.put(COLUMNS.get(i), i);
            }
            columnCount = COLUMNS.size();
            issues.add(new ValidationIssue(lineNumber, "", RULE_NAME, Severity.WARNING,
                    "Header row missing; assuming column order " + String.join(", ", COLUMNS)));
        }

        private void readRow(int lineNumber, List<String> fields) {
            dataRows++;
            if (fields.size() != columnCount) {
                issues.add(error(lineNumber, field(fields, "sample_id"), "Expected " + columnCount
                        + " columns but found " + fields.size()));
                return;
            }
            records.add(new SampleRecord(
                    lineNumber,
                    field(fields, "sample_id"),
                    field(fields, "assay"),
                    field(fields, "value"),
                    field(fields, "unit"),
                    field(fields, "timestamp"),
                    field(fields, "analyst")));
        }

        private String field(List<String> fields, String column) {
            int i = columnIndex.get(column);
            return i < fields.size() ? fields.get(i).trim() : "";
        }

        private void addFileLevelIssues() {
            if (columnIndex == null && issues.isEmpty()) {
                issues.add(error(FILE_LEVEL, "", "File is empty"));
            } else if (dataRows == 0 && columnIndex != null) {
                issues.add(new ValidationIssue(FILE_LEVEL, "", RULE_NAME, Severity.WARNING,
                        "File contains no data rows"));
            }
        }

        private static ValidationIssue error(int line, String sampleId, String message) {
            return new ValidationIssue(line, sampleId, RULE_NAME, Severity.ERROR, message);
        }

        private static String normalize(String headerField) {
            return headerField.trim().toLowerCase(Locale.ROOT);
        }
    }
}
