/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * NcAnimate default bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateConfigBean}.</p>
 *
 * <p>It's used to define properties used in every panels, to reduce repetition.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "panel": {
 *         "id": "default-panel",
 *         "layers": [
 *             "ereefs-model_gbr4-v2",
 *             "world"
 *         ]
 *     },
 *     "legend": "bottom-left-legend"
 * }</pre>
 */
public class NcAnimateDefaultsBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateDefaultsBean.class);

    private NcAnimatePanelBean panel;
    private NcAnimateLegendBean legend;

    /**
     * Create a NcAnimate defaults bean part from a {@code JSONWrapperObject},
     * to be used as default values for each panel.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>panel</em>: a serialised {@link NcAnimatePanelBean} containing
     *       the default values for each panel.
     *   </li>
     *   <li><em>legend</em>: a serialised {@link NcAnimateLegendBean} containing
     *       the default values for the legend of each NetCDF layer of each panel.
     *   </li>
     * </ul>
     *
     * @param jsonDefaults {@code JSONWrapperObject} representing a NcAnimate defaults bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateDefaultsBean(JSONWrapperObject jsonDefaults) throws Exception {
        super(jsonDefaults);
    }

    /**
     * Create a NcAnimate defaults bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateDefaultsBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate defaults bean part ID.
     * @param jsonDefaults {@code JSONWrapperObject} representing a NcAnimate defaults bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateDefaultsBean(String id, JSONWrapperObject jsonDefaults) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.DEFAULTS, id), jsonDefaults);
    }

    /**
     * Load the attributes of the NcAnimate defaults bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonDefaults {@code JSONWrapperObject} representing a NcAnimate defaults bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonDefaults) throws Exception {
        super.parse(jsonDefaults);
        if (jsonDefaults != null) {

            this.panel = null;
            Class panelClass = jsonDefaults.getClass("panel");
            if (panelClass != null) {
                if (String.class.equals(panelClass)) {
                    this.setPanel(jsonDefaults.get(String.class, "panel"));
                } else if (JSONWrapperObject.class.equals(panelClass)) {
                    this.setPanel(jsonDefaults.get(JSONWrapperObject.class, "panel"));
                } else {
                    LOGGER.error(String.format("Invalid default panel config. Expected panel ID (String) or panel configuration (JSONObject). Found %s.%n%s",
                        panelClass.getName(), jsonDefaults));
                }
            }

            this.legend = null;
            Class legendClass = jsonDefaults.getClass("legend");
            if (legendClass != null) {
                if (String.class.equals(legendClass)) {
                    this.setLegend(jsonDefaults.get(String.class, "legend"));
                } else if (JSONWrapperObject.class.equals(legendClass)) {
                    this.setLegend(jsonDefaults.get(JSONWrapperObject.class, "legend"));
                } else {
                    LOGGER.error(String.format("Invalid default legend config. Expected legend ID (String) or legend configuration (JSONObject). Found %s.%n%s",
                        legendClass.getName(), jsonDefaults));
                }
            }
        }
    }

    private void setPanel(String panelId) throws Exception {
        this.panel = null;

        if (panelId != null) {
            this.panel = new NcAnimatePanelBean(panelId, null);
        }
    }

    private void setPanel(JSONWrapperObject jsonPanel) throws Exception {
        this.panel = null;

        if (jsonPanel != null) {
            this.panel = new NcAnimatePanelBean(jsonPanel);
        }
    }

    private void setLegend(String legendId) throws Exception {
        this.legend = null;

        if (legendId != null) {
            this.legend = new NcAnimateLegendBean(legendId, null);
        }
    }

    private void setLegend(JSONWrapperObject jsonLegend) throws Exception {
        this.legend = null;

        if (jsonLegend != null) {
            this.legend = new NcAnimateLegendBean(jsonLegend);
        }
    }

    /**
     * Returns the panels default values.
     * @return the panels default values.
     */
    public NcAnimatePanelBean getPanel() {
        return this.panel;
    }

    /**
     * Returns the legends default values.
     * @return the legends default values.
     */
    public NcAnimateLegendBean getLegend() {
        return this.legend;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.panel != null) {
            json.put("panel", this.panel.toJSON());
        }
        if (this.legend != null) {
            json.put("legend", this.legend.toJSON());
        }

        return json.isEmpty() ? null : json;
    }
}
