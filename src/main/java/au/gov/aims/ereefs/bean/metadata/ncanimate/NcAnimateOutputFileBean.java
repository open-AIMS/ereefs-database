/*
 *  Copyright (C) 2021 Australian Institute of Marine Science
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
package au.gov.aims.ereefs.bean.metadata.ncanimate;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONObject;

/**
 * Bean representing an {@code ereefs-ncanimate2} output file.
 * It's part of the {@link NcAnimateOutputFileMetadataBean} which is used with
 * the {@code ereefs-ncanimate2} project.
 */
public class NcAnimateOutputFileBean extends AbstractBean {
    private String fileURI;
    private Integer width;
    private Integer height;
    private Integer fps;
    private String type;
    private String filetype;

    /**
     * Empty constructor. Use setters to set attributes.
     */
    public NcAnimateOutputFileBean() {}

    /**
     * Construct a {@code NcAnimateOutputFileBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     * @param json JSON serialised NcAnimateOutputFileBean.
     */
    public NcAnimateOutputFileBean(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.fileURI = json.optString("fileURI", null);

        this.width = json.has("width") ? json.optInt("width", 0) : null;
        this.height = json.has("height") ? json.optInt("height", 0) : null;
        this.fps = json.has("fps") ? json.optInt("fps", 0) : null;

        this.type = json.optString("type", null);
        this.filetype = json.optString("filetype", null);
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} file URI.
     * @return the {@code NcAnimateOutputFileBean} file URI.
     */
    public String getFileURI() {
        return this.fileURI;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} file URI.
     * @param fileURI the {@code NcAnimateOutputFileBean} file URI.
     */
    public void setFileURI(String fileURI) {
        this.fileURI = fileURI;
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} width.
     * @return the {@code NcAnimateOutputFileBean} width.
     */
    public Integer getWidth() {
        return this.width;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} width.
     * @param width the {@code NcAnimateOutputFileBean} width.
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} height.
     * @return the {@code NcAnimateOutputFileBean} height.
     */
    public Integer getHeight() {
        return this.height;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} height.
     * @param height the {@code NcAnimateOutputFileBean} height.
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} video FPS.
     * @return the {@code NcAnimateOutputFileBean} video FPS.
     */
    public Integer getFps() {
        return this.fps;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} video FPS.
     * @param fps the {@code NcAnimateOutputFileBean} video FPS.
     */
    public void setFps(Integer fps) {
        this.fps = fps;
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} type.
     * Either {@code MAP} or {@code VIDEO}.
     * @return the {@code NcAnimateOutputFileBean} type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} type.
     * Either {@code MAP} or {@code VIDEO}.
     * @param type the {@code NcAnimateOutputFileBean} type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the {@code NcAnimateOutputFileBean} file type.
     * Either a {@link au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean.VideoFormat}
     * or a {@link au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean.MapFormat}.
     * @return the {@code NcAnimateOutputFileBean} file type.
     */
    public String getFiletype() {
        return this.filetype;
    }

    /**
     * Set the {@code NcAnimateOutputFileBean} file type.
     * Either a {@link au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderVideoBean.VideoFormat}
     * or a {@link au.gov.aims.ereefs.bean.ncanimate.render.NcAnimateRenderMapBean.MapFormat}.
     * @param filetype the {@code NcAnimateOutputFileBean} file type.
     */
    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("fileURI", this.fileURI);
        json.put("width", this.width);
        json.put("height", this.height);
        json.put("fps", this.fps);
        json.put("type", this.type);
        json.put("filetype", this.filetype);

        return json;
    }
}
