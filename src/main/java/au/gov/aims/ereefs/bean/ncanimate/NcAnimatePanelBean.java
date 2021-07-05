/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NcAnimate panel bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimatePanelBean}
 * and in {@link NcAnimateDefaultsBean}.</p>
 *
 * <p>Panels are geographical maps rendered on the canvas.
 * Each panel contains a list of layers, texts elements and
 * style properties.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "borderColour": "#B3B3B3",
 *     "borderWidth": 2,
 *     "width": 452,
 *     "backgroundColour": "#F4FDFD",
 *     "title": {
 *         "text": "Temperature ${ctx.targetHeight %.1f}m",
 *         "position": {
 *             "top": 10
 *         },
 *         "fontSize": 30,
 *         "fontColour": "#000000"
 *     },
 *
 *     "texts": {
 *         "panelVariableIds": {
 *             "text": "Variable ID: ${ctx.panel.variableIds}",
 *             "fontSize": 10,
 *             "fontColour": "#666666",
 *             "position": {
 *                 "bottom": -16,
 *                 "right": 0
 *             }
 *         }
 *     },
 *
 *     "layers": [
 *         "ereefs-model_gbr4-v2": {
 *             "targetHeight": "${ctx.targetHeight}",
 *             "variable": "ereefs/hydro/temp"
 *         }
 *         "world"
 *     ]
 * }</pre>
 */
