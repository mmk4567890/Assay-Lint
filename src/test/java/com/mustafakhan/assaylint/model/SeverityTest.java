package com.mustafakhan.assaylint.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeverityTest {

    @Test
    void errorMeetsBothThresholds() {
        assertTrue(Severity.ERROR.isAtLeast(Severity.ERROR));
        assertTrue(Severity.ERROR.isAtLeast(Severity.WARNING));
    }

    @Test
    void warningDoesNotMeetErrorThreshold() {
        assertTrue(Severity.WARNING.isAtLeast(Severity.WARNING));
        assertFalse(Severity.WARNING.isAtLeast(Severity.ERROR));
    }
}
