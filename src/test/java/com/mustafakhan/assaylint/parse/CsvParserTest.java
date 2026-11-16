package com.mustafakhan.assaylint.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mustafakhan.assaylint.model.ParseResult;
import com.mustafakhan.assaylint.model.SampleRecord;
import com.mustafakhan.assaylint.model.Severity;
import com.mustafakhan.assaylint.model.ValidationIssue;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvParserTest {

    private static final String HEADER = "sample_id,assay,value,unit,timestamp,analyst\n";
    private static final String ROW = "S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe\n";

    private final CsvParser parser = new CsvParser();

    private ParseResult parse(String csv) throws Exception {
        return parser.parse(new StringReader(csv));
    }

    @Test
    void parsesValidFile() throws Exception {
        ParseResult result = parse(HEADER + ROW);

        assertTrue(result.issues().isEmpty());
        assertEquals(1, result.dataRows());
        assertEquals(new SampleRecord(2, "S1", "glucose", "95", "mg/dL", "2026-01-05T10:00:00Z", "jdoe"),
                result.records().get(0));
    }

    @Test
    void skipsBlankLinesButKeepsLineNumbers() throws Exception {
        ParseResult result = parse("\n" + HEADER + "\n   \n" + ROW);

        assertTrue(result.issues().isEmpty());
        assertEquals(5, result.records().get(0).line());
    }

    @Test
    void trimsFieldValues() throws Exception {
        ParseResult result = parse(HEADER + " S1 , glucose ,95,mg/dL,2026-01-05T10:00:00Z, jdoe\n");

        SampleRecord record = result.records().get(0);
        assertEquals("S1", record.sampleId());
        assertEquals("jdoe", record.analyst());
    }

    @Test
    void handlesQuotedFieldWithComma() throws Exception {
        ParseResult result = parse(HEADER + "S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,\"Doe, J.\"\n");

        assertEquals("Doe, J.", result.records().get(0).analyst());
    }

    @Test
    void acceptsSpacesAroundQuotedField() throws Exception {
        ParseResult result = parse(HEADER + "S1, glucose, 95, mg/dL, 2026-01-05T10:00:00Z, \"Nguyen, T.\" \n");

        assertTrue(result.issues().isEmpty());
        assertEquals("Nguyen, T.", result.records().get(0).analyst());
    }

    @Test
    void mapsReorderedHeaderByName() throws Exception {
        ParseResult result = parse("analyst,Value,assay,unit,timestamp,SAMPLE_ID\n"
                + "jdoe,95,glucose,mg/dL,2026-01-05T10:00:00Z,S1\n");

        assertTrue(result.issues().isEmpty());
        SampleRecord record = result.records().get(0);
        assertEquals("S1", record.sampleId());
        assertEquals("95", record.value());
        assertEquals("jdoe", record.analyst());
    }

    @Test
    void missingHeaderAssumesDefaultOrderAndWarns() throws Exception {
        ParseResult result = parse(ROW);

        assertEquals(1, result.records().size());
        assertEquals(1, result.records().get(0).line());
        ValidationIssue issue = result.issues().get(0);
        assertEquals(Severity.WARNING, issue.severity());
        assertTrue(issue.message().contains("Header row missing"));
    }

    @Test
    void headerMissingColumnIsRejected() {
        CsvFormatException e = assertThrows(CsvFormatException.class,
                () -> parse("\nsample_id,assay,value,unit,timestamp\n" + ROW));

        assertEquals("Invalid header on line 2: missing column 'analyst'", e.getMessage());
    }

    @Test
    void headerWithDuplicateColumnIsRejected() {
        CsvFormatException e = assertThrows(CsvFormatException.class,
                () -> parse("sample_id,sample_id,assay,value,unit,timestamp,analyst\n"));

        assertEquals("Invalid header on line 1: duplicate column 'sample_id'", e.getMessage());
    }

    @Test
    void unknownHeaderColumnsAreSkippedWithAWarning() throws Exception {
        ParseResult result = parse("sample_id,comment,assay,value,unit,timestamp,analyst,\n"
                + "S1,rerun,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe,\n");

        assertEquals(new SampleRecord(2, "S1", "glucose", "95", "mg/dL", "2026-01-05T10:00:00Z", "jdoe"),
                result.records().get(0));
        assertEquals(2, result.issues().size());
        ValidationIssue comment = result.issues().get(0);
        assertEquals(1, comment.line());
        assertEquals(Severity.WARNING, comment.severity());
        assertEquals("Ignoring unknown column 'comment'", comment.message());
        assertEquals("Ignoring unnamed column 8", result.issues().get(1).message());
    }

    @Test
    void rowsMustMatchHeaderWidthIncludingExtraColumns() throws Exception {
        ParseResult result = parse("sample_id,assay,value,unit,timestamp,analyst,comment\n" + ROW);

        assertTrue(result.records().isEmpty());
        assertEquals("Expected 7 columns but found 6", result.issues().get(1).message());
    }

    @Test
    void reportsTooFewColumnsWithLineNumber() throws Exception {
        ParseResult result = parse(HEADER + "S2,glucose,95\n" + ROW);

        assertEquals(1, result.records().size());
        assertEquals(2, result.dataRows());
        ValidationIssue issue = result.issues().get(0);
        assertEquals(2, issue.line());
        assertEquals("S2", issue.sampleId());
        assertEquals("Expected 6 columns but found 3", issue.message());
    }

    @Test
    void reportsTooManyColumns() throws Exception {
        ParseResult result = parse(HEADER + "S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe,extra\n");

        assertTrue(result.records().isEmpty());
        assertEquals("Expected 6 columns but found 7", result.issues().get(0).message());
    }

    @Test
    void reportsUnterminatedQuoteAndContinues() throws Exception {
        ParseResult result = parse(HEADER + "S1,\"glucose,95,mg/dL,2026-01-05T10:00:00Z,jdoe\n" + ROW);

        assertEquals(1, result.records().size());
        assertEquals(2, result.dataRows());
        ValidationIssue issue = result.issues().get(0);
        assertEquals(2, issue.line());
        assertTrue(issue.message().startsWith("Malformed row"));
    }

    @Test
    void malformedFirstLineFallsBackToDefaultColumns() throws Exception {
        ParseResult result = parse("\"broken\n" + ROW);

        assertEquals(1, result.records().size());
        assertEquals(2, result.issues().size());
    }

    @Test
    void emptyFileIsAnError() throws Exception {
        ParseResult result = parse("\n\n");

        ValidationIssue issue = result.issues().get(0);
        assertEquals(Severity.ERROR, issue.severity());
        assertEquals("File is empty", issue.message());
    }

    @Test
    void headerOnlyFileWarnsAboutNoData() throws Exception {
        ParseResult result = parse(HEADER);

        assertEquals(Severity.WARNING, result.issues().get(0).severity());
        assertEquals("File contains no data rows", result.issues().get(0).message());
    }

    @Test
    void rejectsSemicolonSeparatedFile() {
        CsvFormatException e = assertThrows(CsvFormatException.class,
                () -> parse(HEADER.replace(',', ';') + ROW.replace(',', ';')));

        assertEquals("File does not appear to be comma-separated (found ';'). Save it as CSV with commas.",
                e.getMessage());
    }

    @Test
    void rejectsTabSeparatedFileEvenAfterBlankLines() {
        CsvFormatException e = assertThrows(CsvFormatException.class,
                () -> parse("\n\n" + HEADER.replace(',', '\t') + ROW.replace(',', '\t')));

        assertEquals("File does not appear to be comma-separated (found tabs). Save it as CSV with commas.",
                e.getMessage());
    }

    @Test
    void semicolonsInsideACommaSeparatedFileAreFine() throws Exception {
        ParseResult result = parse(HEADER + "S1,glucose,95,mg/dL,2026-01-05T10:00:00Z,\"Doe; J.\"\n");

        assertTrue(result.issues().isEmpty());
        assertEquals("Doe; J.", result.records().get(0).analyst());
    }

    @Test
    void singleColumnLineWithoutSeparatorsIsNotTreatedAsWrongDelimiter() throws Exception {
        ParseResult result = parse("S1\n");

        assertEquals("Expected 6 columns but found 1", result.issues().get(1).message());
    }

    @Test
    void handlesCrlfAndByteOrderMark() throws Exception {
        ParseResult result = parse("\uFEFF" + HEADER.replace("\n", "\r\n") + ROW.replace("\n", "\r\n"));

        assertTrue(result.issues().isEmpty());
        assertEquals("jdoe", result.records().get(0).analyst());
    }

    @Test
    void parsesFileFromDisk(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("samples.csv");
        Files.writeString(file, HEADER + ROW, StandardCharsets.UTF_8);

        assertEquals(1, parser.parse(file).records().size());
    }
}