public class NcAnimatePanelBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimatePanelBean.class);

    private Map<String, NcAnimateTextBean> texts;

    private NcAnimateTextBean title;
    private List<NcAnimateLayerBean> layers;
    private Map<String, NcAnimateLayerBean> layerOverwrites;

    private Integer mapScale;
    private String description;

    // Style

    private Integer width;
    // NOTE: height is calculated in proportion of current region

    private Integer borderWidth;
    private String borderColour;

    private String backgroundColour;

    /**
     * Create a NcAnimate panel bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>texts</em>: collection of {@link NcAnimateTextBean} to render on or around the panel.</li>
     *   <li><em>title</em>: {@link NcAnimateTextBean} defining the panel's title.</li>
     *   <li><em>layers</em>: list of {@link NcAnimateLayerBean} to render on the panel, listed in rendered order.</li>
     *   <li><em>layerOverwrites</em>: collection of layer overwrites.</li>
     *   <li><em>mapScale</em>: scale of the map, used with SLD style which are scale dependent.</li>
     *   <li><em>description</em>: panel description. Unused.</li>
     *   <li><em>width</em>: panel width, in pixel.</li>
     *   <li><em>borderWidth</em>: panel border width, in pixel.</li>
     *   <li><em>borderColour</em>: panel border colour, in hexadecimal.</li>
     *   <li><em>backgroundColour</em>: panel background colour, in hexadecimal.</li>
     * </ul>
     *
     * @param jsonPanel {@code JSONWrapperObject} representing a NcAnimate panel bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePanelBean(JSONWrapperObject jsonPanel) throws Exception {
        super(jsonPanel);
    }

    /**
     * Create a NcAnimate panel bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimatePanelBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate panel bean part ID.
     * @param jsonPanel {@code JSONWrapperObject} representing a NcAnimate panel bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePanelBean(String id, JSONWrapperObject jsonPanel) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.PANEL, id), jsonPanel);
    }

    /**
     * Load the attributes of the NcAnimate panel bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonPanel {@code JSONWrapperObject} representing a NcAnimate panel bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonPanel) throws Exception {
        super.parse(jsonPanel);
        if (jsonPanel != null) {
            this.setTexts(jsonPanel.get(JSONWrapperObject.class, "texts"));

            this.setTitle(jsonPanel.get(JSONWrapperObject.class, "title"));
            this.setLayers(jsonPanel.get(JSONWrapperArray.class, "layers"));
            this.setLayerOverwrites(jsonPanel.get(JSONWrapperObject.class, "layerOverwrites"));

            this.mapScale = jsonPanel.get(Integer.class, "mapScale");
            this.description = jsonPanel.get(String.class, "description");

            // Style
            this.width = jsonPanel.get(Integer.class, "width");

            this.borderWidth = jsonPanel.get(Integer.class, "borderWidth");
            this.borderColour = jsonPanel.get(String.class, "borderColour");

            this.backgroundColour = jsonPanel.get(String.class, "backgroundColour");
        }
    }

    private void setTexts(JSONWrapperObject jsonTexts) throws Exception {
        this.texts = new HashMap<String, NcAnimateTextBean>();

        if (jsonTexts != null) {
            Set<String> textIds = jsonTexts.keySet();
            for (String textId : textIds) {
                this.texts.put(textId, new NcAnimateTextBean(textId, jsonTexts.get(JSONWrapperObject.class, textId)));
            }
        }
    }

    private void setTitle(JSONWrapperObject jsonTitle) throws Exception {
        this.title = null;

        if (jsonTitle != null) {
            this.title = new NcAnimateTextBean(jsonTitle);
        }
    }

    private void setLayers(JSONWrapperArray jsonLayers) throws Exception {
        this.layers = new ArrayList<NcAnimateLayerBean>();

        if (jsonLayers != null) {
            for (int i=0; i<jsonLayers.length(); i++) {
                Class layerClass = jsonLayers.getClass(i);
                if (String.class.equals(layerClass)) {
                    String layerId = jsonLayers.get(String.class, i);
                    this.layers.add(new NcAnimateLayerBean(layerId, null));
                } else if (JSONWrapperObject.class.equals(layerClass)) {
                    JSONWrapperObject jsonLayer = jsonLayers.get(JSONWrapperObject.class, i);
                    if (jsonLayer != null) {
                        this.layers.add(new NcAnimateLayerBean(jsonLayer));
                    }
                } else {
                    LOGGER.error(String.format("Invalid layer configuration. Expected layer ID (String) or layer configuration (JSONObject). Found %s.%n%s",
                        layerClass.getName(), jsonLayers));
                }
            }
        }
    }

    private void setLayerOverwrites(JSONWrapperObject jsonLayerOverwrites) throws Exception {
        this.layerOverwrites = new HashMap<String, NcAnimateLayerBean>();

        if (jsonLayerOverwrites != null) {
            Set<String> layerIds = jsonLayerOverwrites.keySet();
            for (String layerId : layerIds) {
                if (layerId != null) {
                    this.layerOverwrites.put(layerId, new NcAnimateLayerBean(layerId, jsonLayerOverwrites.get(JSONWrapperObject.class, layerId)));
                }
            }
        }
    }

    /**
     * Returns the panel's {@code Map} of {@link NcAnimateTextBean} to render on or around the panel.
     * @return the panel's {@code Map} of {@link NcAnimateTextBean}.
     */
    public Map<String, NcAnimateTextBean> getTexts() {
        return this.texts;
    }

    /**
     * Returns the title's {@link NcAnimateTextBean}.
     * @return the title's {@link NcAnimateTextBean}.
     */
    public NcAnimateTextBean getTitle() {
        return this.title;
    }

    /**
     * Returns the list of layers to render on the panel, in rendering order.
     * @return the list of layers to render on the panel.
     */
    public List<NcAnimateLayerBean> getLayers() {
        return this.layers;
    }

    /**
     * Returns the {@code Map} of layer overwrites.
     * @return the {@code Map} of layer overwrites.
     */
    public Map<String, NcAnimateLayerBean> getLayerOverwrites() {
        return this.layerOverwrites;
    }

    /**
     * Returns the map scale.
     * @return the map scale.
     */
    public Integer getMapScale() {
        return this.mapScale;
    }

    /**
     * Returns the panel's description.
     * @return the panel's description.
     */
    public String getDescription() {
        return this.description;
    }

    // Style

    /**
     * Returns the panel width, in pixel.
     * @return the panel width.
     */
    public Integer getWidth() {
        return this.width;
    }

    /**
     * Returns the panel's border width, in pixel.
     * @return the panel's border width.
     */
    public Integer getBorderWidth() {
        return this.borderWidth;
    }

    /**
     * Returns the panel's border colour, in hexadecimal.
     * @return the panel's border colour.
     */
    public String getBorderColour() {
        return this.borderColour;
    }

    /**
     * Returns the panel's background colour, in hexadecimal.
     * @return the panel's background colour.
     */
    public String getBackgroundColour() {
        return this.backgroundColour;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.texts != null && !this.texts.isEmpty()) {
            JSONObject jsonTexts = new JSONObject();
            for (Map.Entry<String, NcAnimateTextBean> textEntry : this.texts.entrySet()) {
                NcAnimateTextBean text = textEntry.getValue();
                jsonTexts.put(textEntry.getKey(),
                    text == null ? JSONObject.NULL : text.toJSON());
            }
            json.put("texts", jsonTexts);
        }

        json.put("mapScale", this.mapScale);
        json.put("description", this.description);

        // Style

        json.put("width", this.width);
        json.put("borderWidth", this.borderWidth);
        json.put("borderColour", this.borderColour);
        json.put("backgroundColour", this.backgroundColour);

        if (this.title != null) {
            json.put("title", this.title.toJSON());
        }

        if (this.layers != null && !this.layers.isEmpty()) {
            JSONArray jsonLayers = new JSONArray();
            for (NcAnimateLayerBean layer : this.layers) {
                jsonLayers.put(layer.toJSON());
            }
            json.put("layers", jsonLayers);
        }

        if (this.layerOverwrites != null && !this.layerOverwrites.isEmpty()) {
            JSONObject jsonLayerOverwrites = new JSONObject();
            for (Map.Entry<String, NcAnimateLayerBean> layerOverwriteEntry : this.layerOverwrites.entrySet()) {
                NcAnimateLayerBean layerOverwrite = layerOverwriteEntry.getValue();
                jsonLayerOverwrites.put(layerOverwriteEntry.getKey(),
                    layerOverwrite == null ? JSONObject.NULL : layerOverwrite.toJSON());
            }
            json.put("layerOverwrites", jsonLayerOverwrites);
        }

        return json.isEmpty() ? null : json;
    }
}
