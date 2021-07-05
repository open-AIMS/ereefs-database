/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table.key;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Object representing a primary key in a database table.
 *
 * <p>A primary key can be a single key value attribute,
 * usually named {@code _id}, but it can also be an JSON
 * object containing an arbitrary number of key values.</p>
 */
public abstract class PrimaryKey implements Comparable<PrimaryKey> {
    private Map<String, Object> keyValues;

    /**
     * Create a primary key from a {@code Map} of values.
     * @param keyValues primary key values.
     */
    public PrimaryKey(Map<String, Object> keyValues) {
        this.keyValues = keyValues;
    }

    /**
     * Returns the primary key values.
     * @return the primary key values.
     */
    public Map<String, Object> getKeyValues() {
        return this.keyValues;
    }

    /**
     * Returns the {@code Bson} filter used to select the primary key.
     * @return the primary key filter.
     */
    public abstract Bson getFilter();

    /**
     * Returns {@code true} is the primary key does not contain any invalid character; {@code false} otherwise.
     * @return {@code true} is the primary key does not contain any invalid character; {@code false} otherwise.
     */
    public boolean validate() {
        return this.validateRecursively(this.keyValues);
    }

    private boolean validateRecursively(Object value) {
        if (value == null) {
            return false;
        }

        // Integer and long are valid ID (no decimal point)
        if (value instanceof Integer || value instanceof Long) {
            return true;
        }

        if (value instanceof String) {
            String valueStr = (String)value;
            return AbstractBean.validateIdValue(valueStr);
        }

        if (value instanceof Map) {
            Map valueMap = (Map)value;
            for (Object entryValue : valueMap.values()) {
                if (!this.validateRecursively(entryValue)) {
                    return false;
                }
            }
            return true;
        }

        if (value instanceof JSONObject) {
            JSONObject json = (JSONObject)value;
            for (String key : json.keySet()) {
                Object entryValue = json.opt(key);
                if (!this.validateRecursively(entryValue)) {
                    return false;
                }
            }
            return true;
        }

        // JSONArray are not allow in ID
        //     "The _id field may contain values of any BSON data type, other than an array."
        //     https://docs.mongodb.com/manual/core/document/#the-_id-field
        /*
        if (value instanceof JSONArray) {
            JSONArray json = (JSONArray)value;
            for (int i=0; i<json.length(); i++) {
                Object entryValue = json.opt(i);
                if (!this.validateRecursively(entryValue)) {
                    return false;
                }
            }
            return true;
        }
        */

        throw new IllegalArgumentException(String.format("Unsupported primary key value class: %s%nPrimary key: %s",
                value.getClass(), this));
    }

    /**
     * Serialise the primary key into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the primary key.
     */
    public JSONObject toJSON() {
        return new JSONObject()
                .put("primaryKey", this.keyValues);
    }

    /**
     * Returns a string representation of the primary key.
     * @return a string representation of the primary key.
     */
    @Override
    public String toString() {
        return this.toJSON().toString(4);
    }

    /**
     * Compares this primary key with the specified primary key for order. Returns a
     * negative integer, zero, or a positive integer as this primary key is less
     * than, equal to, or greater than the specified primary key.
     *
     * @param   o the primary key to be compared.
     * @return  a negative integer, zero, or a positive integer as this primary key
     *          is less than, equal to, or greater than the specified primary key.
     */
    @Override
    public int compareTo(PrimaryKey o) {
        if (this == o) {
            return 0;
        }

        if (this.keyValues == null) {
            return 1;
        }
        if (o.keyValues == null) {
            return -1;
        }

        // Order keys in alphabetical order
        List<String> thisKeys = new ArrayList<String>(this.keyValues.keySet());
        Collections.sort(thisKeys);
        List<String> otherKeys = new ArrayList<String>(o.keyValues.keySet());
        Collections.sort(otherKeys);

        int thisKeyCount = thisKeys.size();
        int otherKeyCount = otherKeys.size();
        int minKeyCount = Math.min(thisKeyCount, otherKeyCount);

        for (int i=0; i<minKeyCount; i++) {
            // Compare key
            String thisKey = thisKeys.get(i);
            String otherKey = otherKeys.get(i);
            int keyCmp = this.simpleCompare(thisKey, otherKey);
            if (keyCmp != 0) {
                return keyCmp;
            }

            // Compare value
            Object thisVal = this.keyValues.get(thisKey);
            Object otherVal = o.keyValues.get(otherKey);
            int valCmp = this.simpleCompare(thisVal, otherVal);
            if (valCmp != 0) {
                return valCmp;
            }
        }

        // All common values are identical. Check if one has more values
        return thisKeyCount - otherKeyCount;
    }

    private int simpleCompare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }

        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }

        String str1 = o1.toString();
        String str2 = o2.toString();

        return str1.compareTo(str2);
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
        PrimaryKey that = (PrimaryKey) o;
        return Objects.equals(this.keyValues, that.keyValues);
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.keyValues);
    }
}
