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
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * NcAnimate layer bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimatePanelBean}.</p>
 *
 * <p>Used to define a layer overlay to be rendered in a panel.</p>
 *
 * <p>Example for a {@code NetCDF} layer:</p>
 * <pre class="code">
 * {
 *     "type": "NETCDF",
 *     "input": "ereefs_gbr4_v2",
 *     "arrowSize": 10
 *     "variable": "ereefs/hydro/wind",
 * }</pre>
 *
 * <p>Example for a {@code NetCDF} layer with different layer for arrows:</p>
 * <pre class="code">
 * {
 *     "type": "NETCDF",
 *     "input": "ereefs__gbr1_2-0",
 *     "arrowSize": 10,
 *     "variable": "ereefs/hydro/wind",
 *     "arrowVariable": "ereefs/hydro/wind"
 * }</pre>
 *
 * <p>NOTE: If the {@code arrowVariable} is not specified and the variable is a vector variable,
 *     NcAnimate will render the variable with arrows from the same variable on top,
 *     just like the visualisation from a {@code THREDDS} server.
 *     If the {@code arrowVariable} is specified, NcAnimate will render the arrows using
 *     {@code DynamicArrowLayer}, where the length of the arrow changed depending on the
 *     amplitude.</p>
 *
 * <p>Example for a {@code GEOJSON} layer:</p>
 * <pre class="code">
 * {
 *     "type": "GEOJSON"
 *     "datasource": "s3://my-bucket/layers/AU_GA_GEODATA-TOPO-5M_aus5fgd_r.geojson",
 *     "style": "s3://my-bucket/styles/AU_GA_GEODATA-TOPO-5M_aus5fgd_r.sld",
 * }</pre>
 *
 * <p>Example for a {@code CSV} layer:</p>
 * <pre class="code">
 * {
 *     "type": "CSV",
 *     "datasource": "s3://my-bucket/layers/World_NE_10m-cities_V3_Ranked_NRM.csv",
 *     "style": "s3://my-bucket/styles/World_NE_10m-cities_V3_Ranked_${ctx.region.id}.sld",
 *     "latitudeColumn": "LATITUDE",
 *     "longitudeColumn": "LONGITUDE"
 * }</pre>
 *
 * <p>NOTE: First row of the CSV must be use for column names.</p>
 */
