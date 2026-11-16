package com.mustafakhan.assaylint.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    private String errorFor(String json) {
        return assertThrows(ConfigException.class, () -> loader.parse(json)).getMessage();
    }

    @Test
    void parsesValidConfig() throws ConfigException {
        RuleConfig config = loader.parse("""
                {"assays": {
                  "glucose": {"unit": "mg/dL", "min": 0, "max": 600},
                  "ph": {"unit": "pH", "min": 6.5, "max": 8.0}
                }}""");

        assertEquals(new AssayDefinition("mg/dL", 0, 600), config.find("glucose").orElseThrow());
        assertEquals(6.5, config.find("ph").orElseThrow().min());
        assertTrue(config.find("sodium").isEmpty());
    }

    @Test
    void reportsMalformedJsonWithLocation() {
        String message = errorFor("{\"assays\": {\"glucose\": }");

        assertTrue(message.startsWith("not valid JSON"), message);
        assertTrue(message.contains("line 1"), message);
    }

    @Test
    void rejectsEmptyDocument() {
        assertEquals("file is empty", errorFor("   "));
    }

    @Test
    void requiresAssaysObject() {
        assertEquals("expected a top-level \"assays\" object", errorFor("{\"assay\": {}}"));
        assertEquals("expected a top-level \"assays\" object", errorFor("[1, 2]"));
    }

    @Test
    void requiresAtLeastOneAssay() {
        assertEquals("\"assays\" must define at least one assay", errorFor("{\"assays\": {}}"));
    }

    @Test
    void reportsMissingFields() {
        assertEquals("assay 'glucose': \"unit\" must be a non-empty string, \"max\" must be a number",
                errorFor("{\"assays\": {\"glucose\": {\"min\": 0}}}"));
    }

    @Test
    void rejectsNonNumericBoundsAndNonObjectAssay() {
        String message = errorFor("{\"assays\": {\"a\": {\"unit\": \"x\", \"min\": \"0\", \"max\": 1}, \"b\": 5}}");

        assertTrue(message.contains("assay 'a': \"min\" must be a number"), message);
        assertTrue(message.contains("assay 'b': must be an object"), message);
    }

    @Test
    void rejectsMinGreaterThanMax() {
        assertEquals("assay 'ph': min (14) is greater than max (0)",
                errorFor("{\"assays\": {\"ph\": {\"unit\": \"pH\", \"min\": 14, \"max\": 0}}}"));
    }

    @Test
    void loadsFromFile(@TempDir Path dir) throws IOException, ConfigException {
        Path file = dir.resolve("assays.json");
        Files.writeString(file, "{\"assays\": {\"glucose\": {\"unit\": \"mg/dL\", \"min\": 0, \"max\": 600}}}");

        assertTrue(loader.load(file).find("glucose").isPresent());
    }

    @Test
    void missingFileHasClearMessage(@TempDir Path dir) {
        Path file = dir.resolve("nope.json");

        ConfigException e = assertThrows(ConfigException.class, () -> loader.load(file));

        assertEquals("Config file not found: " + file, e.getMessage());
    }

    @Test
    void invalidFileNamesThePath(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("bad.json");
        Files.writeString(file, "{}");

        ConfigException e = assertThrows(ConfigException.class, () -> loader.load(file));

        assertTrue(e.getMessage().startsWith("Invalid config " + file + ": "), e.getMessage());
    }
}
