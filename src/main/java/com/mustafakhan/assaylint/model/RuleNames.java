package com.mustafakhan.assaylint.model;

import java.util.Locale;

/**
 * Turns rule identifiers into the names people read in reports. Identifiers stay
 * in machine-readable output; display names are derived from them, so a new rule
 * gets a readable name without any extra registration.
 */
public final class RuleNames {

    private RuleNames() {
    }

    /**
     * Converts a camel-case rule identifier to a sentence-case name, keeping "ID" as
     * an acronym: {@code DuplicateSampleId} becomes {@code Duplicate sample ID}.
     *
     * @param ruleId the rule identifier, for example {@code UnknownAssay}
     * @return the display name, for example {@code Unknown assay}
     */
    public static String displayName(String ruleId) {
        String[] words = ruleId.split("(?<=[a-z0-9])(?=[A-Z])");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                name.append(' ');
            }
            name.append(displayWord(words[i], i == 0));
        }
        return name.toString();
    }

    private static String displayWord(String word, boolean first) {
        if (word.equals("Id")) {
            return "ID";
        }
        return first ? word : word.toLowerCase(Locale.ROOT);
    }
}
