package com.mustafakhan.assaylint.config;

import java.util.Map;
import java.util.Optional;

/**
 * The assay definitions that rules validate against, keyed by assay name.
 * Assay names are matched exactly, including case.
 *
 * @param assays assay name to definition
 */
public record RuleConfig(Map<String, AssayDefinition> assays) {

    /** Makes a defensive, unmodifiable copy of the map. */
    public RuleConfig {
        assays = Map.copyOf(assays);
    }

    /**
     * Looks up the definition for an assay.
     *
     * @param assay assay name as it appears in the data
     * @return the definition, or empty if the assay is not configured
     */
    public Optional<AssayDefinition> find(String assay) {
        return Optional.ofNullable(assays.get(assay));
    }
}
