/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONObject;
import org.opengis.metadata.extent.GeographicBoundingBox;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.BoundingBoxImpl;
import uk.ac.rdg.resc.edal.util.GISUtils;

/**
 * Bean representing the horizontal domain of a variable.
 * It's used with the {@link VariableMetadataBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class HorizontalDomainBean extends AbstractBean {
    private Double minLon;
    private Double minLat;
    private Double maxLon;
    private Double maxLat;

    /**
     * Construct a {@code HorizontalDomainBean} from a EDAL {@code HorizontalDomain} object.
     * Used when parsing the metadata returned by the UCAR library.
     * @param horizontalDomain EDAL HorizontalDomain object.
     */
    public HorizontalDomainBean(HorizontalDomain horizontalDomain) {
        if (horizontalDomain == null) {
            throw new IllegalArgumentException("HorizontalDomain parameter is null.");
        }

        GeographicBoundingBox bbox = horizontalDomain.getGeographicBoundingBox();

        this.minLon = bbox.getWestBoundLongitude();
        this.minLat = bbox.getSouthBoundLatitude();

        this.maxLon = bbox.getEastBoundLongitude();
        this.maxLat = bbox.getNorthBoundLatitude();
    }

    /**
     * Construct a {@code HorizontalDomainBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     * @param jsonHorizontalDomain JSON serialised HorizontalDomainBean.
     */
    public HorizontalDomainBean(JSONObject jsonHorizontalDomain) {
        if (jsonHorizontalDomain == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.minLon = jsonHorizontalDomain.has("minLon") ? jsonHorizontalDomain.optDouble("minLon", 0) : null;
        this.minLat = jsonHorizontalDomain.has("minLat") ? jsonHorizontalDomain.optDouble("minLat", 0) : null;

        this.maxLon = jsonHorizontalDomain.has("maxLon") ? jsonHorizontalDomain.optDouble("maxLon", 0) : null;
        this.maxLat = jsonHorizontalDomain.has("maxLat") ? jsonHorizontalDomain.optDouble("maxLat", 0) : null;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonHorizontalDomain = new JSONObject();

        jsonHorizontalDomain.put("minLon", this.minLon);
        jsonHorizontalDomain.put("minLat", this.minLat);

        jsonHorizontalDomain.put("maxLon", this.maxLon);
        jsonHorizontalDomain.put("maxLat", this.maxLat);

        return jsonHorizontalDomain;
    }

    /**
     * Returns the {@code HorizontalDomainBean} minimum longitude.
     * @return the {@code HorizontalDomainBean} minimum longitude.
     */
    public Double getMinLon() {
        return this.minLon;
    }

    /**
     * Returns the {@code HorizontalDomainBean} minimum latitude.
     * @return the {@code HorizontalDomainBean} minimum latitude.
     */
    public Double getMinLat() {
        return this.minLat;
    }

    /**
     * Returns the {@code HorizontalDomainBean} maximum longitude.
     * @return the {@code HorizontalDomainBean} maximum longitude.
     */
    public Double getMaxLon() {
        return this.maxLon;
    }

    /**
     * Returns the {@code HorizontalDomainBean} maximum latitude.
     * @return the {@code HorizontalDomainBean} maximum latitude.
     */
    public Double getMaxLat() {
        return this.maxLat;
    }

    /**
     * Returns the {@code HorizontalDomainBean} bounding box.
     * @return the {@code HorizontalDomainBean} bounding box.
     */
    public BoundingBox getBoundingBox() {
        return new BoundingBoxImpl(minLon, minLat, maxLon, maxLat, GISUtils.defaultGeographicCRS());
    }
}
