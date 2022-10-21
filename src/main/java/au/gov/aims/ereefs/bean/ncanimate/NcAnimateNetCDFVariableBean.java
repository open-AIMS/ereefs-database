/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * NcAnimate {@code NetCDF} variable bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateLayerBean}.</p>
 *
 * <p>Used to define NetCDF variables.</p>
 *
 * <p>Examples:</p>
 * <pre class="code">
 * {
 *     "variableId": "salt",
 *     "legend": {
 *         "title": { "text": "Salinity (PSU)" }
 *     },
 *     "colourPaletteName": "RedBlueRainbowSalt_24-36-PSU",
 *     "colourSchemeType": "scale"
 *     "scaleMin": 24,
 *     "scaleMax": 36
 * }</pre>
 * <pre class="code">
 * {
 *     "variableId": "nom",
 *     "legend": {
 *         "title": { "text": "Normanby river" }
 *     },
 *     "colourPaletteName": "transparentGreen",
 *     "colourSchemeType": "thresholds"
 *     "thresholds": [0.1, 0.5, 0.8]
 * }</pre>
 */
public class NcAnimateNetCDFVariableBean extends AbstractNcAnimateBean {
    
    public enum ColourSchemeType {
        SCALE(),
        THRESHOLDS()
    }
    
    private String variableId;
    private String colourPaletteName;
    private Boolean logarithmic;
    
    private ColourSchemeType colourSchemeType;

    private Float scaleMin;
    private Float scaleMax;
    
    private ArrayList<Float> thresholds;

    // Used with direction variable

    // Angle of the north, in degree.
    // Used to fix data with "0" angle not point North.
    // Example: NOAA data 0 is pointing South instead of North, it needs northAngle = 180.
    // Default: 0.
    private Float northAngle;

    // For degree:
    //     directionTurns = 360
    // For radian:
    //     directionTurns = 6.2831853 // 2*PI
    // See: https://en.wikipedia.org/wiki/Angular_unit
    // NOTE: For counter clockwise angle, use a negative directionTurns
    //     directionTurns = 360:
    //         North: 0
    //         East: 90
    //         South: 180
    //         West: 270
    //     directionTurns = -360:
    //         North: 0
    //         West: 90
    //         South: 180
    //         East: 270
    // Default: 360
    private Float directionTurns;

    // Legend
    private NcAnimateLegendBean legend;

    /**
     * Create a NcAnimate NetCDF layer variable bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>variableId</em>: the ID of the variable, as defined in the NetCDF file.</li>
     *   <li><em>colourPaletteName</em>: the name of colour palette to use to render the data.</li>
     *   <li><em>colourSchemeType</em>: the type of the colour scheme. Possible values: 'thresholds' and 'scale'.</li>
     *   <li><em>scaleMax</em>: expected maximum value in the data.</li>
     *   <li><em>scaleMin</em>: expected minimum value in the data.</li>
     *   <li><em>thresholds</em>: threshold values as float array.</li>
     *   <li><em>legend</em>: legend bean ID or serialised {@link NcAnimateLegendBean}.</li>
     *   <li><em>logarithmic</em>: {@code true} to render the data using a logarithmic scale. Experimental.</li>
     * </ul>
     *
     * <p>NOTE: {@code GRIB2} are not as standard as NetCDF. Vector layers are sometime defined
     *     as a degree variable and a amplitude variable. The angle variable is arbitrary. 0° doesn't
     *     always point North as expected. It may point to any angle.</p>
     *
     * <p>Extra attributes for {@code GRIB2} files:</p>
     * <ul>
     *   <li><em>northAngle</em>: angle at which North coordinate is point to, in degree.
     *       For example, in NOAA data, 0° points South. Therefore, North is at 180°. Default: 0.</li>
     *   <li><em>directionTurns</em>: 360 degree for clockwise rotation. -360 degree for counter-clockwise.
     *       If another unit than degree is used, use the number of unit for a full circle.
     *       For example, if the unit is radian and it's turning counter-clockwise,
     *       set this value to {@code -2π}, approximately {@code -6.2831853}. Default: 360.</li>
     * </ul>
     *
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF layer variable bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateNetCDFVariableBean(JSONWrapperObject jsonVariable) throws Exception {
        super(jsonVariable);
    }

    /**
     * Create a NcAnimate NetCDF layer variable bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateNetCDFVariableBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate NetCDF layer variable bean part ID.
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF layer variable bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateNetCDFVariableBean(String id, JSONWrapperObject jsonVariable) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.VARIABLE, id), jsonVariable);
    }

    /**
     * Load the attributes of the NcAnimate NetCDF layer variable bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF layer variable bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonVariable) throws Exception {
        super.parse(jsonVariable);
        if (jsonVariable != null) {
            this.variableId = jsonVariable.get(String.class, "variableId");
            this.colourPaletteName = jsonVariable.get(String.class, "colourPaletteName");
            
            this.setColourSchemeType(jsonVariable.get(String.class, "colourSchemeType"));
            
            this.scaleMax = jsonVariable.get(Float.class, "scaleMax");
            this.scaleMin = jsonVariable.get(Float.class, "scaleMin");

            this.setThresholds(jsonVariable.get(JSONWrapperArray.class, "thresholds"));
            
            this.northAngle = jsonVariable.get(Float.class, "northAngle");
            this.directionTurns = jsonVariable.get(Float.class, "directionTurns");

            this.logarithmic = jsonVariable.get(Boolean.class, "logarithmic");

            this.setLegend(jsonVariable.get(JSONWrapperObject.class, "legend"));
        }
    }

    private void setLegend(JSONWrapperObject jsonLegend) throws Exception {
        this.legend = null;

        if (jsonLegend != null) {
            this.legend = new NcAnimateLegendBean(jsonLegend);
        }
    }

    /**
     * Returns the variable ID, as defined in the NetCDF file.
     * @return the variable ID.
     */
    public String getVariableId() {
        return this.variableId == null ? this.getId().getValue() : this.variableId;
    }