public class NcAnimateLayerBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateLayerBean.class);

    /**
     * NOTE: This class contains all attributes which can be found in any
     *   layer type. We could create an abstract NcAnimateLayerBean with
     *   specific class for each layer type, and create a Factory to create
     *   the proper layer type, but that strategy would not permit the
     *   layer overwrites type specific attributes.
     */

    // All types
    private LayerType type;

    // CSV and GeoJSON
    // NOTE: Those attributes can't be URI because their value may contains placeholders which use invalid URI characters.
    //   Example: s3://ncanimate/styles/World_NE_10m-cities_V3_Ranked_{$region.id}.sld
    private String datasource;
    private String style;

    // CSV
    private String latitudeColumn;
    private String longitudeColumn;

    // NetCDF or GRIB2
    private NcAnimateInputBean input;
    private NcAnimateNetCDFVariableBean variable;
    private NcAnimateNetCDFVariableBean arrowVariable;
    private String targetHeight; // Can be a double ("-1.5") or a place holder ("{$height}")
    private Integer arrowSize;
    // Key: variableId
    private Map<String, NcAnimateNetCDFTrueColourVariableBean> trueColourVariables;

    // WMS
    private String server; // https://maps.eatlas.org.au/maps/ows
    private String layerName; // ea:basemap
    private String styleName; // nolabel

    /**
     * Create a NcAnimate layer bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes for GeoJSON:</p>
     * <ul>
     *   <li><em>type</em>: the layer type. Set to {@code GEOJSON}.</li>
     *   <li><em>datasource</em>: the location of the GeoJSON file.
     *       Supports protocols {@code s3://} and {@code file://}.
     *   </li>
     *   <li><em>style</em>: the location of the
     *       <a href="https://docs.geoserver.org/stable/en/user/styling/sld" target="_blank">SLD</a>
     *       style file. Supports protocols {@code s3://} and {@code file://}.
     *   </li>
     * </ul>
     *
     * <p>Allowed attributes for CSV:</p>
     * <ul>
     *   <li><em>type</em>: the layer type. Set to {@code CSV}.</li>
     *   <li><em>datasource</em>: the location of the CSV file.
     *       Supports protocols {@code s3://} and {@code file://}.
     *   </li>
     *   <li><em>style</em>: the location of the
     *       <a href="https://docs.geoserver.org/stable/en/user/styling/sld" target="_blank">SLD</a>
     *       style file. Supports protocols {@code s3://} and {@code file://}.
     *   </li>
     *   <li><em>latitudeColumn</em>: name of the column containing latitude coordinates.</li>
     *   <li><em>longitudeColumn</em>: name of the column containing longitude coordinates.</li>
     * </ul>
     *
     * <p>Allowed attributes for NetCDF and GRIB2:</p>
     * <ul>
     *   <li><em>type</em>: the layer type. Set to {@code NETCDF} or {@code GRIB2}.</li>
     *   <li><em>input</em>: the {@link NcAnimateInputBean} ID or {@code JSONWrapperObject}.</li>
     *   <li><em>arrowSize</em>: size of the arrow. Used with {@code DynamicArrowLayer}. Default: 20.</li>
     *   <li><em>variable</em>: the {@link NcAnimateNetCDFVariableBean} ID or {@code JSONWrapperObject}
     *       of the data to render.</li>
     *   <li><em>arrowVariable</em>: the {@link NcAnimateNetCDFVariableBean} ID or {@code JSONWrapperObject}
     *       of the data to use to render an arrow layer on top of the data. Optional.</li>
     *   <li><em>targetHeight</em>: usually set in the {@link NcAnimateConfigBean} using overwrites.
     *       Use value {@code ${ctx.targetHeight}} to set it to the current target height.
     *   </li>
     * </ul>
     *
     * <p>Allowed attributes for NetCDF true colour variable:</p>
     * <ul>
     *   <li><em>type</em>: the layer type. Set to {@code NETCDF}.</li>
     *   <li><em>input</em>: the {@link NcAnimateInputBean} ID or {@code JSONWrapperObject}.</li>
     *   <li><em>trueColourVariables</em>: the {@link NcAnimateNetCDFTrueColourVariableBean}
     *       ID or {@code JSONWrapperObject} of the true variable data to render.</li>
     *   <li><em>targetHeight</em>: usually set in the {@link NcAnimateConfigBean} using overwrites.
     *       Use value {@code ${ctx.targetHeight}} to set it to the current target height.
     *   </li>
     * </ul>
     *
     * <p>Allowed attributes for WMS:</p>
     * <ul>
     *   <li><em>type</em>: the layer type. Set to {@code WMS}.</li>
     *   <li><em>server</em>: the WMS server URL.</li>
     *   <li><em>layerName</em>: the name (aka ID) of the WMS layer.</li>
     *   <li><em>styleName</em>: the name of the style to render the WMS layer. Optional.</li>
     * </ul>
     *
     * @param jsonLayer {@code JSONWrapperObject} representing a NcAnimate layer bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateLayerBean(JSONWrapperObject jsonLayer) throws Exception {
        super(jsonLayer);
    }

    /**
     * Create a NcAnimate layer bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateLayerBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate layer bean part ID.
     * @param jsonLayer {@code JSONWrapperObject} representing a NcAnimate layer bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateLayerBean(String id, JSONWrapperObject jsonLayer) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.LAYER, id), jsonLayer);
    }

    /**
     * Load the attributes of the NcAnimate layer bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonLayer {@code JSONWrapperObject} representing a NcAnimate layer bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonLayer) throws Exception {
        super.parse(jsonLayer);
        if (jsonLayer != null) {
            this.setType(jsonLayer.get(String.class, "type"));

            // CSV and GeoJSON
            this.datasource = jsonLayer.get(String.class, "datasource");
            this.style = jsonLayer.get(String.class, "style");

            // CSV
            this.latitudeColumn = jsonLayer.get(String.class, "latitudeColumn");
            this.longitudeColumn = jsonLayer.get(String.class, "longitudeColumn");

            // NetCDF
            this.targetHeight = jsonLayer.get(String.class, "targetHeight");
            this.arrowSize = jsonLayer.get(Integer.class, "arrowSize");

            this.input = null;
            Class inputClass = jsonLayer.getClass("input");
            if (inputClass != null) {
                if (String.class.equals(inputClass)) {
                    this.setInput(jsonLayer.get(String.class, "input"));
                } else if (JSONWrapperObject.class.equals(inputClass)) {
                    this.setInput(jsonLayer.get(JSONWrapperObject.class, "input"));
                } else {
                    LOGGER.error(String.format("Invalid layer NetCDF input config. Expected input ID (String) or input configuration (JSONObject). Found %s.%n%s",
                        inputClass.getName(), jsonLayer));
                }
            }

            this.variable = null;
            Class variableClass = jsonLayer.getClass("variable");
            if (variableClass != null) {
                if (String.class.equals(variableClass)) {
                    this.setVariable(jsonLayer.get(String.class, "variable"));
                } else if (JSONWrapperObject.class.equals(variableClass)) {
                    this.setVariable(jsonLayer.get(JSONWrapperObject.class, "variable"));
                } else {
                    LOGGER.error(String.format("Invalid layer NetCDF variable config. Expected variable ID (String) or variable configuration (JSONObject). Found %s.%n%s",
                        variableClass.getName(), jsonLayer));
                }
            }

            this.arrowVariable = null;
            Class arrowVariableClass = jsonLayer.getClass("arrowVariable");
            if (arrowVariableClass != null) {
                if (String.class.equals(arrowVariableClass)) {
                    this.setArrowVariable(jsonLayer.get(String.class, "arrowVariable"));
                } else if (JSONWrapperObject.class.equals(arrowVariableClass)) {
                    this.setArrowVariable(jsonLayer.get(JSONWrapperObject.class, "arrowVariable"));
                } else {
                    LOGGER.error(String.format("Invalid layer NetCDF arrow variable config. Expected variable ID (String) or variable configuration (JSONObject). Found %s.%n%s",
                        arrowVariableClass.getName(), jsonLayer));
                }
            }

            this.trueColourVariables = null;
            Class trueColourVariablesClass = jsonLayer.getClass("trueColourVariables");
            if (trueColourVariablesClass != null) {
                if (JSONWrapperArray.class.equals(trueColourVariablesClass)) {
                    this.setTrueColourVariables(jsonLayer.get(JSONWrapperArray.class, "trueColourVariables"));
                } else if (JSONWrapperObject.class.equals(trueColourVariablesClass)) {
                    this.setTrueColourVariables(jsonLayer.get(JSONWrapperObject.class, "trueColourVariables"));
                } else {
                    LOGGER.error(String.format("Invalid layer NetCDF trueColourVariables config. Expected array if variable ID (String) or map (JSONObject) of variable configuration (JSONObject). Found %s.%n%s",
                        trueColourVariablesClass.getName(), jsonLayer));
                }
            }

            // WMS
            this.server = jsonLayer.get(String.class, "server");
            this.layerName = jsonLayer.get(String.class, "layerName");
            this.styleName = jsonLayer.get(String.class, "styleName");
        }
    }

    private void setInput(JSONWrapperObject jsonInput) throws Exception {
        this.input = null;

        if (jsonInput != null) {
            this.input = new NcAnimateInputBean(jsonInput);
        }
    }

    private void setInput(String inputId) throws Exception {
        this.input = null;

        if (inputId != null) {
            this.input = new NcAnimateInputBean(inputId, null);
        }
    }

    private void setType(String type) {
        this.type = null;
        if (type != null && !type.isEmpty()) {
            this.type = LayerType.valueOf(type.toUpperCase());
        }
    }

    private void setVariable(JSONWrapperObject jsonVariable) throws Exception {
        this.variable = null;

        if (jsonVariable != null) {
            this.variable = new NcAnimateNetCDFVariableBean(jsonVariable);
        }
    }

    private void setVariable(String variableId) throws Exception {
        this.variable = null;

        if (variableId != null) {
            this.variable = new NcAnimateNetCDFVariableBean(variableId, null);
        }
    }

    private void setArrowVariable(JSONWrapperObject jsonArrowVariable) throws Exception {
        this.arrowVariable = null;

        if (jsonArrowVariable != null) {
            this.arrowVariable = new NcAnimateNetCDFVariableBean(jsonArrowVariable);
        }
    }

    private void setArrowVariable(String arrowVariableId) throws Exception {
        this.arrowVariable = null;

        if (arrowVariableId != null) {
            this.arrowVariable = new NcAnimateNetCDFVariableBean(arrowVariableId, null);
        }
    }

    private void setTrueColourVariables(JSONWrapperArray jsonTrueColourVariables) throws Exception {
        this.trueColourVariables = null;

        if (jsonTrueColourVariables != null && jsonTrueColourVariables.length() > 0) {
            this.trueColourVariables = new HashMap<String, NcAnimateNetCDFTrueColourVariableBean>();

            for (int i=0; i<jsonTrueColourVariables.length(); i++) {
                String trueColourVariableId = jsonTrueColourVariables.get(String.class, i);
                NcAnimateNetCDFTrueColourVariableBean trueColourVariable = new NcAnimateNetCDFTrueColourVariableBean(trueColourVariableId, null);
                this.trueColourVariables.put(trueColourVariableId, trueColourVariable);
            }
        }
    }

    private void setTrueColourVariables(JSONWrapperObject jsonTrueColourVariables) throws Exception {
        this.trueColourVariables = null;

        if (jsonTrueColourVariables != null && jsonTrueColourVariables.length() > 0) {
            this.trueColourVariables = new HashMap<String, NcAnimateNetCDFTrueColourVariableBean>();

            for (String trueColourVariableId : jsonTrueColourVariables.keySet()) {
                JSONWrapperObject jsonTrueColourVariable = jsonTrueColourVariables.get(JSONWrapperObject.class, trueColourVariableId);
                NcAnimateNetCDFTrueColourVariableBean trueColourVariable = new NcAnimateNetCDFTrueColourVariableBean(trueColourVariableId, jsonTrueColourVariable);
                this.trueColourVariables.put(trueColourVariableId, trueColourVariable);
            }
        }
    }

    /**
     * Returns the {@link LayerType}.
     * @return the {@link LayerType}.
     */
    public LayerType getType() {
        return this.type;
    }

    /**
     * Returns the datasource file URI.
     * Used with {@link LayerType#GEOJSON} and {@link LayerType#CSV}.
     * @return the datasource URI.
     */
    public String getDatasource() {
        return this.datasource;
    }

    /**
     * Returns the layer
     * <a href="https://docs.geoserver.org/stable/en/user/styling/sld" target="_blank">SLD</a>
     * style URI.
     * Used with {@link LayerType#GEOJSON} and {@link LayerType#CSV}.
     * @return the layer {@code SLD} style URI.
     */
    public String getStyle() {
        return this.style;
    }

    /**
     * Returns the name of the column to use for latitude coordinates.
     * Used with {@link LayerType#CSV}.
     * @return the name of the column to use for latitude coordinates.
     */
    public String getLatitudeColumn() {
        return this.latitudeColumn;
    }

    /**
     * Returns the name of the column to use for longitude coordinates.
     * Used with {@link LayerType#CSV}.
     * @return the name of the column to use for longitude coordinates.
     */
    public String getLongitudeColumn() {
        return this.longitudeColumn;
    }

    /**
     * Returns the {@link NcAnimateInputBean}.
     * Used with {@link LayerType#NETCDF} and {@link LayerType#GRIB2}.
     * @return the {@link NcAnimateInputBean}.
     */
    public NcAnimateInputBean getInput() {
        return this.input;
    }

    /**
     * Returns the data variable {@link NcAnimateNetCDFVariableBean}.
     * Used with {@link LayerType#NETCDF} and {@link LayerType#GRIB2}.
     * @return the data variable {@link NcAnimateNetCDFVariableBean}.
     */
    public NcAnimateNetCDFVariableBean getVariable() {
        return this.variable;
    }

    /**
     * Returns the arrow variable {@link NcAnimateNetCDFVariableBean}.
     * Used with {@link LayerType#NETCDF} and {@link LayerType#GRIB2}.
     * @return the arrow variable {@link NcAnimateNetCDFVariableBean}.
     */
    public NcAnimateNetCDFVariableBean getArrowVariable() {
        return this.arrowVariable;
    }

    /**
     * Returns the current target height this layer should render.
     * Used with {@link LayerType#NETCDF} and {@link LayerType#GRIB2}.
     * @return the current target height this layer should render.
     */
    public String getTargetHeight() {
        return this.targetHeight;
    }

    /**
     * Returns the arrow size.
     * Used with {@link LayerType#NETCDF} and {@link LayerType#GRIB2}.
     * @return the arrow size.
     */
    public Integer getArrowSize() {
        return this.arrowSize;
    }

    /**
     * Returns the {@code Map} of {@link NcAnimateNetCDFVariableBean}
     * used to render a true colour layer.
     * Used with {@link LayerType#NETCDF}.
     * @return the {@code Map} of true colour variables.
     */
    public Map<String, NcAnimateNetCDFTrueColourVariableBean> getTrueColourVariables() {
        return this.trueColourVariables;
    }

    /**
     * Returns the WMS server URL.
     * Used with {@link LayerType#WMS}.
     * @return the WMS server URL.
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Returns the WMS layer name, or layer ID, as presented in the
     * WMS GetCapabilities document provided by the WMS server.
     * Used with {@link LayerType#WMS}.
     * @return the WMS layer name.
     */
    public String getLayerName() {
        return this.layerName;
    }

    /**
     * Returns the WMS style name as presented in the
     * WMS GetCapabilities document provided by the WMS server.
     * Used with {@link LayerType#WMS}.
     * @return the WMS style name.
     */
    public String getStyleName() {
        return this.styleName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("type", this.type == null ? null : this.type.name());

        // CSV and GeoJSON
        json.put("datasource", this.datasource);
        json.put("style", this.style);

        // CSV
        json.put("latitudeColumn", this.latitudeColumn);
        json.put("longitudeColumn", this.longitudeColumn);

        // NetCDF
        json.put("targetHeight", this.targetHeight);
        json.put("arrowSize", this.arrowSize);
        if (this.input != null) {
            json.put("input", this.input.toJSON());
        }
        if (this.variable != null) {
            json.put("variable", this.variable.toJSON());
        }
        if (this.arrowVariable != null) {
            json.put("arrowVariable", this.arrowVariable.toJSON());
        }

        if (this.trueColourVariables != null && !this.trueColourVariables.isEmpty()) {
            JSONObject jsonTrueColourVariables = new JSONObject();
            for (Map.Entry<String, NcAnimateNetCDFTrueColourVariableBean> trueColourVariableEntry : this.trueColourVariables.entrySet()) {
                NcAnimateNetCDFTrueColourVariableBean trueColourVariable = trueColourVariableEntry.getValue();
                if (trueColourVariable != null) {
                    jsonTrueColourVariables.put(
                        trueColourVariableEntry.getKey(),
                        trueColourVariable.toJSON()
                    );
                }
            }
            json.put("trueColourVariables", jsonTrueColourVariables);
        }

        // WMS
        json.put("server", this.server);
        json.put("layerName", this.layerName);
        json.put("styleName", this.styleName);

        return json.isEmpty() ? null : json;
    }

    /**
     * Type of layers supported by NcAnimate.
     */
    public enum LayerType {
        NETCDF, GRIB2, GEOJSON, CSV, WMS
    }
}
