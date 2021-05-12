/*
 *  Copyright (C) 2019 Australian Institute of Marine Science
 *
 *  Contact: Gael Lafond <g.lafond@aims.gov.au>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.gov.aims.ereefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Collection of methods used to zip and unzip archive files.
 */
public class ZipUtils {

    /**
     * Returns true of the filename has a file extension recognised as a zipped file or an archive file.
     *
     * <p>See {@link ZipType} for list of supports file extensions.</p>
     *
     * @param filename the filename to check.
     * @return {@code true} if the filename is recognised as a zipped file or an archive file; {@code false} otherwise.
     */
    public static boolean isZipped(String filename) {
        return ZipUtils.getZipType(filename) != null;
    }

    /**
     * Returns the {@link ZipType} associated to the filename if it have a file extension
     * recognised as a zipped file or an archive file.
     *
     * <p>See {@link ZipType} for list of supports file extensions.</p>
     *
     * @param filename the filename to check.
     * @return the {@link ZipType} associated to the filename extension or null it the
     *     file extension is not recognised as a zipped file or an archive file.
     */
    public static ZipType getZipType(String filename) {
        for (ZipType zipType : ZipType.values()) {
            if (filename.endsWith("." + zipType.getExtension())) {
                return zipType;
            }
        }

        return null;
    }

    /**
     * Returns the expected filename after been unzipped.
     *
     * <p>NOTE: Only supports {@code gzip} file type.</p>
     *
     * @param filename the filename of the zip archive.
     * @return the expected filename after been unzipped.
     */
    public static String getUnzippedFilename(String filename) {
        switch (ZipUtils.getZipType(filename)) {
            case GZ:
                return filename.substring(0, filename.lastIndexOf('.'));

            default:
                return filename;
        }
    }

    /**
     * Unzip zipped file or extract an archive file.
     *
     * <p>NOTE: Only supports {@code gzip} file type.</p>
     *
     * @param zippedFile the zipped file or archive file.
     * @return path to the unzipped file.
     * @throws IOException if the file type is not supported or if something goes wrong.
     */
    public static File unzipFile(File zippedFile) throws IOException {
        ZipType zipType = ZipUtils.getZipType(zippedFile.getName());

        if (zipType == null) {
            return zippedFile;
        }

        switch (zipType) {
            case GZ:
                return ZipUtils.unzipFileGZ(zippedFile);

            default:
                throw new IOException(String.format("Unsupported ZIP format: %s", zipType.getExtension()));
        }
    }

    /**
     * Unzip a {@code gzip} file.
     *
     * <p>Inspired from:
     * <a href="https://mkyong.com/java/how-to-decompress-file-from-gzip-file/" target="_blank">https://mkyong.com/java/how-to-decompress-file-from-gzip-file/</a></p>
     *
     * @param zippedFile the file to unzip.
     * @return path to the unzipped file.
     * @throws IOException if something goes wrong.
     */
    private static File unzipFileGZ(File zippedFile) throws IOException {
        File outputDir = zippedFile.getParentFile();
        File outputFile = new File(outputDir, ZipUtils.getUnzippedFilename(zippedFile.getName()));

        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(zippedFile));
                FileOutputStream out = new FileOutputStream(outputFile);) {

            byte[] buffer = new byte[1024];

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            out.flush();
        }

        return outputFile;
    }

    /**
     * List of supports file zipped file or an archive file extensions:
     *
     * <ul>
     *     <li><em>gz</em>: <a href="https://en.wikipedia.org/wiki/Gzip" target="_blank">gzip file format</a>.</li>
     *     <li><em>tar</em>: <a href="https://en.wikipedia.org/wiki/Tar_(computing)" target="_blank">tar archive file format</a>.</li>
     *     <li><em>tgz</em>: <a href="https://en.wikipedia.org/wiki/Gzip" target="_blank">gzipped</a> <a href="https://en.wikipedia.org/wiki/Tar_(computing)" target="_blank">tar archive file format</a>.</li>
     *     <li><em>zip</em>: <a href="https://en.wikipedia.org/wiki/ZIP_(file_format)" target="_blank">ZIP archive file format</a>.</li>
     * </ul>
     */
    public static enum ZipType {
        GZ("gz"),
        TAR("tar"),
        TGZ("tgz"),
        ZIP("zip");

        private String extension;

        ZipType(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return this.extension;
        }
    }
}
