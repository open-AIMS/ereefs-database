/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.bean.AbstractBean;
import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONUtils;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Base class used for all NcAnimate configuration database beans.
 */
public abstract class AbstractNcAnimateBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(AbstractNcAnimateBean.class);

    private static final String MONGODB_ID_PROPERTY_NAME = "_id";
    private static final String INTERNAL_ID_PROPERTY_NAME = "id";
    private static final String LAST_MODIFIED_PROPERTY_NAME = "lastModified";
    private static final String HIDDEN_PROPERTY_NAME = "hidden";

    private static final Pattern ID_PATTERN = Pattern.compile("[a-zA-Z0-9\\-_/]+");

    private NcAnimateIdBean id;
    private ConfigPartManager.Datatype datatype;
    private Boolean hidden;
    private Long lastModified;

    private Set<String> neverVisited;

    /**
     * Create a NcAnimate bean or bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>_id</em>: serialised {@link NcAnimateIdBean}</li>
     *   <li><em>lastModified</em>: last modified timestamp (long integer)</li>
     *   <li><em>hidden</em>: set to false to prevent NcAnimate from rendering the element. Default: true</li>
     * </ul>
     *
     * @param json {@code JSONWrapperObject} representing a NcAnimate bean or bean part.
     * @throws Exception if the json object is malformed.
     */
    public AbstractNcAnimateBean(JSONWrapperObject json) throws Exception {
        this(null, json);
    }

    /**
     * Create a NcAnimate bean or bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>lastModified</em>: last modified timestamp (long integer)</li>
     *   <li><em>hidden</em>: set to false to prevent NcAnimate from rendering the element. Default: true</li>
     * </ul>
     *
     * @param id the ID of the NcAnimate bean or bean part.
     * @param json {@code JSONWrapperObject} representing a NcAnimate bean or bean part.
     * @throws Exception if the json object is malformed.
     */
    public AbstractNcAnimateBean(NcAnimateIdBean id, JSONWrapperObject json) throws Exception {
        this.id = id;

        if (json != null) {
            this.parse(json);
            this.neverVisited = json.getNeverVisited();
        } else {
            this.neverVisited = new TreeSet<String>();
        }
    }

    /**
     * Load the attributes of a NcAnimate bean or bean part from a {@code JSONWrapperObject}.
     *
     * <p>NOTE: Sub classes are expected to extends this method.</p>
     *
     * @param json {@code JSONWrapperObject} representing a NcAnimate bean or bean part.
     * @throws Exception if the json object is malformed.
     */
    protected void parse(JSONWrapperObject json) throws Exception {
        if (json != null) {
            this.id = new NcAnimateIdBean();

            // ID used for replacing parts of config with a configPart
            String internalId = json.get(String.class, INTERNAL_ID_PROPERTY_NAME);
            if (internalId != null) {
                this.id = new NcAnimateIdBean(null, internalId);
            }

            Class mongodbIdClass = json.getClass(MONGODB_ID_PROPERTY_NAME);
            if (JSONWrapperObject.class.equals(mongodbIdClass)) {
                this.id.parse(json.get(JSONWrapperObject.class, MONGODB_ID_PROPERTY_NAME), INTERNAL_ID_PROPERTY_NAME);
            } else if (String.class.equals(mongodbIdClass)) {
                this.id.parse(json, MONGODB_ID_PROPERTY_NAME);
            }

            this.setLastModified(json);
            this.hidden = json.get(Boolean.class, HIDDEN_PROPERTY_NAME);
        }
    }

    private void setLastModified(JSONWrapperObject json) throws InvalidClassException {
        this.lastModified = null;
        if (json != null && json.has(LAST_MODIFIED_PROPERTY_NAME)) {
            String lastModifiedStr = json.get(String.class, LAST_MODIFIED_PROPERTY_NAME, null);

            try {
                DateTime lastModifiedDate = DateTime.parse(lastModifiedStr);
                if (lastModifiedDate != null) {
                    this.lastModified = lastModifiedDate.getMillis();
                }
            } catch(Exception ex) {
                LOGGER.warn(String.format("Invalid %s for %s: %s", LAST_MODIFIED_PROPERTY_NAME, this.id, lastModifiedStr));
            }
        }
    }

    private static boolean validateId(String id) {
        boolean valid = id == null ||
                id.isEmpty() ||
                ID_PATTERN.matcher(id).matches();

        if (!valid) {
            throw new IllegalArgumentException(String.format("Invalid character in ID. " +
                    "Characters allowed are alphanumeric characters, hyphen (-), underscore (_) and slash (/). " +
                    "Id found: %s", id));
        }

        return valid;
    }

    /**
     * Creates a {@link TimeIncrement} from a {@code JSONWrapperObject}.
     *
     * <p>NOTE: Using new TimeIncrement(jsonTimeIncrement.toJSON()) would work but the read
     *     on the attributes won't be recorded. NcAnimate config is monitored to be sure
     *     there is no unused config element lurking around.</p>
     *
     * @param jsonTimeIncrement {@code JSONWrapperObject} representing a {@link TimeIncrement}.
     * @return A TimeIncrement object, or null if input parameter is null.
     * @throws InvalidClassException if the json object is malformed.
     */
    public static TimeIncrement parseTimeIncrement(JSONWrapperObject jsonTimeIncrement) throws InvalidClassException {
        if (jsonTimeIncrement == null) {
            return null;
        }

        return new TimeIncrement(
                jsonTimeIncrement.get(Integer.class, "increment"),
                jsonTimeIncrement.get(String.class, "unit")
        );
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        return new JSONObject()
            .put(INTERNAL_ID_PROPERTY_NAME, this.id == null ? null : this.id.getValue())
            // Cast long into String: JSONObject have issues with long
            .put(LAST_MODIFIED_PROPERTY_NAME, this.lastModified == null ? null : new DateTime(this.lastModified).toString())
            .put(HIDDEN_PROPERTY_NAME, this.hidden);
    }

    private void setDatatype(String datatypeStr) {
        this.datatype = null;

        if (datatypeStr != null && !datatypeStr.isEmpty()) {
            // NOTE: This will produce an exception if the datatype is invalid
            this.datatype = ConfigPartManager.Datatype.valueOf(datatypeStr.toUpperCase());
        }
    }

    /**
     * Internal configuration overwrite method used to parse the configuration files.
     *
     * <p>Overwrites the values of current object with the values found in parameter {@code overwrites}.</p>
     *
     * @param overwrites a {@link AbstractNcAnimateBean} containing the overwrites values.
     * @throws Exception if something goes wrong during the overwrite process.
     */
    public void overwrite(AbstractNcAnimateBean overwrites) throws Exception {
        this.overwrite(this, overwrites);
    }

    /**
     * Internal configuration overwrite method used to parse the configuration files.
     *
     * <p>Overwrites the current object (this) with <em>base overwritten with overwrites</em>.
     * This is used to change the current object values (this) for the result
     * of <em>this overwritten with base overwritten with overwrites</em>.</p>
     *
     * <p>Useful for situations where the base needs to be reused.</p>
     *
     * <p>It would be easier to return a new configuration bean created by
     * overwritting this with overwrites, but it would be hard to dynamically
     * call the constructor of <em>this</em> object.</p>
     *
     * <p>NOTE: Base stays untouched.</p>
     *
     * @param base a {@link AbstractNcAnimateBean} containing the base values.
     * @param overwrites a {@link AbstractNcAnimateBean} containing the overwrites values.
     * @throws Exception if something goes wrong during the overwrite process.
     */
    public void overwrite(AbstractNcAnimateBean base, AbstractNcAnimateBean overwrites) throws Exception {
        if (base != null && overwrites != null) {
            this.neverVisited.addAll(base.neverVisited);
            this.neverVisited.addAll(overwrites.neverVisited);

            JSONObject jsonBase = base.toJSON();
            JSONObject jsonOverwrites = overwrites.toJSON();

            JSONObject overwrittenJson = JSONUtils.overwrite(jsonBase, jsonOverwrites, MONGODB_ID_PROPERTY_NAME);
            this.parse(new JSONWrapperObject(overwrittenJson));
        }
    }

    /**
     * Returns the NcAnimate bean or bean part ID.
     * @return the NcAnimate bean or bean part ID.
     */
    public NcAnimateIdBean getId() {
        return this.id;
    }

    /**
     * Returns the NcAnimate bean or bean part last modified timestamp.
     * @return the NcAnimate bean or bean part last modified timestamp.
     */
    public long getLastModified() {
        return this.lastModified == null ? 0 : this.lastModified;
    }

    /**
     * Returns the NcAnimate bean part datatype.
     *
     * <p>See: {@link ConfigPartManager.Datatype}.</p>
     *
     * @return the NcAnimate bean part datatype.
     */
    public ConfigPartManager.Datatype getDatatype() {
        return this.datatype;
    }

    /**
     * Returns the NcAnimate bean part hidden value.
     *
     * <p>If {@code hidden} is {@code true}, NcAnimate won't render
     * the graphical element define by the NcAnimate bean part.</p>
     *
     * @return the NcAnimate bean part hidden value.
     */
    public boolean isHidden() {
        return this.hidden == null ? false : this.hidden;
    }

    /**
     * Collate the list of all attributes which were not parsed.
     * This is used during the parsing of the NcAnimate configuration files.
     * Use {@link #getNeverVisited()} to get the list of configuration
     * attributes which were never parsed.
     *
     * @param parentPath
     * @param neverVisited
     */
    public void addAllNeverVisited(String parentPath, Set<String> neverVisited) {
        if (neverVisited != null) {
            for (String relativePath : neverVisited) {
                this.neverVisited.add(
                        (parentPath == null ? "" : parentPath + ".") + relativePath);
            }
        }
    }

    /**
     * Returns the list of configuration attributes which were
     * not parsed.
     *
     * <p>NOTE: NcAnimate throws an exception its configuration
     *   contains any unparsed attribute. This is used to prevent
     *   NcAnimate from wasting hours generating files when
     *   there is typos in its configuration files.</p>
     *
     * @return set of configuration attributes that were not parsed,
     *   therefore are likely to be mistyped.
     */
    public Set<String> getNeverVisited() {
        return this.neverVisited;
    }
}
