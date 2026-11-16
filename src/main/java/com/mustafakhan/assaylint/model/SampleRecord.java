package com.mustafakhan.assaylint.model;

import java.util.Objects;

/**
 * One data row from a sample file. Field values are kept as the raw, trimmed
 * strings from the file; interpreting them (as numbers, timestamps, units) is
 * the job of the validation rules.
 *
 * @param line      1-based line number in the source file
 * @param sampleId  sample identifier
 * @param assay     assay name, matched against the config
 * @param value     measured value
 * @param unit      unit of the measured value
 * @param timestamp time of measurement, expected in ISO-8601
 * @param analyst   person who recorded the measurement
 */
public record SampleRecord(
        int line,
        String sampleId,
        String assay,
        String value,
        String unit,
        String timestamp,
        String analyst) {

    /** Rejects null fields so rules never have to null-check. */
    public SampleRecord {
        Objects.requireNonNull(sampleId, "sampleId");
        Objects.requireNonNull(assay, "assay");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(analyst, "analyst");
    }
}
