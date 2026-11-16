package com.mustafakhan.assaylint.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileHasherTest {

    @Test
    void hashesKnownContent(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("abc.txt");
        Files.writeString(file, "abc", StandardCharsets.US_ASCII);

        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", FileHasher.sha256(file));
    }
}
