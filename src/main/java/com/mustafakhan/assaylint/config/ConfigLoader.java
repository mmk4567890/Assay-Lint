package com.mustafakhan.assaylint.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads assay definitions from JSON of the form
 * <pre>{@code
 * { "assays": { "glucose": { "unit": "mg/dL", "min": 0, "max": 600 } } }
 * }</pre>
 *
 * <p>Every problem in the file is collected and reported together, so a user
 * fixing a config sees all mistakes at once rather than one per run.
 */
public final class ConfigLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads and validates a config file.
     *
     * @param path the JSON config file
     * @return the validated config
     * @throws ConfigException if the file is missing, unreadable or invalid
     */
    public RuleConfig load(Path path) throws ConfigException {
        String json;
        try {
            json = Files.readString(path, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ConfigException("Config file not found: " + path);
        } catch (IOException e) {
            throw new ConfigException("Cannot read config file " + path + ": " + e.getMessage());
        }
        try {
            return parse(json);
        } catch (ConfigException e) {
            throw new ConfigException("Invalid config " + path + ": " + e.getMessage());
        }
    }

    /**
     * Parses and validates config JSON.
     *
     * @param json the config document
     * @return the validated config
     * @throws ConfigException if the JSON is malformed or does not describe valid assays
     */
    public RuleConfig parse(String json) throws ConfigException {
        JsonNode root = readTree(json);
        JsonNode assays = root.path("assays");
        if (!assays.isObject()) {
            throw new ConfigException("expected a top-level \"assays\" object");
        }
        if (assays.isEmpty()) {
            throw new ConfigException("\"assays\" must define at least one assay");
        }
        Map<String, AssayDefinition> definitions = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = assays.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            List<String> assayProblems = validateAssay(entry.getValue());
            if (assayProblems.isEmpty()) {
                definitions.put(entry.getKey(), toDefinition(entry.getValue()));
            } else {
                problems.add("assay '" + entry.getKey() + "': " + String.join(", ", assayProblems));
            }
        }
        if (!problems.isEmpty()) {
            throw new ConfigException(String.join("; ", problems));
        }
        return new RuleConfig(definitions);
    }

    private JsonNode readTree(String json) throws ConfigException {
        try {
            JsonNode root = mapper.readTree(json);
            if (root == null || root.isMissingNode()) {
                throw new ConfigException("file is empty");
            }
            return root;
        } catch (JsonProcessingException e) {
            throw new ConfigException("not valid JSON (" + e.getOriginalMessage() + " at line "
                    + e.getLocation().getLineNr() + ", column " + e.getLocation().getColumnNr() + ")");
        }
    }

    private static List<String> validateAssay(JsonNode assay) {
        List<String> problems = new ArrayList<>();
        if (!assay.isObject()) {
            problems.add("must be an object with unit, min and max");
            return problems;
        }
        JsonNode unit = assay.get("unit");
        if (unit == null || !unit.isTextual() || unit.asText().isBlank()) {
            problems.add("\"unit\" must be a non-empty string");
        }
        boolean boundsValid = checkBound(assay, "min", problems) & checkBound(assay, "max", problems);
        if (boundsValid && assay.get("min").asDouble() > assay.get("max").asDouble()) {
            problems.add("min (" + assay.get("min").asText() + ") is greater than max ("
                    + assay.get("max").asText() + ")");
        }
        return problems;
    }

    private static boolean checkBound(JsonNode assay, String name, List<String> problems) {
        JsonNode bound = assay.get(name);
        if (bound == null || !bound.isNumber() || !Double.isFinite(bound.asDouble())) {
            problems.add("\"" + name + "\" must be a number");
            return false;
        }
        return true;
    }

    private static AssayDefinition toDefinition(JsonNode assay) {
        return new AssayDefinition(assay.get("unit").asText(), assay.get("min").asDouble(), assay.get("max").asDouble());
    }
}
