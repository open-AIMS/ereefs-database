/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

/**
 * NcAnimate region bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateConfigBean}.
 * It is also used in the output file metadata object {@link au.gov.aims.ereefs.bean.metadata.ncanimate.NcAnimateOutputFileMetadataBean}.</p>
 *
 * <p>It's used to define a geographical region.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "label": "Queensland",
 *     "scale": 1,
 *     "bbox": {
 *         "east": 156.570062893342,
 *         "north": -7.38712861975422,
 *         "south": -29.2584086447012,
 *         "west": 142.358682747285
 *     }
 * }</pre>
 */
public class NcAnimateRegionBean extends AbstractNcAnimateBean {
    private String label;
    private Integer scale;
    private NcAnimateBboxBean bbox;

    /**
     * Create a NcAnimate region bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>label</em>: the region display name.</li>
     *   <li><em>scale</em>: a number used to group regions together. Can be ignored.</li>
     *   <li><em>bbox</em>: the region geographical bounding box coordinates.</li>
     * </ul>
     *
     * @param jsonRegion {@code JSONWrapperObject} representing a NcAnimate region bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRegionBean(JSONWrapperObject jsonRegion) throws Exception {
        super(jsonRegion);
    }

    /**
     * Create a NcAnimate region bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateRegionBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate region bean part ID.
     * @param jsonRegion {@code JSONWrapperObject} representing a NcAnimate region bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRegionBean(String id, JSONWrapperObject jsonRegion) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.REGION, id), jsonRegion);
    }

    /**
     * Load the attributes of the NcAnimate region bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonRegion {@code JSONWrapperObject} representing a NcAnimate region bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonRegion) throws Exception {
        super.parse(jsonRegion);
        if (jsonRegion != null) {
            this.label = jsonRegion.get(String.class, "label");
            this.scale = jsonRegion.get(Integer.class, "scale");
            this.setBbox(jsonRegion.get(JSONWrapperObject.class, "bbox"));
        }
    }

    private void setBbox(JSONWrapperObject jsonBbox) throws Exception {
        this.bbox = null;

        if (jsonBbox != null) {
            this.bbox = new NcAnimateBboxBean(jsonBbox);
        }
    }

    /**
     * Returns the region display name.
     * @return the region display name.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the region scale group number.
     * @return the region scale group number.
     */
    public Integer getScale() {
        return this.scale;
    }

    /**
     * Returns the region geographical bounding box coordinates.
     * @return the region geographical bounding box coordinates.
     */
    public NcAnimateBboxBean getBbox() {
        return this.bbox;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("label", this.label);
        json.put("scale", this.scale);
        if (this.bbox != null) {
            json.put("bbox", this.bbox.toJSON());
        }

        return json.isEmpty() ? null : json;
    }
}
