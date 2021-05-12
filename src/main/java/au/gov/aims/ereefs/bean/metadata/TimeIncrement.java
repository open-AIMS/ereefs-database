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
package au.gov.aims.ereefs.bean.metadata;

import org.joda.time.Period;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Define a time increment, for the duration of a video, a video frame,
 * a geographical map, a timestamp in a NetCDF file, etc.
 *
 * <p>It's similar to a Joda time Period, but it's easier to serialise
 * and deserialise from {@code JSONObject}.</p>
 *
 * <p>For example, the following TimeIncrement:</p>
 * <pre class="code">
 * increment = 3
 * unit = "DAY"
 * </pre>
 *
 * <p>could be used to generate time ranges like those:</p>
 * <pre class="code">
 * ...
 * 2012-01-02T12:00:00.000+10:00 - 2012-01-05T12:00:00.000+10:00
 * 2012-01-05T12:00:00.000+10:00 - 2012-01-08T12:00:00.000+10:00
 * 2012-01-08T12:00:00.000+10:00 - 2012-01-11T12:00:00.000+10:00
 * 2012-01-11T12:00:00.000+10:00 - 2012-01-14T12:00:00.000+10:00
 * ...
 * </pre>
 */
public class TimeIncrement implements Comparable<TimeIncrement> {
    // Duration of the interval (contains 1 day of data, or 3 months, or 5 years, etc).
    private Integer increment;
    private TimeIncrementUnit unit;

    /**
     * Create a TimeIncrement from a JSON object.
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *   "increment": 3,
     *   "unit": "DAY"
     * }</pre>
     *
     * @param jsonTimeIncrement {@code JSONObject} representing a TimeIncrement object.
     */
    public TimeIncrement(JSONObject jsonTimeIncrement) {
        this.parse(jsonTimeIncrement);
    }

    /**
     * Create a TimeIncrement from an increment and a unit (string).
     *
     * <p>See {@link TimeIncrementUnit} for accepted units.</p>
     *
     * @param timeIncrement the increment.
     * @param timeIncrementUnit the unit.
     */
    public TimeIncrement(Integer timeIncrement, String timeIncrementUnit) {
        this(
            timeIncrement,
            TimeIncrementUnit.parse(timeIncrementUnit));
    }

    /**
     * Create a TimeIncrement from an increment and a unit.
     *
     * @param increment the increment.
     * @param unit the unit.
     */
    public TimeIncrement(Integer increment, TimeIncrementUnit unit) {
        this.increment = increment;
        this.unit = unit;
    }

    private void parse(JSONObject jsonTimeIncrement) {
        if (jsonTimeIncrement == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.increment = jsonTimeIncrement.has("increment") ? jsonTimeIncrement.optInt("increment", 1) : null;
        this.unit = TimeIncrementUnit.parse(jsonTimeIncrement.optString("unit", null));
    }

    /**
     * Returns the increment.
     * @return the increment.
     */
    public Integer getIncrement() {
        return this.increment;
    }

    /**
     * Returns a non-null increment.
     * @return the increment if not null; 1 otherwise.
     */
    public int getSafeIncrement() {
        return this.increment == null ? 1 : this.increment;
    }

    /**
     * Returns the unit.
     * @return the unit.
     */
    public TimeIncrementUnit getUnit() {
        return this.unit;
    }

    /**
     * Returns a non-null unit.
     * @return the unit if not null; "HOUR" otherwise.
     */
    public TimeIncrementUnit getSafeUnit() {
        return this.unit == null ? TimeIncrementUnit.HOUR : this.unit;
    }

    /**
     * Returns a Joda time Period object representing this TimeIncrement.
     * @return an equivalent Joda time Period.
     */
    public Period getPeriod() {
        return this.getSafeUnit().getPeriod(this.getSafeIncrement());
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TimeIncrement that = (TimeIncrement) o;

        return Objects.equals(this.increment, that.increment) &&
                this.unit == that.unit;
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.increment, this.unit);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(TimeIncrement o) {
        if (this == o) {
            return 0;
        }

        if (o == null) {
            return -1;
        }

        Period thisPeriod = this.getPeriod();
        Period otherPeriod = o.getPeriod();

        if (thisPeriod == otherPeriod) {
            return 0;
        }

        // Move eternity at the end
        if (thisPeriod == null) {
            return 1;
        }
        if (otherPeriod == null) {
            return -1;
        }

        return new PeriodComparator().compare(thisPeriod, otherPeriod);
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        return new JSONObject()
            .put("increment", this.increment)
            .put("unit", this.unit == null ? null : this.unit.name());
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return this.toJSON().toString(4);
    }
}
