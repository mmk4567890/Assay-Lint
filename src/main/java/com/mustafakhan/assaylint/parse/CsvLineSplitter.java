package com.mustafakhan.assaylint.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a single CSV line into fields following RFC 4180 quoting: fields may be
 * wrapped in double quotes, which allows commas inside them, and a doubled quote
 * inside a quoted field stands for one literal quote.
 */
final class CsvLineSplitter {

    private static final char QUOTE = '"';
    private static final char COMMA = ',';

    private CsvLineSplitter() {
    }

    /**
     * Splits one line into its raw, untrimmed fields.
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
            if (i < line.length() && line.charAt(i) == QUOTE) {
                i = readQuoted(line, i + 1, field);
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
        if (afterClosingQuote < line.length() && line.charAt(afterClosingQuote) != COMMA) {
            throw new MalformedLineException("unexpected text after closing quote at position " + (afterClosingQuote + 1));
        }
        return afterClosingQuote;
    }
}
