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
import org.json.JSONObject;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

/**
 * Bean representing the CRS of the vertical domain of a variable.
 * It's used with the {@link VerticalDomainBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class VerticalCrsBean extends AbstractBean {
    private String units;
    private Boolean pressure;
    private Boolean dimensionless;
    private Boolean positiveUpwards;

    /**
     * Construct a {@code VerticalCrsBean} from a EDAL {@code VerticalCrs} object.
     * Used when parsing the metadata returned by the UCAR library.
     *
     * @param verticalCrs EDAL VerticalCrs object.
     */
    public VerticalCrsBean(VerticalCrs verticalCrs) {
        if (verticalCrs == null) {
            throw new IllegalArgumentException("VerticalCrs parameter is null.");
        }

        this.units = verticalCrs.getUnits();
        this.pressure = verticalCrs.isPressure();
        this.dimensionless = verticalCrs.isDimensionless();
        this.positiveUpwards = verticalCrs.isPositiveUpwards();
    }

    /**
     * Construct a {@code VerticalCrsBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonVerticalCrs JSON serialised VerticalCrsBean.
     */
    public VerticalCrsBean(JSONObject jsonVerticalCrs) {
        if (jsonVerticalCrs == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.units = jsonVerticalCrs.optString("units", null);
        this.pressure = jsonVerticalCrs.has("pressure") ? jsonVerticalCrs.optBoolean("pressure") : null;
        this.dimensionless = jsonVerticalCrs.has("dimensionless") ? jsonVerticalCrs.optBoolean("dimensionless") : null;
        this.positiveUpwards = jsonVerticalCrs.has("positiveUpwards") ? jsonVerticalCrs.optBoolean("positiveUpwards") : null;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonVerticalCrs = new JSONObject();

        jsonVerticalCrs.put("units", this.units);
        jsonVerticalCrs.put("pressure", this.pressure);
        jsonVerticalCrs.put("dimensionless", this.dimensionless);
        jsonVerticalCrs.put("positiveUpwards", this.positiveUpwards);

        return jsonVerticalCrs;
    }

    /**
     * Returns the {@code VerticalCrsBean} units.
     * @return the {@code VerticalCrsBean} units.
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Returns the {@code VerticalCrsBean} pressure.
     * @return the {@code VerticalCrsBean} pressure.
     */
    public Boolean isPressure() {
        return this.pressure;
    }

    /**
     * Return {@code true} if this is a dimensionless (e.g. sigma or terrain-following)
     * coordinate system. If this is true then the units are irrelevant, and isPressure()
     * will return false.
     *
     * @return {@code true} if the {@code VerticalCrsBean} is dimensionless.
     */
    public Boolean isDimensionless() {
        return this.dimensionless;
    }

    /**
     * Indicates whether coordinate values increase upward or downward.
     * @return {@code true} if the coordinate values increase upward.
     */
    public Boolean isPositiveUpwards() {
        return this.positiveUpwards;
    }
}
