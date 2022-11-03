/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

/**
 * NcAnimate NetCDF layer legend bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateNetCDFVariableBean}
 * and in {@link NcAnimateDefaultsBean}.</p>
 *
 * <p>Used to define how to render legend for a {@code NetCDF} layer.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "colourBandWidth": 20,
 *     "colourBandHeight": 300,
 *     "backgroundColour": "#FFFFFF99",
 *     "steps": 7,
 *     "position": {
 *         "bottom": 5,
 *         "left": 5
 *     },
 *     "title": {
 *         "fontColour": "#000000",
 *         "fontSize": 16,
 *         "bold": true,
 *         "position": {
 *             "top": 10
 *         }
 *     },
 *     "label": {
 *         "fontSize": 14,
 *         "position": {
 *             "left": 5,
 *             "bottom": 5
 *         }
 *     }
 * }</pre>
 */
public class NcAnimateLegendBean extends AbstractNcAnimateBean {
    private NcAnimateTextBean title; // Position of the legendTitle itself and spec for the legendTitle title
    private NcAnimateTextBean label; // Labels in the legend
    private Integer steps; // Number of labels to display in the legend
    private Integer labelPrecision; // Number of digit to display

    // Used to convert to a different unit.
    private Float labelMultiplier;
    private Float labelOffset;

    // Sometimes, the lower or higher label in the legend makes no sense.
    // It's better to not display it.
    private Boolean hideLowerLabel;
    private Boolean hideHigherLabel;

    private NcAnimatePositionBean position;
    private NcAnimatePaddingBean padding;

    private String backgroundColour;

    private Integer colourBandWidth;
    private Integer colourBandHeight;
    private Integer colourBandColourCount; // Number of colours used to render the layer and the legend

    // Add extra space at the top and bottom of the colour band
    private Float extraAmountOutOfRangeLow;
    private Float extraAmountOutOfRangeHigh;

    private Integer majorTickMarkLength;
    private Integer minorTickMarkLength;