    /**
     * Set the colour scheme type to either THRESHOLDS or SCALE.
     * @param typeString The string representing the colour scheme type (case-insensitive).
     */
    public void setColourSchemeType(String typeString) {
        this.colourSchemeType = null;
        
        if (typeString != null) {
            if (typeString.equalsIgnoreCase(ColourSchemeType.THRESHOLDS.toString())) {
                this.colourSchemeType = ColourSchemeType.THRESHOLDS;
            }
            else if (typeString.equalsIgnoreCase(ColourSchemeType.SCALE.toString())) {
                this.colourSchemeType = ColourSchemeType.SCALE;
            }
        }
    }

    /**
     * Return the colour scheme type.
     * @return The colour scheme type.
     */
    public ColourSchemeType getColourSchemeType() {
        return this.colourSchemeType;
    }

    /**
     * Returns the colour palette name to use to render the data.
     * @return the colour palette name.
     */
    public String getColourPaletteName() {
        return this.colourPaletteName;
    }

    /**
     * Returns the maximum value to use on the scale.
     * @return the maximum scale value.
     */
    public Float getScaleMax() {
        return this.scaleMax;
    }

    /**
     * Returns the minimum value to use on the scale.
     * @return the minimum scale value.
     */
    public Float getScaleMin() {
        return this.scaleMin;
    }

    /**
     * Set the thresholds array
     * @param thresholdsWrapperArray The threshold values
     * @throws Exception if the thresholdsWrapperArray does not contain valid data
     */
    public void setThresholds(JSONWrapperArray thresholdsWrapperArray) throws Exception {
        this.thresholds = null;

        if (thresholdsWrapperArray != null && thresholdsWrapperArray.length() > 0) {
            this.thresholds = new ArrayList<>();

            for (int i = 0; i < thresholdsWrapperArray.length(); i++) {
                this.thresholds.add(thresholdsWrapperArray.get(Float.class, i));
            }
        }
    }

    /**
     * Returns the list of thresholds.
     *
     * @return the list of threshold values.
     */
    public ArrayList<Float> getThresholds() {
        return this.thresholds;
    }

    /**
     * Returns the {@code GRIB2} angle at which North is pointing,
     * if the variable is a angle variable.
     *
     * @return the {@code GRIB2} north angle.
     */
    public Float getNorthAngle() {
        return this.northAngle;
    }

    /**
     * Returns the {@code GRIB2} number of units in a full circle
     * and the direction of rotation,
     * if the variable is a angle variable.
     *
     * @return the direction turns value.
     */
    public Float getDirectionTurns() {
        return this.directionTurns;
    }

    /**
     * Returns {@code true} if the variable is logarithmic; {@code false} otherwise.
     * @return {@code true} if the variable is logarithmic.
     */
    public Boolean isLogarithmic() {
        return this.logarithmic;
    }

    /**
     * Returns the {@link NcAnimateLegendBean} object defining how to render the legend.
     * @return the {@link NcAnimateLegendBean}.
     */
    public NcAnimateLegendBean getLegend() {
        return this.legend;
    }

    /**
     * Set the {@link NcAnimateLegendBean} object defining how to render the legend.
     * @param legend the {@link NcAnimateLegendBean}.
     */
    public void setLegend(NcAnimateLegendBean legend) {
        this.legend = legend;
    }

    // NOTE: equals and hashcode are defined in AbstractNcAnimateBean

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("variableId", this.variableId);

        json.put("colourPaletteName", this.colourPaletteName);

        json.put("colourSchemeType", this.colourSchemeType);
        
        json.put("scaleMax", this.scaleMax);
        json.put("scaleMin", this.scaleMin);

        if (this.thresholds != null && !this.thresholds.isEmpty()) {
            json.put("thresholds", new JSONArray(this.thresholds));
        }

        json.put("northAngle", this.northAngle);
        json.put("directionTurns", this.directionTurns);

        json.put("logarithmic", this.logarithmic);

        if (this.legend != null) {
            json.put("legend", this.legend.toJSON());
        }

        return json.isEmpty() ? null : json;
    }
}
