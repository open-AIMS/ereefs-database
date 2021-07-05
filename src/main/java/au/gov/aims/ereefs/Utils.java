/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Collection of useful methods.
 */
public class Utils {
    // Used with checksum
    private static final int DIGEST_BUFFER_SIZE = 4096; // 4KB

    /**
     * Returns a user friendly error message for a given exception.
     *
     * @param ex the exception (technically a {@code Throwable} to offer broader support).
     * @return a user friendly error message.
     */
    public static String getExceptionMessage(Throwable ex) {
        String defaultMsg = ex == null ? "No error message available" : ex.getClass().getName();
        return getExceptionMessage(ex, defaultMsg);
    }

    /**
     * Returns a user friendly error message for a given exception.
     *
     * @param ex the exception (technically a {@code Throwable} to offer broader support).
     * @param defaultMsg an error message to returns if no suitable error message is found in the exception.
     * @return a user friendly error message.
     */
    public static String getExceptionMessage(Throwable ex, String defaultMsg) {
        String msg = null;
        if (ex != null) {
            msg = ex.getMessage();

            // SAXParseException has handy values that do not shows on getMessage.
            if (ex instanceof SAXParseException) {
                SAXParseException saxEx = (SAXParseException)ex;
                if (msg == null || msg.isEmpty()) {
                    // That should not happen
                    msg = "Can not parse the XML document.";
                }
                msg += "\nline: " + saxEx.getLineNumber() + ", character: " + saxEx.getColumnNumber();
            }

            if (msg == null || msg.isEmpty()) {
                msg = getExceptionMessage(ex.getCause(), defaultMsg);
            }
        }
        if (msg == null || msg.isEmpty()) {
            msg = defaultMsg;
        }
        return msg;
    }

    /**
     * Create a directory recursively.
     * Returns {@code false} if the directory could not be created,
     * if it already exists and it's not a directory
     * or is not readable or writable.
     *
     * @param dir the directory to create.
     * @return {@code true} if the directory exists and is writable; {@code false} otherwise.
     */
    public static boolean prepareDirectory(File dir) {
        if (dir == null) {
            return false;
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir.exists()
                && dir.isDirectory()
                && dir.canRead()
                && dir.canWrite();
    }


    /**
     * Delete a directory recursively and all of its content.
     *
     * <p>NOTE: This method is a workaround for a bug in {@link FileUtils#deleteDirectory(File)}.
     *     That method throw an {@code IOException} when it encounter a broken symbolic link.
     *     Unfortunately, this happens every time it tries to delete a broken symbolic link,
     *     for example, when it tries to delete a symbolic link after deleting the file it points to.
     *     To fix the issue, this method calls {@link Utils#deleteSymbolicLinks(File)} to delete
     *     the symbolic links in the directory before calling {@link FileUtils#deleteDirectory(File)}.</p>
     *
     * @param directory the directory to delete (recursively).
     * @return {@code true} if the directory and its content was successfully deleted; {@code false} otherwise.
     * @throws IOException if something goes wrong.
     */
    public static boolean deleteDirectory(File directory) throws IOException {
        // Remove all symbolic links
        if (Files.isSymbolicLink(directory.toPath())) {
            return directory.delete();
        } else if (directory.isDirectory()) {
            Utils.deleteSymbolicLinks(directory);
            FileUtils.deleteDirectory(directory);
        }

        return !directory.exists();
    }

    /**
     * Deletes every symbolic links found in the directory (recursively).
     *
     * @param directory the directory to purge from its symbolic links.
     * @return {@code true} if all the symbolic links were deleted; {@code false} otherwise.
     */
    public static boolean deleteSymbolicLinks(File directory) {
        boolean clean = true;

        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (Files.isSymbolicLink(file.toPath())) {
                        clean = file.delete() && clean;
                    } else if (file.isDirectory()) {
                        clean = Utils.deleteSymbolicLinks(file) && clean;
                    }
                }
            }
        }

        return clean;
    }

    /**
     * Returns the filename found at the end of an {@code URI}.
     *
     * @param uri the {@code URI} to parse.
     * @return the filename found at the end of the {@code URI}.
     */
    public static String getFilename(URI uri) {
        if (uri == null) {
            return null;
        }

        return new File(uri.getPath()).getName();
    }

    /**
     * Make a filename safe to use on a disk, by replacing any
     * unsafe character with an underscore {@code _}.
     *
     * @param filename the filename to parse.
     * @return a {@code String} which is safe to use as a filename.
     */
    public static String safeFilename(String filename) {
        return filename.replaceAll("[^A-Za-z0-9.\\-_]+", "_");
    }


    /**
     * Calculate the {@code MD5} checksum of a file.
     *
     * @param file the file to compute its {@code MD5} checksum.
     * @return the file {@code MD5} checksum in hexadecimal format.
     * @throws NoSuchAlgorithmException if the {@code MD5} algorithm is not supported by Java.
     * @throws IOException if something goes wrong.
     */
    public static String md5sum(File file) throws NoSuchAlgorithmException, IOException {
        return checksum(file, "MD5");
    }

    /**
     * Calculate the checksum for a file.
     *
     * @param file the file to compute its checksum.
     * @param algorithm the checksum algorithm to use. Example: {@code MD5}.
     * @return the file checksum in hexadecimal format.
     * @throws NoSuchAlgorithmException if java do not support the given checksum algorithm.
     * @throws IOException if something goes wrong.
     */
    public static String checksum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
        return toHex(binaryChecksum(file, algorithm));
    }

    /**
     * Calculate the binary checksum for a file.
     *
     * @param file the file to compute its checksum.
     * @param algorithm the checksum algorithm to use. Example: {@code MD5}.
     * @return the file checksum in binary format.
     * @throws NoSuchAlgorithmException if java do not support the given checksum algorithm.
     * @throws IOException if something goes wrong.
     */
    public static byte[] binaryChecksum(File file, String algorithm) throws NoSuchAlgorithmException, IOException {
        try (InputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] buffer = new byte[DIGEST_BUFFER_SIZE];
            int byteReadCount;

            while ((byteReadCount = fileInputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, byteReadCount);
            }

            return digest.digest();
        }
    }

    /**
     * Convert an array of binary {@code bytes} into an hexadecimal {@code String}.
     *
     * @param bytes the array of bytes
     * @return an hexadecimal representation of the array of bytes.
     */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte abyte : bytes) {
            sb.append(String.format("%02x", abyte));
        }
        return sb.toString();
    }

    /**
     * Returns the path of the running jar file.
     *
     * <p>There is many ways to achieve this, but most do not work on all operating systems.</p>
     *
     * <p>Inspired from: <a href="https://stackoverflow.com/questions/4871051/getting-the-current-working-directory-in-java" target="_blank">https://stackoverflow.com/questions/4871051/getting-the-current-working-directory-in-java</a></p>
     *
     * @return the path of the running jar file.
     */
    public static File getJarDirectory() {
        // https://stackoverflow.com/questions/4871051/getting-the-current-working-directory-in-java
        return new File("").getAbsoluteFile();

        // Might not work on Windows.
        //return new File(System.getProperty("user.dir"));

        // https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
        // Messy, doesn't work with some classes and custom class loader and doesn't return the path I want.
        //return new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
}