    /**
     * Create a NcAnimate NetCDF layer legend bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>title</em>: {@link NcAnimateTextBean} representing the title of the legend,
     *       displayed in a 90° angle, to the right of the colour band.</li>
     *   <li><em>label</em>: {@link NcAnimateTextBean} used to style the numbers on the scale
     *       of the colour band.</li>
     *   <li><em>steps</em>: number of labels to display on the legend.</li>
     *   <li><em>position</em>: {@link NcAnimatePositionBean} representing the position of the
     *       legend in relation to the panel.</li>
     *   <li><em>padding</em>: {@link NcAnimatePaddingBean} representing the padding to add
     *       to the legend background, in pixels.</li>
     *   <li><em>backgroundColour</em>: the colour of the legend background, in hexadecimal value,
     *       in format {@code #RRGGBBAA}.
     *       Example, for a semi transparent white background: {@code #FFFFFF99}</li>
     *   <li><em>colourBandWidth</em>: the width of the colour band, in pixels. Default: 20.</li>
     *   <li><em>colourBandHeight</em>: the height of the colour band, in pixels. Default: 300.</li>
     *   <li><em>majorTickMarkLength</em>: length of the major tick marks in the legend, in pixels. Default: 6.</li>
     *   <li><em>minorTickMarkLength</em>: length of the minor tick marks in the legend, in pixels. Default: 3.</li>
     * </ul>
     *
     * @param jsonLegend {@code JSONWrapperObject} representing a NcAnimate NetCDF layer legend bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateLegendBean(JSONWrapperObject jsonLegend) throws Exception {
        super(jsonLegend);
    }

    /**
     * Create a NcAnimate NetCDF layer legend bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateLegendBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate NetCDF layer legend bean part ID.
     * @param jsonLegend {@code JSONWrapperObject} representing a NcAnimate NetCDF layer legend bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateLegendBean(String id, JSONWrapperObject jsonLegend) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.LEGEND, id), jsonLegend);
    }

    /**
     * Load the attributes of the NcAnimate legend bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonLegend {@code JSONWrapperObject} representing a NcAnimate legend bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonLegend) throws Exception {
        super.parse(jsonLegend);
        if (jsonLegend != null) {
            this.setTitle(jsonLegend.get(JSONWrapperObject.class, "title"));
            this.setLabel(jsonLegend.get(JSONWrapperObject.class, "label"));
            this.steps = jsonLegend.get(Integer.class, "steps");
            this.labelPrecision = jsonLegend.get(Integer.class, "labelPrecision");
            this.labelMultiplier = jsonLegend.get(Float.class, "labelMultiplier");
            this.labelOffset = jsonLegend.get(Float.class, "labelOffset");

            this.hideLowerLabel = jsonLegend.get(Boolean.class, "hideLowerLabel");
            this.hideHigherLabel = jsonLegend.get(Boolean.class, "hideHigherLabel");

            this.setPosition(jsonLegend.get(JSONWrapperObject.class, "position"));
            this.setPadding(jsonLegend.get(JSONWrapperObject.class, "padding"));

            this.backgroundColour = jsonLegend.get(String.class, "backgroundColour");

            this.colourBandWidth = jsonLegend.get(Integer.class, "colourBandWidth");
            this.colourBandHeight = jsonLegend.get(Integer.class, "colourBandHeight");
            this.colourBandColourCount = jsonLegend.get(Integer.class, "colourBandColourCount");

            this.extraAmountOutOfRangeLow = jsonLegend.get(Float.class, "extraAmountOutOfRangeLow");
            this.extraAmountOutOfRangeHigh = jsonLegend.get(Float.class, "extraAmountOutOfRangeHigh");

            this.majorTickMarkLength = jsonLegend.get(Integer.class, "majorTickMarkLength");
            this.minorTickMarkLength = jsonLegend.get(Integer.class, "minorTickMarkLength");
        }
    }

    private void setTitle(JSONWrapperObject jsonTitle) throws Exception {
        this.title = null;

        if (jsonTitle != null) {
            this.title = new NcAnimateTextBean(jsonTitle);
        }
    }

    private void setLabel(JSONWrapperObject jsonLabel) throws Exception {
        this.label = null;

        if (jsonLabel != null) {
            this.label = new NcAnimateTextBean(jsonLabel);
        }
    }

    private void setPosition(JSONWrapperObject jsonPosition) throws Exception {
        this.position = null;

        if (jsonPosition != null) {
            this.position = new NcAnimatePositionBean(jsonPosition);
        }
    }

    private void setPadding(JSONWrapperObject jsonPadding) throws Exception {
        this.padding = null;

        if (jsonPadding != null) {
            this.padding = new NcAnimatePaddingBean(jsonPadding);
        }
    }

    /**
     * Returns the legend title {@link NcAnimateTextBean}.
     * @return the legend title.
     */
    public NcAnimateTextBean getTitle() {
        return this.title;
    }

    /**
     * Returns the legend label {@link NcAnimateTextBean},
     * used to style the legend numbers.
     * @return the legend label.
     */
    public NcAnimateTextBean getLabel() {
        return this.label;
    }

    /**
     * Returns the number of labels to display in the legend.
     * @return the number of labels to display in the legend.
     */
    public Integer getSteps() {
        return this.steps;
    }

    /**
     * Returns the number of digits to display for the numbers in the legend.
     * @return the number of digits to display.
     */
    public Integer getLabelPrecision() {
        return this.labelPrecision;
    }

    /**
     * Each values in the legend are multiplied by this number.
     * Can be used to convert units, or generate prettier legend with small values or large values.
     * @return the label multiplier.
     */
    public Float getLabelMultiplier() {
        return this.labelMultiplier;
    }

