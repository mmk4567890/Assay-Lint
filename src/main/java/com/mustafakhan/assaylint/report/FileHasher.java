package com.mustafakhan.assaylint.report;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Computes the SHA-256 of a file so a report can be tied to the exact bytes that were validated. */
public final class FileHasher {

    private static final int BUFFER_SIZE = 8192;

    private FileHasher() {
    }

    /**
     * Hashes a file's contents.
     *
     * @param file the file to hash
     * @return the SHA-256 digest in lowercase hex
     * @throws IOException if the file cannot be read
     */
    public static String sha256(Path file) throws IOException {
        MessageDigest digest = newSha256();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256.
            throw new IllegalStateException(e);
        }
    }
}
