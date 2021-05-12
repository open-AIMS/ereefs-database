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
package au.gov.aims.ereefs.bean.download;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Bean representing where the files are downloaded.
 * It's part of the {@link DownloadBean} which is used with the {@code ereefs-download-manager} project.
 * Downloaded files can be saved to disk (URI starting with {@code "file://"})
 * or to S3 (URI starting with {@code "s3://"}).
 */
public class OutputBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(OutputBean.class);

    // Temporary download location.
    private File downloadDir;

    // Where the file should be copied after upload, if it's not corrupted.
    private URI destination;

    public OutputBean() {}

    /**
     * Create an {@code OutputBean} from a {@code JSONObject}.
     *
     * <p>The {@code JSONObject} may contain the following properties:</p>
     * <ul>
     *   <li><em>downloadDir</em>: Temporary download location, used to download the file
     *     locally before uploading it or moving it to its final location.
     *     It's recommended to use a temporary location in the OS such as
     *     "/tmp/netcdf" to benefit from the automatic disk clean-up at reboot.
     *     Downloaded files are automatically removed from the downloadDir by the
     *     DownloadManager.
     *     If the final destination is a file on disk, which is on a different
     *     disk or partition than the OS, then the downloadDir should be set to
     *     a temporary folder on the same disk at the destination URI to minimise
     *     the file move processing time.</li>
     *   <li><em>destination</em>: URI representing the folder where the downloaded
     *     files are uploaded or moved after download.
     *     The file is only uploaded to the destination if it's not corrupted.</li>
     * </ul>
     *
     * <p>Examples:</p>
     * <pre class="code">
     * {
     *     "destination": "s3://my_s3_bucket/mirror/nci/gbr4_bgc_924",
     *     "downloadDir": "/tmp/netcdf/gbr4_bgc_924"
     * }</pre>
     *
     * <pre class="code">
     * {
     *     "destination": "file://download_dir/mirror/nci/gbr4_bgc_924",
     *     "downloadDir": "/tmp/netcdf/gbr4_bgc_924"
     * }</pre>
     *
     * @param jsonOutput OutputBean JSON definition
     */
    public OutputBean(JSONObject jsonOutput) {
        if (jsonOutput != null) {
            this.setDownloadDir(jsonOutput.optString("downloadDir", null));
            this.setDestination(jsonOutput.optString("destination", null));
        }
    }

    /**
     * Get the location of the temporary download directory.
     * It's the directory where the files are downloaded before been uploaded
     * or moved to their final destination.
     * @return the location of the temporary download directory.
     */
    public File getDownloadDir() {
        return this.downloadDir;
    }

    /**
     * Set the location of the temporary download directory.
     * It's the directory where the files are downloaded before been uploaded
     * or moved to their final destination.
     * @param downloadDir the location of the temporary download directory.
     */
    public void setDownloadDir(File downloadDir) {
        if (downloadDir == null) {
            throw new IllegalArgumentException("Download directory (property \"downloadDir\") is missing.");
        } else if (downloadDir.exists()) {
            if (!downloadDir.isDirectory()) {
                throw new IllegalArgumentException(String.format("Invalid download directory: %s. " +
                        "The directory exists but it is NOT a directory.",
                        downloadDir));
            } else if (!downloadDir.canRead() || !downloadDir.canWrite()) {
                throw new IllegalArgumentException(String.format("Invalid download directory: %s. " +
                        "The directory exists but it is NOT readable and writable.",
                        downloadDir));
            }
        } else {
            if (!downloadDir.mkdirs()) {
                throw new IllegalArgumentException(String.format("Invalid download directory: %s. " +
                        "The directory does NOT exist and can not be created.",
                        downloadDir));
            }
        }

        this.downloadDir = downloadDir;
    }

    /**
     * Set the location of the temporary download directory.
     * It's the directory where the files are downloaded before been uploaded
     * or moved to their final destination.
     * @param downloadDirStr a {@code String} representing the {@code File} of the location of the temporary download directory.
     */
    public void setDownloadDir(String downloadDirStr) {
        File downloadDir = null;
        if (downloadDirStr != null && !downloadDirStr.isEmpty()) {
            downloadDir = new File(downloadDirStr);
        }

        this.setDownloadDir(downloadDir);
    }

    /**
     * Get the {@code URI} of the file final destination.
     * It's the URI on disk (URI starting with {@code "file://"})
     * or to S3 (URI starting with {@code "s3://"}) where the files
     * are uploaded or moved after been successfully downloaded
     * to the {@code downloadDir}.
     * @return the {@code URI} of the file final destination.
     */
    public URI getDestination() {
        return this.destination;
    }

    /**
     * Set the {@code URI} of the file final destination.
     * It's the URI on disk (URI starting with {@code "file://"})
     * or to S3 (URI starting with {@code "s3://"}) where the files
     * are uploaded or moved after been successfully downloaded
     * to the {@code downloadDir}.
     * @param destinationUri the {@code URI} of the file final destination.
     */
    public void setDestination(URI destinationUri) {
        if (destinationUri == null) {
            throw new IllegalArgumentException("Destination URI (property \"destination\") is missing. " +
                    "The URI must starts with \"s3://\" for S3 files or \"file://\" for system files.");
        }

        Type type = this.getType(destinationUri);
        if (type == null) {
                throw new IllegalArgumentException(String.format("Invalid destination URI: %s. " +
                        "The URI must starts with \"s3://\" for S3 files or \"file://\" for system files.",
                        destinationUri));
        }

        this.destination = destinationUri;
    }

    /**
     * Set the {@code URI} of the file final destination.
     * It's the URI on disk (URI starting with {@code "file://"})
     * or to S3 (URI starting with {@code "s3://"}) where the files
     * are uploaded or moved after been successfully downloaded
     * to the {@code downloadDir}.
     * @param destinationStr a {@code String} representing the {@code URI} of the file final destination.
     */
    public void setDestination(String destinationStr) {
        URI destinationUri = null;
        if (destinationStr != null && !destinationStr.isEmpty()) {
            // If there is no protocol (aka scheme), assume it's a file system URI
            if (!destinationStr.contains("://")) {
                destinationStr = "file://" + destinationStr;
            }

            try {
                destinationUri = new URI(destinationStr);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException(String.format("Invalid destination URI: %s. " +
                        "The URI must starts with \"s3://\" for S3 files or \"file://\" for system files.",
                        destinationStr), ex);
            }
        }

        this.setDestination(destinationUri);
    }

    /**
     * Return the type of destination URI. Either FILE or S3.
     * @return The destination type, if destination is set; {@code null} otherwise.
     */
    public Type getType() {
        if (this.destination == null) {
            return null;
        }

        return this.getType(this.destination);
    }

    private Type getType(URI uri) {
        String uriScheme = uri.getScheme();

        if ("s3".equalsIgnoreCase(uriScheme)) {
            return Type.S3;
        }
        if ("file".equalsIgnoreCase(uriScheme)) {
            return Type.FILE;
        }

        return null;
    }


    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("downloadDir", this.downloadDir);
        json.put("destination", this.destination);

        return json;
    }


    /**
     * enum representing the type of destination {@code URI}.
     */
    public enum Type {
        S3, FILE
    }
}
