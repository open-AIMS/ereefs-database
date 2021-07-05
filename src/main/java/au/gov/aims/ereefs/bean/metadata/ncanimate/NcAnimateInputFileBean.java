/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.ncanimate;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.net.URI;

/**
 * Bean representing an input file used to produce a set of {@code ereefs-ncanimate2} output file.
 * It's part of the {@link NcAnimateOutputFileMetadataBean} which is used with
 * the {@code ereefs-ncanimate2} project.
 */
public class NcAnimateInputFileBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateInputFileBean.class);

    private URI fileURI;
    private String checksum;

    /**
     * Empty constructor. Use setters to set attributes.
     */
    public NcAnimateInputFileBean() {}

    /**
     * Construct a {@code NcAnimateInputFileBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     * @param json JSON serialised NcAnimateInputFileBean.
     */
    public NcAnimateInputFileBean(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.fileURI = this.toURI(json.optString("fileURI", null));
        this.checksum = json.optString("checksum", null);
    }

    private URI toURI(String uriStr) {
        if (uriStr == null || uriStr.isEmpty()) {
            return null;
        }

        try {
            return new URI(uriStr);
        } catch(Exception ex) {
            LOGGER.warn(String.format("Invalid input file fileURI: %s",
                    uriStr));
        }

        return null;
    }

    /**
     * Returns the {@code NcAnimateInputFileBean} file URI.
     * @return the {@code NcAnimateInputFileBean} file URI.
     */
    public URI getFileURI() {
        return this.fileURI;
    }

    /**
     * Set the {@code NcAnimateInputFileBean} file URI.
     * @param fileURI the {@code NcAnimateInputFileBean} file URI.
     */
    public void setFileURI(URI fileURI) {
        this.fileURI = fileURI;
    }

    /**
     * Returns the {@code NcAnimateInputFileBean} file checksum.
     * @return the {@code NcAnimateInputFileBean} file checksum.
     */
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * Set the {@code NcAnimateInputFileBean} file checksum.
     * @param checksum the {@code NcAnimateInputFileBean} file checksum.
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("fileURI", this.fileURI);
        json.put("checksum", this.checksum);

        return json;
    }
}
