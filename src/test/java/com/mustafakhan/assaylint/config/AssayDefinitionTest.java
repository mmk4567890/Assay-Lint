package com.mustafakhan.assaylint.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AssayDefinitionTest {

    private final AssayDefinition glucose = new AssayDefinition("mg/dL", 0, 600);

    @Test
    void boundsAreInclusive() {
        assertTrue(glucose.contains(0));
        assertTrue(glucose.contains(600));
        assertFalse(glucose.contains(-0.1));
        assertFalse(glucose.contains(600.1));
    }

    @Test
    void rejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> new AssayDefinition("mg/dL", 10, 1));
    }
}
