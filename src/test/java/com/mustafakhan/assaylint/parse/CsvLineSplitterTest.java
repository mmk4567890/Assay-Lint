package com.mustafakhan.assaylint.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvLineSplitterTest {

    @Test
    void splitsPlainFields() throws Exception {
        assertEquals(List.of("a", "b", "c"), CsvLineSplitter.split("a,b,c"));
    }

    @Test
    void keepsEmptyFieldsIncludingTrailing() throws Exception {
        assertEquals(List.of("a", "", "c", ""), CsvLineSplitter.split("a,,c,"));
    }

    @Test
    void allowsCommasInsideQuotes() throws Exception {
        assertEquals(List.of("S1", "Smith, J.", "x"), CsvLineSplitter.split("S1,\"Smith, J.\",x"));
    }

    @Test
    void unescapesDoubledQuotes() throws Exception {
        assertEquals(List.of("say \"hi\"", "b"), CsvLineSplitter.split("\"say \"\"hi\"\"\",b"));
    }

    @Test
    void quotedEmptyFieldIsEmpty() throws Exception {
        assertEquals(List.of("", "b"), CsvLineSplitter.split("\"\",b"));
    }

    @Test
    void rejectsUnterminatedQuote() {
        MalformedLineException e = assertThrows(MalformedLineException.class,
                () -> CsvLineSplitter.split("S1,\"open,value"));
        assertTrue(e.getMessage().contains("unterminated"));
    }

    @Test
    void rejectsTextAfterClosingQuote() {
        MalformedLineException e = assertThrows(MalformedLineException.class,
                () -> CsvLineSplitter.split("\"ab\"c,d"));
        assertTrue(e.getMessage().contains("after closing quote"));
    }
}
