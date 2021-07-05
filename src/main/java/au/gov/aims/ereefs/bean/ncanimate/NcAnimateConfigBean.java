/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.bean.metadata.TimeIncrementUnit;
import au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderBean;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NcAnimate configuration bean.
 *
 * <p>This is the configuration which describe how to render videos or maps.</p>
 *
 * <p>The configuration can be entirely written in a single configuration file,
 * or split into small parts. When split into parts, the parts can be re-used
 * in other NcAnimate configurations.</p>
 *
 * <p>For example, in the eReefs project, we want all our product to look
 * similar. Therefore, we put the canvas configuration into a
 * {@code default-canvas.json} file. The file can be re-used in every
 * NcAnimate configuration, and its properties can be overwritten if needed.</p>
 *
 * <p>If we want to use the {@code default-canvas.json} file but we want to
 * change the text for the {@code frameDate} text element, we can write
 * the NcAnimate configuration as followed.</p>
 * <pre class="code">
 * {
 *     "canvas": {
 *         "id": "default-canvas",
 *         "texts": {
 *             "frameDate": {
 *                 "text": "${ctx.frameDateFrom dd-MMM-yyyy}"
 *             }
 *         }
 *     }
 * }</pre>
 *
 * <p>See the example bellow to see it in context.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "_id": "products__ncanimate__ereefs__gbr4_v2__current-multi-depth_daily",
 *     "lastModified": "2020-07-28T12:00:00.000+08:00",
 *
 *     "version": "2.0",
 *     "enabled": true,
 *
 *     "regions": [
 *         "queensland"
 *     ],
 *     "canvas": {
 *         "id": "default-canvas",
 *         "texts": {
 *             "frameDate": {
 *                 "text": "${ctx.frameDateFrom dd-MMM-yyyy}"
 *             }
 *         }
 *     },
 *
 *     "defaults": {
 *         "panel": {
 *             "id": "default-panel",
 *             "layers": [
 *                 "ncaggregate_ereefs-model_gbr4-v2_daily",
 *                 "world"
 *             ]
 *         },
 *         "legend": "bottom-left-legend"
 *     },
 *
 *     "panels": [
 *         {
 *             "id": "current-1_5",
 *             "title": { "text": "Current -1.5m" },
 *             "layerOverwrites": {
 *                 "ncaggregate_ereefs-model_gbr4-v2_daily": {
 *                     "targetHeight": -1.5,
 *                     "variable": "ereefs/hydro/current",
 *                     "arrowVariable": "ereefs/hydro/current"
 *                 }
 *             }
 *         }
 *     ],
 *
 *     "render": {
 *         "id": "yearly-videos",
 *         "definitionId": "products__ncanimate__ereefs__gbr4_v2__current-multi-depth"
 *     }
 * }</pre>
 */
