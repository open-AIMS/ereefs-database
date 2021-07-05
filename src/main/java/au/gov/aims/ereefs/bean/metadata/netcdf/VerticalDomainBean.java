/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONArray;
import org.json.JSONObject;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.grid.VerticalAxis;
import uk.ac.rdg.resc.edal.position.VerticalCrs;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing the vertical domain of a variable.
 * It's used with the {@link VariableMetadataBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class VerticalDomainBean extends AbstractBean {
    private Double min = null;
    private Double max = null;
    private VerticalCrsBean verticalCrsBean = null;

    private String name = null;
    private List<Double> heightValues = null;

    /**
     * Construct a {@code VerticalDomainBean} from a EDAL {@code VerticalDomain} object.
     * Used when parsing the metadata returned by the UCAR library.
     *
     * @param verticalDomain EDAL VerticalDomain object.
     */
    public VerticalDomainBean(VerticalDomain verticalDomain) {
        if (verticalDomain == null) {
            throw new IllegalArgumentException("VerticalDomain parameter is null.");
        }

        Extent<Double> extent = verticalDomain.getExtent();

        if (extent != null) {
            this.min = extent.getLow();
            this.max = extent.getHigh();
        }

        if (verticalDomain instanceof VerticalAxis) {
            VerticalAxis verticalAxis = (VerticalAxis)verticalDomain;
            this.name = verticalAxis.getName();
            this.heightValues = verticalAxis.getCoordinateValues();
        }

        VerticalCrs verticalCrs = verticalDomain.getVerticalCrs();
        if (verticalCrs != null) {
            this.verticalCrsBean = new VerticalCrsBean(verticalCrs);
        }
    }

    /**
     * Construct a {@code VerticalDomainBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonVerticalDomain JSON serialised VerticalDomainBean.
     */
    public VerticalDomainBean(JSONObject jsonVerticalDomain) {
        if (jsonVerticalDomain == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.min = jsonVerticalDomain.has("min") ? jsonVerticalDomain.optDouble("min") : null;
        this.max = jsonVerticalDomain.has("max") ? jsonVerticalDomain.optDouble("max") : null;

        JSONObject jsonVerticalCrs = jsonVerticalDomain.optJSONObject("verticalCrs");
        if (jsonVerticalCrs != null) {
            this.verticalCrsBean = new VerticalCrsBean(jsonVerticalCrs);
        }

        this.name = jsonVerticalDomain.optString("name", null);
        JSONArray jsonHeightValues = jsonVerticalDomain.optJSONArray("heightValues");
        if (jsonHeightValues != null) {
            int heightCount = jsonHeightValues.length();
            this.heightValues = new ArrayList<Double>(heightCount);
            for (int i=0; i<heightCount; i++) {
                this.heightValues.add(jsonHeightValues.optDouble(i, 0));
            }
        }
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonVerticalDomain = new JSONObject();

        jsonVerticalDomain.put("min", this.min);
        jsonVerticalDomain.put("max", this.max);

        if (this.verticalCrsBean != null) {
            jsonVerticalDomain.put("verticalCrs", this.verticalCrsBean.toJSON());
        }

        jsonVerticalDomain.put("name", this.name);
        jsonVerticalDomain.put("heightValues", this.heightValues);

        return jsonVerticalDomain;
    }


    /**
     * Helper method to find the closest available height to the value provided in parameter.
     *
     * @param targetHeight Requested height.
     * @return Closest available height to the requested height.
     */
    public Double getClosestHeight(Double targetHeight) {
        Integer heightIndex = this.getClosestHeightIndex(targetHeight);
        if (heightIndex == null) {
            return null;
        }

        return this.heightValues.get(heightIndex);
    }

    /**
     * Helper method to find the index of the closest available height to the value provided in parameter.
     *
     * @param targetHeight Requested height.
     * @return Index of the closest available height to the requested height.
     */
    public Integer getClosestHeightIndex(Double targetHeight) {
        if (targetHeight == null || this.heightValues == null || this.heightValues.isEmpty()) {
            return null;
        }

        // Find the closest height.
        int index = 0;
        Double currentHeight, smallestDelta = null;
        for (int i=0; i<this.heightValues.size(); i++) {
            currentHeight = this.heightValues.get(i);
            double currentDelta = Math.abs(currentHeight - targetHeight);
            if (smallestDelta == null || currentDelta < smallestDelta) {
                smallestDelta = currentDelta;
                index = i;
            }
        }

        return index;
    }


    /**
     * Returns the low bound of the {@code VerticalDomainBean} extent.
     * @return the extent low bound.
     */
    public Double getMin() {
        return this.min;
    }

    /**
     * Returns the high bound of the {@code VerticalDomainBean} extent.
     * @return the extent high bound.
     */
    public Double getMax() {
        return this.max;
    }

    /**
     * Returns the {@code VerticalDomainBean} coordinate reference system (CRS).
     * @return the {@code VerticalDomainBean} coordinate reference system (CRS).
     */
    public VerticalCrsBean getVerticalCrsBean() {
        return this.verticalCrsBean;
    }

    /**
     * Returns the {@code VerticalDomainBean} name.
     * @return the {@code VerticalDomainBean} name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the list of available heights for this {@code VerticalDomainBean}.
     * @return the list of available heights.
     */
    public List<Double> getHeightValues() {
        return this.heightValues;
    }
}
