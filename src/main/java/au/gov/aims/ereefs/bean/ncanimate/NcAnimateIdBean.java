/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * NcAnimate ID bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateConfigBean}
 * and in every NcAnimate configuration parts.</p>
 *
 * <p>This configuration part is used to define a {@code NcAnimate configuration ID} or
 * a {@code NcAnimate configuration part ID}.</p>
 *
 * <p>Example for a {@code NcAnimate configuration ID}:</p>
 * <pre class="code">
 * "_id": "products__ncanimate__ereefs__gbr4_v2__temp-wind-salt-current_hourly"</pre>
 *
 * <p>Example for a {@code NcAnimate configuration part ID}:</p>
 * <pre class="code">
 * "_id": {
 *     "id": "default-canvas",
 *     "datatype": "CANVAS"
 * }</pre>
 */
public class NcAnimateIdBean {
    private static final String DATATYPE_PROPERTY_NAME = "datatype";

    private String value;
    private String propertyName;
    private ConfigPartManager.Datatype datatype;

    /**
     * Empty constructor. Use setters to set attributes.
     */
    public NcAnimateIdBean() {}

    /**
     * Create a NcAnimate ID bean part using a datatype and its value.
     * The datatype is only required when defining the ID of an NcAnimate
     * configuration part; the ID of the main configuration
     * {@link NcAnimateConfigBean} do not need a datatype.
     *
     * @param datatype the type of configuration part.
     * @param value the value of the ID.
     */
    public NcAnimateIdBean(ConfigPartManager.Datatype datatype, String value) {
        this.datatype = datatype;
        this.value = value;
        this.propertyName = "id";
    }

    /**
     * Load the attributes of the NcAnimate ID bean part from a {@code JSONWrapperObject}.
     *
     * @param json {@code JSONWrapperObject} representing a NcAnimate ID bean part.
     * @param idPropertyName name of the property used for the ID in the JSON document.
     * @throws Exception if the json object is malformed.
     */
    protected void parse(JSONWrapperObject json, String idPropertyName) throws Exception {
        this.propertyName = idPropertyName;

        if (json != null) {
            String value = json.get(String.class, idPropertyName, this.value);
            if (AbstractNcAnimateBean.validateIdValue(value)) {
                this.value = value;
            }

            this.setDatatype(json.get(String.class, DATATYPE_PROPERTY_NAME));
        }
    }

    private void setDatatype(String datatypeStr) {
        this.datatype = null;

        if (datatypeStr != null && !datatypeStr.isEmpty()) {
            // NOTE: This will produce an exception if the datatype is invalid
            this.datatype = ConfigPartManager.Datatype.valueOf(datatypeStr.toUpperCase());
        }
    }

    /**
     * Returns the value of the ID.
     * @return the value of the ID.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the ID property name.
     * Usually {@code id} or {@code _id}.
     * @return the ID property name.
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    /**
     * Returns the ID {@link au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager.Datatype}.
     * @return the ID {@link au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager.Datatype}.
     */
    public ConfigPartManager.Datatype getDatatype() {
        return this.datatype;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public Object toJSON() {
        if (this.datatype == null) {
            return this.value;
        }

        return new JSONObject()
                .put(this.propertyName, this.value)
                .put(DATATYPE_PROPERTY_NAME, this.datatype == null ? null : this.datatype.name());
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        NcAnimateIdBean that = (NcAnimateIdBean) o;
        return Objects.equals(this.value, that.value) &&
                Objects.equals(this.propertyName, that.propertyName) &&
                this.datatype == that.datatype;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.datatype);
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        Object jsonId = this.toJSON();
        return jsonId == null ? null : jsonId.toString();
    }
}
