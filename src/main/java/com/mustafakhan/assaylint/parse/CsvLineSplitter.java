package com.mustafakhan.assaylint.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a single CSV line into fields following RFC 4180 quoting: fields may be
 * wrapped in double quotes, which allows commas inside them, and a doubled quote
 * inside a quoted field stands for one literal quote.
 *
 * <p>Unlike strict RFC 4180, spaces and tabs are allowed before an opening quote
 * and after a closing quote, as in {@code jdoe, "Nguyen, T."}. Spreadsheet tools and
 * hand edits produce this often, and the padding carries no meaning.
 */
final class CsvLineSplitter {

    private static final char QUOTE = '"';
    private static final char COMMA = ',';

    private CsvLineSplitter() {
    }

    /**
     * Splits one line into its fields. Unquoted fields are returned untrimmed;
     * padding outside a quoted field is dropped.
     *
     * @param line a single line without its line terminator
     * @return the fields, in order
     * @throws MalformedLineException if a quoted field is unterminated or followed by stray text
     */
    static List<String> split(String line) throws MalformedLineException {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        int i = 0;
        while (true) {
            int firstNonBlank = skipBlanks(line, i);
            if (firstNonBlank < line.length() && line.charAt(firstNonBlank) == QUOTE) {
                i = readQuoted(line, firstNonBlank + 1, field);
            } else {
                i = readUnquoted(line, i, field);
            }
            fields.add(field.toString());
            field.setLength(0);
            if (i >= line.length()) {
                return fields;
            }
            i++; // skip the comma that ended the field
        }
    }

    private static int readUnquoted(String line, int start, StringBuilder field) {
        int i = start;
        while (i < line.length() && line.charAt(i) != COMMA) {
            field.append(line.charAt(i));
            i++;
        }
        return i;
    }

    private static int readQuoted(String line, int start, StringBuilder field) throws MalformedLineException {
        int i = start;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c != QUOTE) {
                field.append(c);
                i++;
            } else if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                field.append(QUOTE);
                i += 2;
            } else {
                return expectFieldEnd(line, i + 1);
            }
        }
        throw new MalformedLineException("unterminated quoted field");
    }

    private static int expectFieldEnd(String line, int afterClosingQuote) throws MalformedLineException {
        int i = skipBlanks(line, afterClosingQuote);
        if (i < line.length() && line.charAt(i) != COMMA) {
            throw new MalformedLineException("unexpected text after closing quote at position " + (i + 1));
        }
        return i;
    }

    private static int skipBlanks(String line, int start) {
        int i = start;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        return i;
    }
}
