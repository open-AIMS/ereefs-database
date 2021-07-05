/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * NcAnimate input bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateLayerBean}.</p>
 *
 * <p>Used to define a {@code NETCDF} layer input file source.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "timeIncrement": {
 *         "increment": 1,
 *         "unit": "HOUR"
 *     },
 *     "licence": "Creative Commons Attribution 4.0 International (https://creativecommons.org/licenses/by/4.0/)",
 *     "author": "eReefs CSIRO GBR4 Hydrodynamic Model v2.0"
 * }</pre>
 */
public class NcAnimateInputBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateInputBean.class);

    // NOTE: File URI is found with MetadataHelper

    // Period of time covered by each frame of the file
    private TimeIncrement timeIncrement;

    private String licence; // Example: "CC-BY 4.0 Aust"
    private List<String> authors; // Example: "eReefs CSIRO GBR4 Hydrodynamic Model v2.0"

    /**
     * Create a NcAnimate input bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>timeIncrement</em>: the period of time represented by each dates in the input data.</li>
     *   <li><em>licence</em>: the licence of the input files.</li>
     *   <li><em>author</em>: the author of the input files.</li>
     *   <li><em>authors</em>: {@code JSONWrapperArray} of authors, if there is more than one author.</li>
     * </ul>
     *
     * @param jsonInput {@code JSONWrapperObject} representing a NcAnimate input bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateInputBean(JSONWrapperObject jsonInput) throws Exception {
        super(jsonInput);
    }

    /**
     * Create a NcAnimate input bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateInputBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate input bean part ID.
     * @param jsonInput {@code JSONWrapperObject} representing a NcAnimate input bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateInputBean(String id, JSONWrapperObject jsonInput) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.INPUT, id), jsonInput);
    }

    /**
     * Load the attributes of the NcAnimate input bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonInput {@code JSONWrapperObject} representing a NcAnimate input bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonInput) throws Exception {
        super.parse(jsonInput);
        if (jsonInput != null) {
            this.timeIncrement = AbstractNcAnimateBean.parseTimeIncrement(
                    jsonInput.get(JSONWrapperObject.class, "timeIncrement"));

            this.licence = jsonInput.get(String.class, "licence");
            if (jsonInput.has("author")) {
                this.addAuthor(jsonInput.get(String.class, "author"));
            }
            if (jsonInput.has("authors")) {
                this.addAuthors(jsonInput.get(JSONWrapperArray.class, "authors"));
            }
        }
    }

    /**
     * Add a list of author.
     * @param authors {@code JSONWrapperArray} containing a list of author.
     * @throws InvalidClassException if the json object contains something else than {@code String}.
     */
    public void addAuthors(JSONWrapperArray authors) throws InvalidClassException {
        for (int i=0; i<authors.length(); i++) {
            this.addAuthor(authors.get(String.class, i, null));
        }
    }

    /**
     * Add a single author to the list.
     * @param author the author to add.
     */
    public void addAuthor(String author) {
        if (author != null && !author.isEmpty()) {
            if (this.authors == null) {
                this.authors = new ArrayList<String>();
            }
            if (!this.authors.contains(author)) {
                this.authors.add(author);
            }
        }
    }

    /**
     * Returns the list of authors.
     * @return the list of authors.
     */
    public List<String> getAuthors() {
        return this.authors;
    }

    /**
     * Returns the period of time represented by each dates in the input data.
     * @return the input data {@link TimeIncrement}.
     */
    public TimeIncrement getTimeIncrement() {
        return this.timeIncrement;
    }

    /**
     * Returns the input file licence.
     * @return the input file licence.
     */
    public String getLicence() {
        return this.licence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.timeIncrement != null) {
            json.put("timeIncrement", this.timeIncrement.toJSON());
        }

        json.put("licence", this.licence);
        if (this.authors != null && !this.authors.isEmpty()) {
            JSONArray jsonAuthors = new JSONArray();
            for (String author : this.authors) {
                jsonAuthors.put(author);
            }
            json.put("authors", jsonAuthors);
        }

        return json.isEmpty() ? null : json;
    }
}
