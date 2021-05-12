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
 * NcAnimate bounding box bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateRegionBean}.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "_id": {
 *         "id": "queensland",
 *         "datatype": "BBOX"
 *     },
 *     "east": 156.57,
 *     "north": -7.39,
 *     "south": -29.26,
 *     "west": 142.36
 * }</pre>
 */
public class NcAnimateBboxBean extends AbstractNcAnimateBean {
    private Double east;
    private Double north;
    private Double south;
    private Double west;

    /**
     * Create a NcAnimate bounding box bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>east</em>: value of the most Eastern longitude coordinate.</li>
     *   <li><em>north</em>: value of the most Northern latitude coordinate.</li>
     *   <li><em>south</em>: value of the most Southern latitude coordinate.</li>
     *   <li><em>west</em>: value of the most Western longitude coordinate.</li>
     * </ul>
     *
     * @param jsonBbox {@code JSONWrapperObject} representing a NcAnimate bounding box bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateBboxBean(JSONWrapperObject jsonBbox) throws Exception {
        super(jsonBbox);
    }

    /**
     * Create a NcAnimate bounding box bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateBboxBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate bounding box bean part ID.
     * @param jsonBbox {@code JSONWrapperObject} representing a NcAnimate bounding box bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateBboxBean(String id, JSONWrapperObject jsonBbox) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.BBOX, id), jsonBbox);
    }

    /**
     * Load the attributes of the NcAnimate bounding box bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonBbox {@code JSONWrapperObject} representing a NcAnimate bounding box bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonBbox) throws Exception {
        super.parse(jsonBbox);
        if (jsonBbox != null) {
            this.east = jsonBbox.get(Double.class, "east");
            this.north = jsonBbox.get(Double.class, "north");
            this.south = jsonBbox.get(Double.class, "south");
            this.west = jsonBbox.get(Double.class, "west");
        }
    }

    /**
     * Returns the bounding box most Eastern longitude coordinate.
     * @return the bounding box most Eastern longitude coordinate.
     */
    public Double getEast() {
        return this.east;
    }

    /**
     * Returns the bounding box most Northern latitude coordinate.
     * @return the bounding box most Northern latitude coordinate.
     */
    public Double getNorth() {
        return this.north;
    }

    /**
     * Returns the bounding box most Southern latitude coordinate.
     * @return the bounding box most Southern latitude coordinate.
     */
    public Double getSouth() {
        return this.south;
    }

    /**
     * Returns the bounding box most Western longitude coordinate.
     * @return the bounding box most Western longitude coordinate.
     */
    public Double getWest() {
        return this.west;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("east", this.east);
        json.put("north", this.north);
        json.put("south", this.south);
        json.put("west", this.west);

        return json.isEmpty() ? null : json;
    }
}
