package com.mustafakhan.assaylint.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/** Build-time facts about the tool, read from a resource that Maven fills in from the POM. */
final class BuildInfo {

    private static final String RESOURCE = "/assay-lint.properties";

    private BuildInfo() {
    }

    /**
     * Returns the version the tool was built as.
     *
     * @return the project version, or {@code "unknown"} if the build did not record one
     */
    static String version() {
        Properties properties = new Properties();
        try (InputStream in = BuildInfo.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties.getProperty("version", "unknown");
    }
}