public class NcAnimateConfigBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateConfigBean.class);

    private String version;

    private NcAnimateCanvasBean canvas;
    private List<NcAnimatePanelBean> panels;
    private NcAnimateRenderBean render;

    private NcAnimateDefaultsBean defaults;
    private List<String> focusLayers;

    // TODO Change for a Map<String, List<NcAnimateRegionBean>>
    //   Current config would be considered as array of 1 region.
    //   Make the array elements repeats, like "element recycling" in R.
    //   When rendering panels, first panel use first region of the array,
    //   second panel use second region of the array, etc.
    //   That would allow maps to have multiple regions in them,
    //   with coding.
    //   Issues to solve: region placeholders and map selector on Drupal.
    private Map<String, NcAnimateRegionBean> regions;

    private List<Double> targetHeights;

    private boolean enabled;

    // Set during combine parts (see NcAnimateConfigHelper)
    // Get the latest last modified timestamp of all the parts that gets combined to this config.
    private AbstractNcAnimateBean lastModifiedConfigPart;

    // Cache
    private TimeIncrement frameTimeIncrement;

    /**
     * Create a NcAnimate canvas bean part from a {@code JSONWrapperObject}.<p>
     *
     * Allowed attributes:
     * <ul>
     *   <li><em>version</em>: version of the configuration file. Currently {@code 2.0}</li>
     *   <li><em>enabled</em>: set to {@code false} to tell the {@code ereefs-job-planner}
     *       to ignore this configuration; {@code true} otherwise.<br/>
     *       NOTE: this property is ignored by NcAnimate.
     *   </li>
     *   <li><em>regions</em>: list of {@link NcAnimateRegionBean}. NcAnimate generates
     *       products ({@code VIDEO} and {@code MAP}) for each of the listed regions.
     *   </li>
     *   <li><em>targetHeights</em>: list of height (Z dimension in the NetCDF files).
     *       NcAnimate generates products ({@code VIDEO} and {@code MAP}) for each of
     *       the listed height. If a NetCDF file do not support the provided height,
     *       NcAnimate will use the closest height available in the data.
     *   </li>
     *   <li><em>canvas</em>: the {@link NcAnimateCanvasBean} config part used to describe
     *       the style and location of each element.
     *   </li>
     *   <li><em>panels</em>: list of {@link NcAnimatePanelBean} config parts used to
     *       describe what to render in each panel; base layer, data layer, etc.
     *   </li>
     *   <li><em>defaults</em>: default legend style and layers to use in each panel,
     *       used to reuse common panel configuration.
     *   </li>
     *   <li><em>focusLayers</em>: list of NetCDF layer IDs that should be used
     *       to create the list of products ({@code VIDEO} and {@code MAP})
     *       that can be generated. If unspecified, all NetCDF layers are considered.<br/>
     *
     *       This is used to prevent generating unnecessary products.
     *       For example, if we want to generate a product to show NOAA's data,
     *       with a panel showing the equivalent data from the eReefs model,
     *       we can use this property to set the focus on NOAA layers
     *       which will prevent NcAnimate from generating years of videos
     *       for which there is eReefs data but no NOAA data.
     *   </li>
     *   <li><em>render</em>: the {@link NcAnimateRenderBean} config part used to describe
     *       what NcAnimate needs to render: {@code VIDEO} and/or {@code MAP}.
     *   </li>
     * </ul>
     *
     * @param jsonConfig {@code JSONWrapperObject} representing a NcAnimate config bean.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateConfigBean(JSONWrapperObject jsonConfig) throws Exception {
        super(jsonConfig);
    }

    /**
     * Create a NcAnimate configuration bean from an ID and a {@code JSONWrapperObject}.<p>
     *
     * See {@link #NcAnimateConfigBean(JSONWrapperObject)}.
     *
     * @param id the NcAnimate canvas bean part ID.
     * @param jsonConfig {@code JSONWrapperObject} representing a NcAnimate config bean.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateConfigBean(String id, JSONWrapperObject jsonConfig) throws Exception {
        super(new NcAnimateIdBean(null, id), jsonConfig);
    }

    /**
     * Load the attributes of the NcAnimate config bean from a {@code JSONWrapperObject}.
     *
     * @param jsonConfig {@code JSONWrapperObject} representing a NcAnimate config bean.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonConfig) throws Exception {
        this.frameTimeIncrement = null;
        super.parse(jsonConfig);
        if (jsonConfig != null) {
            this.setDefaults(jsonConfig.get(JSONWrapperObject.class, "defaults"));
            this.setFocusLayers(jsonConfig.get(JSONWrapperArray.class, "focusLayers"));

            this.regions = new HashMap<String, NcAnimateRegionBean>();
            Class regionsClass = jsonConfig.getClass("regions");
            if (regionsClass != null) {
                if (JSONWrapperArray.class.equals(regionsClass)) {
                    this.setRegionIds(jsonConfig.get(JSONWrapperArray.class, "regions"));
                } else if (JSONWrapperObject.class.equals(regionsClass)) {
                    this.setRegions(jsonConfig.get(JSONWrapperObject.class, "regions"));
                }
            }
            if (this.regions.isEmpty()) {
                this.regions = null;
            }

            this.setTargetHeights(jsonConfig.get(JSONWrapperArray.class, "targetHeights"));

            this.version = jsonConfig.get(String.class, "version");

            this.setCanvas(jsonConfig.get(JSONWrapperObject.class, "canvas"));
            this.setPanels(jsonConfig.get(JSONWrapperArray.class, "panels"));
            this.setRender(jsonConfig.get(JSONWrapperObject.class, "render"));

            this.setEnabled(jsonConfig.get(Boolean.class, "enabled"));
        }
        this.lastModifiedConfigPart = this;
    }

    private void setDefaults(JSONWrapperObject jsonDefaults) throws Exception {
        this.defaults = null;

        if (jsonDefaults != null) {
            this.defaults = new NcAnimateDefaultsBean(jsonDefaults);
        }
    }

    private void setFocusLayers(JSONWrapperArray jsonFocusLayers) throws InvalidClassException {
        this.focusLayers = new ArrayList<String>();

        if (jsonFocusLayers != null) {
            for (int i=0; i<jsonFocusLayers.length(); i++) {
                String focusLayer = jsonFocusLayers.get(String.class, i);
                if (focusLayer != null && !focusLayer.isEmpty()) {
                    this.focusLayers.add(focusLayer);
                }
            }
        }
    }

    private void setRegionIds(JSONWrapperArray jsonRegions) throws InvalidClassException {
        if (jsonRegions != null) {
            for (int i=0; i<jsonRegions.length(); i++) {
                String regionId = jsonRegions.get(String.class, i, null);
                if (regionId != null && !regionId.isEmpty()) {
                    // The region object will be added using the NcAnimateConfigHelper
                    this.regions.put(regionId, null);
                }
            }
        }
    }

    private void setRegions(JSONWrapperObject jsonRegions) throws Exception {
        if (jsonRegions != null) {
            Set<String> regionIds = jsonRegions.keySet();
            for (String regionId : regionIds) {
                if (regionId != null && !regionId.isEmpty()) {
                    JSONWrapperObject jsonRegion = jsonRegions.get(JSONWrapperObject.class, regionId);
                    NcAnimateRegionBean region = null;
                    if (jsonRegion != null) {
                        region = new NcAnimateRegionBean(jsonRegion);
                    }
                    this.regions.put(regionId, region);
                }
            }
        }
    }

    private void setTargetHeights(JSONWrapperArray jsonTargetHeights) throws InvalidClassException {
        this.targetHeights = new ArrayList<Double>();

        if (jsonTargetHeights != null) {
            for (int i=0; i<jsonTargetHeights.length(); i++) {
                Double targetHeight = jsonTargetHeights.get(Double.class, i);
                if (targetHeight != null) {
                    this.targetHeights.add(targetHeight);
                }
            }
        }
    }

    private void setPanels(JSONWrapperArray jsonPanels) throws Exception {
        this.frameTimeIncrement = null;
        this.panels = new ArrayList<NcAnimatePanelBean>();

        if (jsonPanels != null) {
            for (int i=0; i<jsonPanels.length(); i++) {
                JSONWrapperObject jsonPanel = jsonPanels.get(JSONWrapperObject.class, i);
                if (jsonPanel != null) {
                    this.panels.add(new NcAnimatePanelBean(jsonPanel));
                }
            }
        }
    }

    private void setCanvas(JSONWrapperObject jsonCanvas) throws Exception {
        this.canvas = null;

        if (jsonCanvas != null) {
            this.canvas = new NcAnimateCanvasBean(jsonCanvas);
        }
    }

    private void setRender(JSONWrapperObject jsonRender) throws Exception {
        this.frameTimeIncrement = null;
        this.render = null;

        if (jsonRender != null) {
            this.render = new NcAnimateRenderBean(jsonRender);
        }
    }

    private void setEnabled(Boolean enabled) throws Exception {
        this.enabled = false;

        if (enabled != null) {
            this.enabled = enabled;
        }
    }

    /**
     * Returns the {@code Map} of regions, keyed with region ID.
     * @return the {@code Map} of regions.
     */
    public Map<String, NcAnimateRegionBean> getRegions() {
        return this.regions;
    }

    /**
     * Returns the {@code List} of target height; values for the Z dimension.
     * @return the {@code List} of target height.
     */
    public List<Double> getTargetHeights() {
        return this.targetHeights;
    }

    /**
     * Returns the configuration version number.
     * @return the configuration version number.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Returns the {@link NcAnimateCanvasBean} configuration part.
     * @return the {@link NcAnimateCanvasBean} configuration part.
     */
    public NcAnimateCanvasBean getCanvas() {
        return this.canvas;
    }

    /**
     * Returns the {@code List} of {@link NcAnimatePanelBean} configuration parts.
     * @return the {@code List} of {@link NcAnimatePanelBean} configuration parts.
     */
    public List<NcAnimatePanelBean> getPanels() {
        return this.panels;
    }

    /**
     * Returns the {@link NcAnimateRenderBean} configuration part.
     * @return the {@link NcAnimateRenderBean} configuration part.
     */
    public NcAnimateRenderBean getRender() {
        return this.render;
    }

    /**
     * Returns {@code true} if the {@code NcAnimateConfigBean} is enabled;
     * {@code false} otherwise. This is used with the {@code ereefs-job-planner}
     * and have no effect on NcAnimate.
     * @return {@code true} if the {@code NcAnimateConfigBean} is enabled; {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Returns the most recent configuration parts used with this
     * {@code NcAnimateConfigBean}. This is used to find out which
     * products are outdated and could be used to tell the user
     * which configuration have changed last since the last generation
     * of the product.
     * @return the most recent configuration parts used with this
     *     {@code NcAnimateConfigBean}.
     */
    public AbstractNcAnimateBean getLastModifiedConfigPart() {
        return this.lastModifiedConfigPart;
    }

    /**
     * Internal method used to set the most recent configuration parts
     * used with this {@code NcAnimateConfigBean}. This method is used
     * during the parsing of the configuration files.
     * @param config the most recent configuration parts used with this
     *     {@code NcAnimateConfigBean}.
     */
    public void setLastModifiedConfigPart(AbstractNcAnimateBean config) {
        this.lastModifiedConfigPart = config;
    }

    /**
     * Internal method used to set the most recent configuration parts
     * used with this {@code NcAnimateConfigBean}. This method is used
     * during the parsing of the configuration files.
     * @param config the most recent configuration parts used with this
     *     {@code NcAnimateConfigBean}.
     */
    public void addLastModifiedConfigPart(AbstractNcAnimateBean config) {
        if (config != null && config.getLastModified() > this.lastModifiedConfigPart.getLastModified()) {
            this.setLastModifiedConfigPart(config);
        }
    }

    /**
     * Returns the {@link NcAnimateDefaultsBean} configuration part.
     * @return the {@link NcAnimateDefaultsBean} configuration part.
     */
    public NcAnimateDefaultsBean getDefaults() {
        return this.defaults;
    }

    /**
     * Returns the list of NetCDF layer that should be used to create the list of products to generate.
     * @return the list of focus NetCDF layers.
     */
    public List<String> getFocusLayers() {
        return this.focusLayers;
    }

    /**
     * Used by the {@link NcAnimateConfigHelper}
     *   to apply the default properties to all the panels.<p>
     *
     * NOTE: The "defaults" could stay in the config but it would be
     *   redundant information. It is removed simply to reduce noise.
     *
     * @return the {@link NcAnimateDefaultsBean} configuration part, after been removed.
     */
    public NcAnimateDefaultsBean removeDefaults() {
        NcAnimateDefaultsBean defaults = this.defaults;
        this.defaults = null;
        return defaults;
    }

    /**
     * Returns the period of time represented by each dates in the input data.
     * @return the frame {@link TimeIncrement}.
     */
    public TimeIncrement getFrameTimeIncrement() {
        if (this.frameTimeIncrement == null) {
            TimeIncrement frameTimeIncrement = null;
            String source = "unknown source"; // Variable used for debugging only

            // First, check if it's overwritten in the config
            NcAnimateRenderBean render = this.getRender();
            if (render != null) {
                frameTimeIncrement = render.getFrameTimeIncrement();
                source = String.format("configuration id %s", this.getId().getValue());
            }

            // Look at all input files to find the smallest frame time increment
            // I.E. If we find "1 HOUR", "3 DAYS" and "1 MONTH", "1 HOUR" will be returned.
            if (frameTimeIncrement == null) {
                List<NcAnimatePanelBean> panels = this.getPanels();
                if (panels != null) {
                    for (NcAnimatePanelBean panel : panels) {
                        List<NcAnimateLayerBean> layers = panel.getLayers();
                        if (layers != null) {
                            for (NcAnimateLayerBean layer : layers) {
                                NcAnimateInputBean input = layer.getInput();
                                if (input != null) {
                                    TimeIncrement inputTimeIncrement = input.getTimeIncrement();
                                    if (inputTimeIncrement != null) {
                                        if (frameTimeIncrement == null || inputTimeIncrement.compareTo(frameTimeIncrement) < 0) {
                                            source = String.format("definition id %s", input.getId().getValue());
                                            frameTimeIncrement = inputTimeIncrement;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (frameTimeIncrement == null) {
                // This should not happen
                frameTimeIncrement = new TimeIncrement(1, TimeIncrementUnit.HOUR);
                source = "fallback";
                LOGGER.error(String.format("No frame time increment found in input files. Assuming %s", frameTimeIncrement));
            }

            LOGGER.debug(String.format("Frame time increment for %s is %s, found from source: %s",
                    this.getId().getValue(),
                    frameTimeIncrement,
                    source));

            this.frameTimeIncrement = frameTimeIncrement;
        }

        return this.frameTimeIncrement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.regions != null && !this.regions.isEmpty()) {
            JSONObject jsonRegions = new JSONObject();
            for (Map.Entry<String, NcAnimateRegionBean> regionEntry : this.regions.entrySet()) {
                NcAnimateRegionBean region = regionEntry.getValue();
                jsonRegions.put(regionEntry.getKey(),
                    region == null ? JSONObject.NULL : region.toJSON());
            }
            json.put("regions", jsonRegions);
        }

        json.put("targetHeights", this.targetHeights);

        if (this.defaults != null) {
            json.put("defaults", this.defaults.toJSON());
        }

        json.put("focusLayers", this.focusLayers);

        json.put("version", this.version);
        json.put("latestLastModifiedConfigId", "" + this.lastModifiedConfigPart.getId());
        json.put("latestLastModified", "" + this.lastModifiedConfigPart.getLastModified());

        if (this.canvas != null) {
            json.put("canvas", this.canvas.toJSON());
        }

        if (this.panels != null && !this.panels.isEmpty()) {
            JSONArray jsonPanels = new JSONArray();
            for (NcAnimatePanelBean panel : this.panels) {
                if (panel != null) {
                    jsonPanels.put(panel.toJSON());
                }
            }
            if (!jsonPanels.isEmpty()) {
                json.put("panels", jsonPanels);
            }
        }

        if (this.render != null) {
            json.put("render", this.render.toJSON());
        }

        return json.isEmpty() ? null : json;
    }
}
