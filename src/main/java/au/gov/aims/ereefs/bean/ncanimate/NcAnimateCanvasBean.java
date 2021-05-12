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
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * NcAnimate canvas bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateConfigBean}.</p>
 *
 * <p>This is the configuration part which describe where each elements are rendered.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "_id": {
 *         "id": "default-canvas",
 *         "datatype": "CANVAS"
 *     },
 *     "backgroundColour": "#FFFFFF",
 *     "padding": {
 *         "top": 80,
 *         "left": 16,
 *         "bottom": 115,
 *         "right": 16
 *     },
 *     "paddingBetweenPanels": 8,
 *     "texts": {
 *         "authors": {
 *             "text": "Data: eReefs CSIRO GBR4 Hydrodynamic Model v2.0. Map generation: AIMS",
 *             "fontSize": 12,
 *             "position": {
 *                 "bottom": 76,
 *                 "right": 16
 *             },
 *             "bold": false,
 *             "italic": true
 *         }
 *     }
 * }</pre>
 */
public class NcAnimateCanvasBean extends AbstractNcAnimateBean {
    private Map<String, NcAnimateTextBean> texts;

    // Style

    private String backgroundColour;
    private NcAnimatePaddingBean padding;
    private Integer paddingBetweenPanels;

    /**
     * Create a NcAnimate canvas bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>backgroundColour</em>: background colour of the canvas. Example: {@code "#FFFFFF"}.</li>
     *   <li><em>padding</em>: serialised {@link NcAnimatePaddingBean} defining the space around the canvas.</li>
     *   <li><em>paddingBetweenPanels</em>: integer used to set the distance between map panels on the canvas.</li>
     *   <li><em>texts</em>: {@code JSONWrapperObject} of serialised {@link NcAnimateTextBean}. Keys are arbitrary.</li>
     * </ul>
     *
     * @param jsonCanvas {@code JSONWrapperObject} representing a NcAnimate canvas bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateCanvasBean(JSONWrapperObject jsonCanvas) throws Exception {
        super(jsonCanvas);
    }

    /**
     * Create a NcAnimate canvas bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateCanvasBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate canvas bean part ID.
     * @param jsonCanvas {@code JSONWrapperObject} representing a NcAnimate canvas bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateCanvasBean(String id, JSONWrapperObject jsonCanvas) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.CANVAS, id), jsonCanvas);
    }

    /**
     * Load the attributes of the NcAnimate canvas bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonCanvas {@code JSONWrapperObject} representing a NcAnimate canvas bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonCanvas) throws Exception {
        super.parse(jsonCanvas);
        if (jsonCanvas != null) {
            this.setTexts(jsonCanvas.get(JSONWrapperObject.class, "texts"));

            // Style

            this.backgroundColour = jsonCanvas.get(String.class, "backgroundColour");
            this.setPadding(jsonCanvas.get(JSONWrapperObject.class, "padding"));
            this.paddingBetweenPanels = jsonCanvas.get(Integer.class, "paddingBetweenPanels");
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

    private void setPadding(JSONWrapperObject jsonPadding) throws Exception {
        this.padding = null;

        if (jsonPadding != null) {
            this.padding = new NcAnimatePaddingBean(jsonPadding);
        }
    }

    /**
     * Returns the {@code Map} of {@link NcAnimateTextBean} to render on the canvas.
     * @return the {@code Map} of {@link NcAnimateTextBean}.
     */
    public Map<String, NcAnimateTextBean> getTexts() {
        return this.texts;
    }

    // Style


    /**
     * Returns the background colour of the canvas.
     * @return the background colour of the canvas.
     */
    public String getBackgroundColour() {
        return this.backgroundColour;
    }

    /**
     * Returns the canvas' padding.
     * @return the canvas' padding.
     */
    public NcAnimatePaddingBean getPadding() {
        return this.padding;
    }

    /**
     * Returns the padding between panels on the canvas.
     * @return the padding between panels.
     */
    public Integer getPaddingBetweenPanels() {
        return this.paddingBetweenPanels;
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

        // Style

        json.put("backgroundColour", this.backgroundColour);
        if (this.padding != null) {
            json.put("padding", this.padding.toJSON());
        }
        json.put("paddingBetweenPanels", this.paddingBetweenPanels);

        return json.isEmpty() ? null : json;
    }
}
