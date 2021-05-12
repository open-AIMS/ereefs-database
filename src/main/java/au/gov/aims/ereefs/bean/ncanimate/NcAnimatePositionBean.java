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
 * NcAnimate position bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateLegendBean}
 * and in {@link NcAnimateTextBean}.</p>
 *
 * <p>This is used to position an element with an offset from a point of reference.
 * It doesn't make sense to define top and bottom at the same time, or
 * left and right at the same time.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "bottom": 16,
 *     "right": 0
 * }</pre>
 *
 * <p>NOTE: An overwrites can to change the position of an element.
 * For example, if we want to change the element positioned
 * from the bottom to the top, we can provide the overwrite:</p>
 * <pre class="code">
 * {
 *     "top": 16
 * }</pre>
 *
 * <p>Unfortunately, such an overwrite does not get rid of the other
 * attributes and the result position would look like this:</p>
 * <pre class="code">
 * {
 *     "top": 16,
 *     "bottom": 16,
 *     "right": 0
 * }</pre>
 *
 * <p>and NcAnimate would not know if it needs to position the element
 * from the top or the bottom.</p>
 *
 * <p>The solution for this is to provide a {@code pos} properties
 * when the position of an element needs to be overwritten.
 * See {@link #getPos()}.</p>
 */
public class NcAnimatePositionBean extends AbstractNcAnimateBean {
    // NOTE: The "pos" attribute is used to allow overwrites to eliminate attributes.
    private String pos;

    private Integer top;
    private Integer bottom;
    private Integer left;
    private Integer right;

    /**
     * Create a NcAnimate position bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>top</em>: the position from the top, in pixels.</li>
     *   <li><em>bottom</em>: the position from the bottom, in pixels.</li>
     *   <li><em>left</em>: the position from the left, in pixels.</li>
     *   <li><em>right</em>: the position from the right, in pixels.</li>
     *   <li><em>pos</em>: the position attribute to consider. Used with overwrites.
     *       See comment bellow.</li>
     * </ul>
     *
     * <p>Only {@code top} or {@code bottom} can be specified, not both.
     *     Only {@code left} or {@code right} can be specified, not both.
     *     If an element is positioned to the {@code top}, and an overwrite attempt
     *     to re-positioned to the {@code bottom}, the position object will have
     *     both {@code top} and {@code bottom} attributes since there is
     *     no way to remove an attribute using layer overwrites.</p>
     *
     * <p>In this case, the {@code pos} attribute must be provided to specified
     *     which attribute should be considered.<p>
     *
     * <p>Example:<p>
     * <pre class="code">
     * {
     *     "top": 16,
     *     "bottom": 16,
     *     "right": 0,
     *     "pos": "br"
     * }</pre>
     *
     * <p><em>pos</em> values</p>
     * <ul>
     *   <li><em>t</em>: consider the {@code top} attribute.</li>
     *   <li><em>b</em>: consider the {@code bottom} attribute.</li>
     *   <li><em>l</em>: consider the {@code left} attribute.</li>
     *   <li><em>r</em>: consider the {@code right} attribute.</li>
     * </ul>
     *
     * @param jsonPosition {@code JSONWrapperObject} representing a NcAnimate position bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePositionBean(JSONWrapperObject jsonPosition) throws Exception {
        super(jsonPosition);
    }

    /**
     * Create a NcAnimate position bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimatePositionBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate position bean part ID.
     * @param jsonPosition {@code JSONWrapperObject} representing a NcAnimate position bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimatePositionBean(String id, JSONWrapperObject jsonPosition) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.POSITION, id), jsonPosition);
    }

    /**
     * Load the attributes of the NcAnimate position bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonPosition {@code JSONWrapperObject} representing a NcAnimate position bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonPosition) throws Exception {
        super.parse(jsonPosition);
        if (jsonPosition != null) {
            this.top = jsonPosition.get(Integer.class, "top");
            this.bottom = jsonPosition.get(Integer.class, "bottom");

            this.left = jsonPosition.get(Integer.class, "left");
            this.right = jsonPosition.get(Integer.class, "right");

            this.pos = jsonPosition.get(String.class, "pos");
            if (pos != null) {
                if (pos.indexOf('t') < 0) { this.top = null; }
                if (pos.indexOf('b') < 0) { this.bottom = null; }
                if (pos.indexOf('l') < 0) { this.left = null; }
                if (pos.indexOf('r') < 0) { this.right = null; }
            } else {
                StringBuilder posSb = new StringBuilder();
                if (this.top != null)    { posSb.append('t'); }
                if (this.bottom != null) { posSb.append('b'); }
                if (this.left != null)   { posSb.append('l'); }
                if (this.right != null)  { posSb.append('r'); }
                this.pos = posSb.toString();
            }
        }
    }

    /**
     * Returns the position string, defining which attributes to consider.
     * @return the position string.
     */
    public String getPos() {
        return this.pos;
    }

    /**
     * Returns the position from the top, in pixels.
     * @return the position from the top.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Returns the position from the bottom, in pixels.
     * @return the position from the bottom.
     */
    public Integer getBottom() {
        return this.bottom;
    }

    /**
     * Returns the position from the left, in pixels.
     * @return the position from the left.
     */
    public Integer getLeft() {
        return this.left;
    }

    /**
     * Returns the position from the right, in pixels.
     * @return the position from the right.
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

        json.put("pos", this.pos);

        json.put("top", this.top);
        json.put("bottom", this.bottom);
        json.put("left", this.left);
        json.put("right", this.right);

        return json.isEmpty() ? null : json;
    }
}
