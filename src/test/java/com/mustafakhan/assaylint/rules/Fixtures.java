package com.mustafakhan.assaylint.rules;

import com.mustafakhan.assaylint.config.AssayDefinition;
import com.mustafakhan.assaylint.config.RuleConfig;
import com.mustafakhan.assaylint.model.SampleRecord;
import java.util.Map;

/** Small builders shared by the rule tests. */
final class Fixtures {

    static final RuleConfig CONFIG = new RuleConfig(Map.of(
            "glucose", new AssayDefinition("mg/dL", 0, 600),
            "ph", new AssayDefinition("pH", 0, 14)));

    private Fixtures() {
    }

    static SampleRecord record(int line, String sampleId, String assay, String value) {
        return new SampleRecord(line, sampleId, assay, value, "mg/dL", "2026-01-05T10:00:00Z", "jdoe");
    }

    static SampleRecord withUnit(int line, String assay, String unit) {
        return new SampleRecord(line, "S" + line, assay, "7", unit, "2026-01-05T10:00:00Z", "jdoe");
    }

    static SampleRecord withTimestamp(int line, String sampleId, String timestamp) {
        return new SampleRecord(line, sampleId, "glucose", "95", "mg/dL", timestamp, "jdoe");
    }
}
