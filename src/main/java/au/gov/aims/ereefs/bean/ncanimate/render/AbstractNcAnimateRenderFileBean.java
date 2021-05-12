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
package au.gov.aims.ereefs.bean.ncanimate.render;

import au.gov.aims.ereefs.bean.ncanimate.AbstractNcAnimateBean;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

/**
 * NcAnimate render file bean part.
 *
 * <p>This abstract class is used to allow {@link NcAnimateRenderVideoBean}
 * and {@link NcAnimateRenderMapBean} to be mixed together in collections,
 * and to reuse common code.</p>
 */
public abstract class AbstractNcAnimateRenderFileBean extends AbstractNcAnimateBean {

    private String fileURI;
    private Integer maxWidth;
    private Integer maxHeight;

    /**
     * Create a copy of a render file bean object.
     * This is similar to a deep clone method.
     *
     * <p>NOTE: This method does NOT copy any file on disk.</p>
     *
     * @param renderFile the {@link AbstractNcAnimateRenderFileBean} bean to copy.
     * @return a copy of the {@link AbstractNcAnimateRenderFileBean} bean.
     * @throws Exception if something goes wrong during the copy.
     */
    public static AbstractNcAnimateRenderFileBean copyRenderFile(AbstractNcAnimateRenderFileBean renderFile) throws Exception {
        if (renderFile == null) {
            return null;
        }
        return AbstractNcAnimateRenderFileBean.createRenderFile(new JSONWrapperObject(renderFile.toJSON()));
    }

    /**
     * Create a {@link AbstractNcAnimateRenderFileBean} from a serialised render file.
     *
     * @param jsonRenderFile the serialised {@link AbstractNcAnimateRenderFileBean} object.
     * @return a new {@link AbstractNcAnimateRenderFileBean} object.
     * @throws Exception if the json object is malformed.
     */
    public static AbstractNcAnimateRenderFileBean createRenderFile(JSONWrapperObject jsonRenderFile) throws Exception {
        String fileTypeStr = jsonRenderFile.get(String.class, "fileType");
        if (fileTypeStr != null) {
            RenderFileType fileType = RenderFileType.valueOf(fileTypeStr.toUpperCase());

            switch (fileType) {
                case MAP:
                    return new NcAnimateRenderMapBean(jsonRenderFile);

                case VIDEO:
                    return new NcAnimateRenderVideoBean(jsonRenderFile);
            }
        }

        return null;
    }

    /**
     * Create a NcAnimate render file bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>fileURI</em>: the URI where the render file is uploaded.</li>
     *   <li><em>maxWidth</em>: Constraint the video or map dimensions.</li>
     *   <li><em>maxHeight</em>: Constraint the video or map dimensions.</li>
     * </ul>
     *
     * @param jsonRenderFile {@code JSONWrapperObject} representing a NcAnimate render file bean part.
     * @throws Exception if the json object is malformed.
     */
    public AbstractNcAnimateRenderFileBean(JSONWrapperObject jsonRenderFile) throws Exception {
        super(jsonRenderFile);
    }

    /**
     * Load the attributes of the NcAnimate render file bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonRenderFile {@code JSONWrapperObject} representing a NcAnimate render file bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonRenderFile) throws Exception {
        super.parse(jsonRenderFile);
        if (jsonRenderFile != null) {
            this.setFileURI(jsonRenderFile.get(String.class, "fileURI"));

            this.maxWidth = jsonRenderFile.get(Integer.class, "maxWidth");
            this.maxHeight = jsonRenderFile.get(Integer.class, "maxHeight");
        }
    }

    /**
     * Set the URI of the rendered file.
     * Supports protocols {@code s3://} and {@code file://}.
     * @param fileURIStr the URI of the rendered file
     */
    public void setFileURI(String fileURIStr) {
        this.fileURI = fileURIStr;
    }

    /**
     * Returns the URI of the rendered file.
     * Supports protocols {@code s3://} and {@code file://}.
     * @return the URI of the rendered file.
     */
    public String getFileURI() {
        return this.fileURI;
    }

    /**
     * Returns the render file max width, if set.
     * @return the render file max width.
     */
    public Integer getMaxWidth() {
        return this.maxWidth;
    }

    /**
     * Returns the render file max height, if set.
     * @return the render file max height.
     */
    public Integer getMaxHeight() {
        return this.maxHeight;
    }

    /**
     * Returns the {@link RenderFileType}.
     * <ul>
     *   <li><em>{@link RenderFileType#MAP}</em>: for raster map files, such as
     *     <ul>
     *       <li>{@link NcAnimateRenderMapBean.MapFormat#SVG},</li>
     *       <li>{@link NcAnimateRenderMapBean.MapFormat#PNG},</li>
     *       <li>...</li>
     *     </ul>
     *   </li>
     *   <li><em>{@link RenderFileType#VIDEO}</em>: for video files, such as
     *     <ul>
     *       <li>{@link NcAnimateRenderVideoBean.VideoFormat#MP4},</li>
     *       <li>{@link NcAnimateRenderVideoBean.VideoFormat#WMV},</li>
     *       <li>...</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the {@link RenderFileType}.
     */
    public abstract RenderFileType getFileType();

    /**
     * Returns the file extension.
     *
     * <p>For {@link RenderFileType#MAP}:</p>
     * <ul>
     *   <li>Returns {@code svg} for: {@link NcAnimateRenderMapBean.MapFormat#SVG},</li>
     *   <li>Returns {@code png} for: {@link NcAnimateRenderMapBean.MapFormat#PNG},</li>
     *   <li>...</li>
     * </ul>
     * <p>See: {@link NcAnimateRenderMapBean}</p>
     *
     * <p>For {@link RenderFileType#VIDEO}:</p>
     * <ul>
     *   <li>Returns {@code mp4} for: {@link NcAnimateRenderVideoBean.VideoFormat#MP4},</li>
     *   <li>Returns {@code wmv} for: {@link NcAnimateRenderVideoBean.VideoFormat#WMV},</li>
     *   <li>...</li>
     * </ul>
     * <p>See: {@link NcAnimateRenderVideoBean}</p>
     *
     * @return the file extension.
     */
    public abstract String getFileExtension();

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("fileType", this.getFileType().name());
        json.put("fileURI", this.fileURI);
        json.put("maxWidth", this.maxWidth);
        json.put("maxHeight", this.maxHeight);

        return json;
    }

    /**
     * List of supported render file types.
     */
    public enum RenderFileType {
        MAP, VIDEO
    }
}
