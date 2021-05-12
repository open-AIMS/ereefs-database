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

/**
 * NcAnimate padding bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateCanvasBean}
 * and in {@link NcAnimateLegendBean}.</p>
 *
 * <p>It's used to create space around an element's content.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "top": 80,
 *     "left": 16,
 *     "bottom": 115,
 *     "right": 16
 * }</pre>
 */
public class NcAnimatePaddingBean extends AbstractNcAnimateBean {
    private Integer top;
    private Integer bottom;
    private Integer left;
    private Integer right;

    /**
     * Create a NcAnimate padding bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>top</em>: value of the top padding, in pixels.</li>
     *   <li><em>bottom</em>: value of the bottom padding, in pixels.</li>
     *   <li><em>left</em>: value of the left padding, in pixels.</li>
     *   <li><em>right</em>: value of the right padding, in pixels.</li>
     * </ul>
     *
     * @param jsonPadding {@code JSONWrapperObject} representing a NcAnimate padding bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePaddingBean(JSONWrapperObject jsonPadding) throws Exception {
        super(jsonPadding);
    }

    /**
     * Create a NcAnimate padding bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimatePaddingBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate padding bean part ID.
     * @param jsonPadding {@code JSONWrapperObject} representing a NcAnimate padding bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePaddingBean(String id, JSONWrapperObject jsonPadding) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.PADDING, id), jsonPadding);
    }

    /**
     * Load the attributes of the NcAnimate padding bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonPadding {@code JSONWrapperObject} representing a NcAnimate padding bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonPadding) throws Exception {
        super.parse(jsonPadding);
        if (jsonPadding != null) {
            this.top = jsonPadding.get(Integer.class, "top");
            this.bottom = jsonPadding.get(Integer.class, "bottom");

            this.left = jsonPadding.get(Integer.class, "left");
            this.right = jsonPadding.get(Integer.class, "right");
        }
    }

    /**
     * Returns the top padding, in pixels.
     * @return the top padding.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Returns the bottom padding, in pixels.
     * @return the bottom padding.
     */
    public Integer getBottom() {
        return this.bottom;
    }

    /**
     * Returns the left padding, in pixels.
     * @return the left padding.
     */
    public Integer getLeft() {
        return this.left;
    }

    /**
     * Returns the right padding, in pixels.
     * @return the right padding.
     */
    public Integer getRight() {
        return this.right;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("top", this.top);
        json.put("bottom", this.bottom);
        json.put("left", this.left);
        json.put("right", this.right);

        return json.isEmpty() ? null : json;
    }
}
