/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * NcAnimate {@code NetCDF} <em>true colour</em> variable bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateLayerBean}.</p>
 *
 * <p>This is a very specific type of variable used to define a
 * {@code NetCDF} <em>true colour</em> variable.
 * The variable is rendered by combining multiple wavelength variables
 * together. Each wavelength variable is rendered to a specific colour
 * palette defined as {@code hexColours}.</p>
 *
 * <p>Example of 3 true colour variables, in context:</p>
 * <pre class="code">
 * {
 *     "R_470": {
 *         "variableId": "R_470",
 *         "hexColours": [
 *             "#000001",
 *             "#00005e",
 *             "#000091",
 *             "#0000ae",
 *             "#0000c3",
 *             "#0000d5",
 *             "#0000e0",
 *             "#0000eb",
 *             "#0000f3",
 *             "#0000f9",
 *             "#0000ff"
 *         ],
 *         "scaleMin": 0,
 *         "scaleMax": 0.1
 *     },
 *     "R_555": {
 *         "variableId": "R_555",
 *         "hexColours": [
 *             "#000100",
 *             "#005e00",
 *             "#009100",
 *             "#00ae00",
 *             "#00c300",
 *             "#00d500",
 *             "#00e000",
 *             "#00eb00",
 *             "#00f300",
 *             "#00f900",
 *             "#00ff00"
 *         ],
 *         "scaleMin": 0,
 *         "scaleMax": 0.1
 *     },
 *     "R_645": {
 *         "variableId": "R_645",
 *         "hexColours": [
 *             "#010000",
 *             "#5e0000",
 *             "#910000",
 *             "#ae0000",
 *             "#c30000",
 *             "#d50000",
 *             "#e00000",
 *             "#eb0000",
 *             "#f30000",
 *             "#f90000",
 *             "#ff0000"
 *         ],
 *         "scaleMin": 0,
 *         "scaleMax": 0.1
 *     }
 * }</pre>
 */
public class NcAnimateNetCDFTrueColourVariableBean extends NcAnimateNetCDFVariableBean {
    private List<String> hexColours;

    /**
     * Create a NcAnimate NetCDF true colour variable bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>hexColours</em>: list of hexadecimal colours to use as a colour palette.</li>
     * </ul>
     *
     * <p>Allowed attributes inherited from {@link NcAnimateNetCDFVariableBean}:</p>
     * <ul>
     *   <li><em>variableId</em>: the ID of the variable, as defined in the NetCDF file.</li>
     *   <li><em>scaleMax</em>: expected maximum value in the data.</li>
     *   <li><em>scaleMin</em>: expected minimum value in the data.</li>
     * </ul>
     *
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF true colour variable bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateNetCDFTrueColourVariableBean(JSONWrapperObject jsonVariable) throws Exception {
        super(jsonVariable);
    }

    /**
     * Create a NcAnimate NetCDF true colour variable bean part from an ID and a {@code JSONWrapperObject}.<p>
     *
     * See {@link #NcAnimateNetCDFTrueColourVariableBean(JSONWrapperObject)}.
     *
     * @param id the NcAnimate NetCDF true colour variable bean part ID.
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF true colour variable bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateNetCDFTrueColourVariableBean(String id, JSONWrapperObject jsonVariable) throws Exception {
        super(id, jsonVariable);
    }

    /**
     * Load the attributes of the NcAnimate NetCDF true colour variable bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonVariable {@code JSONWrapperObject} representing a NcAnimate NetCDF true colour variable bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonVariable) throws Exception {
        super.parse(jsonVariable);
        if (jsonVariable != null) {
            this.setHexColours(jsonVariable.get(JSONWrapperArray.class, "hexColours"));
        }
    }

    private void setHexColours(JSONWrapperArray jsonHexColours) throws InvalidClassException {
        this.hexColours = null;

        if (jsonHexColours != null) {
            this.hexColours = new ArrayList<String>();

            for (int i=0; i<jsonHexColours.length(); i++) {
                this.hexColours.add(jsonHexColours.get(String.class, i));
            }
        }
    }

    /**
     * Returns the colour palette as a list of hexadecimal colours.
     * @return the colour palette as a list of hexadecimal colours.
     */
    public List<String> getHexColours() {
        return this.hexColours;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("hexColours", this.hexColours);

        return json.isEmpty() ? null : json;
    }
}
