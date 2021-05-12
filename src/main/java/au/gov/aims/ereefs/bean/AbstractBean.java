/*
 *  Copyright (C) 2021 Australian Institute of Marine Science
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
     * <p>Valid characters:</p>
     * <ul>
     *     <li>alphanumeric character: {@code a-z}, {@code A-Z}, {@code 0-9}</li>
     *     <li>backslash: {@code \}</li>
     *     <li>hyphen: {@code -}</li>
     *     <li>underscore: {@code _}</li>
     *     <li>slash: {@code /}</li>
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
