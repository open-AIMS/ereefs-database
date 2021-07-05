/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate.render;

import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * NcAnimate render map bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateRenderBean}.</p>
 *
 * <p>Used to defined how to render a map, as in a
 * raster-graphical image contains geographic map(s).</p>
 *
 * <p>It can generate the following file formats:</p>
 * <ul>
 *     <li><em>SVG</em>: <a href="https://en.wikipedia.org/wiki/Scalable_Vector_Graphics" target="_blank">Scalable Vector Graphics</a>. Large file size but very flexible.
 *         They can be edited using vector graphics software such as <a href="https://inkscape.org/" target="_blank">InkScape</a>
 *         or <a href="https://www.adobe.com/au/products/illustrator.html" target="_blank">Adobe Illustrator</a>.<br/>
 *         NOTE: <a href="https://inkscape.org/" target="_blank">InkScape</a> is recommended since it support SVG {@code groups},
 *         which are shown as layers. {@code Adobe Illustrator} has a proprietary implementation of layers which
 *         doesn't translate to SVG.</li>
 *     <li><em>PNG</em>: <a href="https://en.wikipedia.org/wiki/Portable_Network_Graphics" target="_blank">Portable Network Graphics</a>. The preferred file format for raster images.</li>
 *     <li><em>JPG</em>: <a href="https://en.wikipedia.org/wiki/JPEG" target="_blank">JPEG file format</a>. Small file size with visible compression artifacts.
 *         Not suitable for data layer with subtle variation.</li>
 *     <li><em>GIF</em>: <a href="https://en.wikipedia.org/wiki/GIF" target="_blank">Graphics Interchange Format</a>. Limited to 256 colours. Not suitable for most data layers.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "format": "SVG"
 * }</pre>
 */
public class NcAnimateRenderMapBean extends AbstractNcAnimateRenderFileBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateRenderMapBean.class);

    private MapFormat format;

    /**
     * Create a NcAnimate render map bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>format</em>: the file format to generate.
     *     <p>Example: {@code SVG}</p></li>
     * </ul>
     *
     * <p>See {@link AbstractNcAnimateRenderFileBean#AbstractNcAnimateRenderFileBean(JSONWrapperObject)}
     *   for inherited attributes.</p>
     *
     * @param jsonRenderMap {@code JSONWrapperObject} representing a NcAnimate render map bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRenderMapBean(JSONWrapperObject jsonRenderMap) throws Exception {
        super(jsonRenderMap);
    }

    /**
     * Load the attributes of the NcAnimate render map bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonRenderMap {@code JSONWrapperObject} representing a NcAnimate render map bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonRenderMap) throws Exception {
        super.parse(jsonRenderMap);
        if (jsonRenderMap != null) {
            this.setFormat(jsonRenderMap.get(String.class, "format"));
        }
    }

    private void setFormat(String formatStr) {
        this.format = null;

        if (formatStr != null && !formatStr.isEmpty()) {
            try {
                this.format = MapFormat.valueOf(formatStr.toUpperCase());
            } catch (Exception ex) {
                LOGGER.warn("Invalid map format: " + formatStr, ex);
            }
        }
    }

    /**
     * Returns the map format.
     * @return the map format.
     */
    public MapFormat getFormat() {
        return this.format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileExtension() {
        return this.format == null ? null : this.format.getExtension();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenderFileType getFileType() {
        return RenderFileType.MAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.format != null) {
            json.put("format", this.format.name());
        }

        return json.isEmpty() ? null : json;
    }

    /**
     * List of supported raster map file format.
     */
    public enum MapFormat {
        SVG("svg", false),
        PNG("png", true),
        GIF("gif", true),
        JPG("jpg", true);

        private String extension;
        private boolean raster;

        private MapFormat(String extension, boolean raster) {
            this.extension = extension;
            this.raster = raster;
        }

        public String getExtension() {
            return this.extension;
        }

        public boolean isRaster() {
            return this.raster;
        }

        public boolean isVector() {
            return !this.raster;
        }

        public static MapFormat fromExtension(String extension) {
            if (extension == null) {
                return null;
            }

            for (MapFormat mapFormat : MapFormat.values()) {
                if (extension.equalsIgnoreCase(mapFormat.extension)) {
                    return mapFormat;
                }
            }

            return null;
        }
    }
}
