/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import org.json.JSONObject;

import java.util.regex.Pattern;

public abstract class AbstractBean {
    private static final String ID_PATTERN_STR = "a-zA-Z0-9-_/";
    private static final Pattern ID_PATTERN = Pattern.compile("[" + ID_PATTERN_STR + "]+");

    public abstract JSONObject toJSON();

    public static boolean validateIdValue(String value) {
        boolean valid = value == null ||
                value.isEmpty() ||
                ID_PATTERN.matcher(value).matches();

        if (!valid) {
            throw new IllegalArgumentException(String.format("Invalid character found in ID value. " +
                    "Characters allowed are alphanumeric characters, hyphen (-), underscore (_) and slash (/). " +
                    "Id value found: %s", value));
        }

        return valid;
    }

    /**
     * Utility method used to ensure IDs are suitable for the database.
     * Invalid characters are replaced with underscore {@code _}.
     *
     * NOTE: IDs should follow path name rules, so they can be used as ID
     *     in document object. For example, when listing input files
     *     and their respective MD5 checksum.
     *
     * Mongo DB has very little limitations:
     *     https://docs.mongodb.com/manual/reference/limits/
     *
     * The most problematic characters are "." and "$".
     * This library only allows the following characters,
     * just to be sure it also plays well with most JSON parsers:
     *
     * <p>Valid characters:</p>
     * <ul>
     *     <li>alphanumeric character: {@code a-z}, {@code A-Z}, {@code 0-9}</li>
     *     <li>slash and backslash: {@code /}, {@code \}</li>
     *     <li>hyphen and underscore: {@code -}, {@code _}</li>
     * </ul>
     *
     * @param rawId ID which may contain invalid characters.
     * @return ID without invalid characters.
     */
    public static String safeIdValue(String rawId) {
        //return rawId.replaceAll("[^a-zA-Z0-9\\-_/]+", "_");
        return rawId.replaceAll("[^" + ID_PATTERN_STR + "]+", "_");
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return this.toJSON().toString().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!obj.getClass().isAssignableFrom(this.getClass())) {
            return false;
        }

        AbstractBean other = (AbstractBean)obj;

        String thisStr = this.toJSON().toString();
        String otherStr = other.toJSON().toString();

        return thisStr.equals(otherStr);
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        JSONObject json = this.toJSON();
        return json == null ? null : json.toString(4);
    }
}