    /**
     * Each values in the legend are offset by this number. Can be used to convert units.
     * Note that `labelMultiplier` is applied before `labelOffset`.
     * @return the label offset.
     */
    public Float getLabelOffset() {
        return this.labelOffset;
    }

    /**
     * Prevent the rendering of the lower number in the legend.
     * @return True or False, or null is unset.
     */
    public Boolean getHideLowerLabel() {
        return this.hideLowerLabel;
    }

    /**
     * Prevent the rendering of the higher number in the legend.
     * @return True or False, or null is unset.
     */
    public Boolean getHideHigherLabel() {
        return this.hideHigherLabel;
    }

    /**
     * Returns the {@link NcAnimatePositionBean} defining the
     * position of the legend in relation of the panel it is in.
     * @return the {@link NcAnimatePositionBean} of the legend.
     */
    public NcAnimatePositionBean getPosition() {
        return this.position;
    }

    /**
     * Returns the {@link NcAnimatePaddingBean} defining the
     * padding between the legend background and its content.
     * @return the {@link NcAnimatePaddingBean} of the legend.
     */
    public NcAnimatePaddingBean getPadding() {
        return this.padding;
    }

    /**
     * Returns the background's colour of the legend.
     * @return the background's colour of the legend.
     */
    public String getBackgroundColour() {
        return this.backgroundColour;
    }

    /**
     * Returns the width of the legend colour band, in pixels.
     * @return the width of the legend colour band, in pixels.
     */
    public Integer getColourBandWidth() {
        return this.colourBandWidth;
    }

    /**
     * Returns the height of the legend colour band, in pixels.
     * @return the height of the legend colour band, in pixels.
     */
    public Integer getColourBandHeight() {
        return this.colourBandHeight;
    }

    /**
     * Returns the number of colours used to generate the layer and the legend.
     * @return the number of colours in the colour band.
     */
    public Integer getColourBandColourCount() {
        return this.colourBandColourCount;
    }

    public Float getExtraAmountOutOfRangeLow() {
        return this.extraAmountOutOfRangeLow;
    }

    public Float getExtraAmountOutOfRangeHigh() {
        return this.extraAmountOutOfRangeHigh;
    }

    /**
     * Returns the length of the major tick marks in the legend, in pixels.
     * @return the length of the major tick marks in the legend, in pixels.
     */
    public Integer getMajorTickMarkLength() {
        return this.majorTickMarkLength;
    }

    /**
     * Returns the length of the minor tick marks in the legend, in pixels.
     * @return the length of the minor tick marks in the legend, in pixels.
     */
    public Integer getMinorTickMarkLength() {
        return this.minorTickMarkLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.title != null) {
            json.put("title", this.title.toJSON());
        }
        if (this.label != null) {
            json.put("label", this.label.toJSON());
        }

        json.put("steps", this.steps);
        json.put("labelPrecision", this.labelPrecision);
        json.put("labelMultiplier", this.labelMultiplier);
        json.put("labelOffset", this.labelOffset);

        json.put("hideLowerLabel", this.hideLowerLabel);
        json.put("hideHigherLabel", this.hideHigherLabel);

        if (this.position != null) {
            json.put("position", this.position.toJSON());
        }
        if (this.padding != null) {
            json.put("padding", this.padding.toJSON());
        }
        json.put("backgroundColour", this.backgroundColour);

        json.put("colourBandWidth", this.colourBandWidth);
        json.put("colourBandHeight", this.colourBandHeight);
        json.put("colourBandColourCount", this.colourBandColourCount);

        json.put("extraAmountOutOfRangeLow", this.extraAmountOutOfRangeLow);
        json.put("extraAmountOutOfRangeHigh", this.extraAmountOutOfRangeHigh);

        json.put("majorTickMarkLength", this.majorTickMarkLength);
        json.put("minorTickMarkLength", this.minorTickMarkLength);

        return json.isEmpty() ? null : json;
    }
}
