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
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.grid.TimeAxis;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing the temporal domain of a variable.
 * It's used with the {@link VariableMetadataBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class TemporalDomainBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(TemporalDomainBean.class);

    private DateTime minDate;
    private DateTime maxDate;

    // Time Axis
    private String name;
    private List<DateTime> timeValues = null;

    /**
     * Construct a {@code TemporalDomainBean} from a EDAL {@code TemporalDomain} object.
     * Used when parsing the metadata returned by the UCAR library.
     *
     * @param temporalDomain EDAL TemporalDomain object.
     */
    public TemporalDomainBean(TemporalDomain temporalDomain) {
        if (temporalDomain == null) {
            throw new IllegalArgumentException("TemporalDomain parameter is null.");
        }

        Extent<DateTime> temporalExtent = temporalDomain.getExtent();
        if (temporalExtent != null) {
            this.minDate = temporalExtent.getLow();
            this.maxDate = temporalExtent.getHigh();
        }

        if (temporalDomain instanceof TimeAxis) {
            TimeAxis timeAxis = (TimeAxis)temporalDomain;

            this.name = timeAxis.getName();
            this.timeValues = timeAxis.getCoordinateValues();
        }
    }

    /**
     * Construct a {@code TemporalDomainBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonTemporalDomain JSON serialised TemporalDomainBean.
     */
    public TemporalDomainBean(JSONObject jsonTemporalDomain) {
        if (jsonTemporalDomain == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.minDate = TemporalDomainBean.deserialiseDateTime(jsonTemporalDomain.optString("minDate", null));
        this.maxDate = TemporalDomainBean.deserialiseDateTime(jsonTemporalDomain.optString("maxDate", null));

        this.name = jsonTemporalDomain.optString("name", null);

        JSONArray jsonTimeValues = jsonTemporalDomain.optJSONArray("timeValues");
        if (jsonTimeValues != null) {
            this.timeValues = new ArrayList<DateTime>(jsonTimeValues.length());
            for (int i=0; i<jsonTimeValues.length(); i++) {
                DateTime jsonTimeValue = TemporalDomainBean.deserialiseDateTime(jsonTimeValues.optString(i, null));
                if (jsonTimeValue != null) {
                    this.timeValues.add(jsonTimeValue);
                }
            }
        }
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonTemporalDomain = new JSONObject();

        jsonTemporalDomain.put("minDate", TemporalDomainBean.serialiseDateTime(this.minDate));
        jsonTemporalDomain.put("maxDate", TemporalDomainBean.serialiseDateTime(this.maxDate));

        jsonTemporalDomain.put("name", this.name);

        if (this.timeValues != null && !this.timeValues.isEmpty()) {
            JSONArray jsonTimeValues = new JSONArray();
            for (DateTime timeValue : this.timeValues) {
                jsonTimeValues.put(TemporalDomainBean.serialiseDateTime(timeValue));
            }
            jsonTemporalDomain.put("timeValues", jsonTimeValues);
        }

        return jsonTemporalDomain;
    }

    private static String serialiseDateTime(DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // Write dates in UTC timezone for consistency
        return dateTime.withZone(DateTimeZone.UTC).toString();
    }

    private static DateTime deserialiseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }

        return new DateTime(dateTimeStr);
    }

    /**
     * Returns the {@code TemporalDomainBean} minimum date.
     * @return the {@code TemporalDomainBean} minimum date.
     */
    public DateTime getMinDate() {
        return this.minDate;
    }

    /**
     * Returns the {@code TemporalDomainBean} maximum date.
     * @return the {@code TemporalDomainBean} maximum date.
     */
    public DateTime getMaxDate() {
        return this.maxDate;
    }

    /**
     * Returns the {@code TemporalDomainBean} name.
     * @return the {@code TemporalDomainBean} name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the {@code TemporalDomainBean} list of {@code DateTime}.
     * @return the {@code TemporalDomainBean} list of {@code DateTime}.
     */
    public List<DateTime> getTimeValues() {
        return this.timeValues;
    }
}
