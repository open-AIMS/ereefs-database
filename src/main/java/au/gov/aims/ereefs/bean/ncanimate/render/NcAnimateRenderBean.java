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

import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.metadata.TimeIncrement;
import au.gov.aims.ereefs.bean.ncanimate.AbstractNcAnimateBean;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * NcAnimate render bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean}.</p>
 *
 * <p>Used to defined what to render ({@code VIDEO} and/or {@code MAP}),
 * which format ({@code MP4}, {@code WMV}, etc), where to render it,
 * and more.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "workingDirectory": "/tmp/ncanimate/working",
 *     "paletteDirectoryUri": "s3://my-bucket/palettes",
 *     "directoryUri": "s3://my-bucket/products/${id}",
 *     "frameDirectoryUri": "s3://my-bucket/frames/${id}",
 *     "timezone": "Australia/Brisbane",
 *     "videoTimeIncrement": {
 *         "increment": 1,
 *         "unit": "MONTH"
 *     },
 *
 *     "scale": 1.2,
 *
 *     "videos": {
 *         "mp4Video": {
 *             "format": "MP4",
 *             "fps": 15,
 *             "blockSize": [16, 16],
 *             "commandLines": [
 *                 "/usr/bin/ffmpeg -y -r \"${ctx.renderFile.fps}\" -i \"${ctx.videoFrameDirectory}/${ctx.frameFilenamePrefix}_%05d.png\" -vcodec libx264 -profile:v baseline -pix_fmt yuv420p -crf 29 -vf \"pad=${ctx.productWidth}:${ctx.productHeight}:${ctx.padding.left}:${ctx.padding.top}:white\" \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\"",
 *                 "/usr/bin/qt-faststart \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\" \"${ctx.outputFile}\"",
 *                 "rm \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\""
 *             ]
 *         },
 *         "wmvVideo": {
 *             "format": "WMV",
 *             "fps": 10,
 *             "commandLines": ["/usr/bin/ffmpeg -y -r \"${ctx.renderFile.fps}\" -i \"${ctx.videoFrameDirectory}/${ctx.frameFilenamePrefix}_%05d.png\" -qscale 10 -s ${ctx.productWidth}x${ctx.productHeight} \"${ctx.outputFile}\""],
 *             "maxWidth": 1280
 *         },
 *         "zipArchive": {
 *             "format": "ZIP"
 *         }
 *     },
 *     "maps": {
 *         "svgMap": {
 *             "format": "SVG"
 *         },
 *         "pngMap": {
 *             "format": "PNG"
 *         }
 *     },
 *
 *     "metadata": {
 *         "properties": {
 *             "targetHeight": "${ctx.targetHeight}",
 *             "framePeriod": "${ctx.framePeriod}"
 *         }
 *     }
 * }</pre>
 */
public class NcAnimateRenderBean extends AbstractNcAnimateBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateRenderBean.class);
    private static final File DEFAULT_WORKING_DIRECTORY = new File(new File(System.getProperty("java.io.tmpdir")), "ncanimate");
    public static final DateTimeZone DEFAULT_DATETIMEZONE = DateTimeZone.UTC;
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final float DEFAULT_SCALE = 1f;

    // Directory (on filesystem or S3 bucket) representing where the files (videos and maps) are stored.
    private String directoryUri;

    // Directory (on filesystem or S3 bucket) representing where the frames (video frames and maps) are stored.
    private String frameDirectoryUri;

    private String paletteDirectoryUri;

    // Directory (on filesystem) used to create / copy temporary files. If unspecified, "/tmp/ncanimate" is used.
    private String workingDirectory;

    // Path to the NcAnimate frame jar file (ereefs-ncanimate2-frame-0.1-jar-with-dependencies.jar)
    // If the attribute is missing or represent a non existent file, NcAnimate will
    // look in the same directory as NcAnimate jar file.
    // If the path represent a directory, NcAnimate will try to find an appropriate
    // file in the directory (ereefs-ncanimate2-frame*-jar-with-dependencies.jar).
    private String ncanimateFrameJar;

    // The definitionID is the NcAnimateConfigId by default.
    // It can be overwrite using this property.
    // This is used to create "Hourly", "Daily", "Monthly" output files
    // into the same group (definitionId) using different NcAnimate config
    // (different variables from different input files).
    private String definitionId;

    // The resolution multiplier.
    private Float scale;

    // Timezone to use to parse dates from input files,
    //     to do the grouping (daily, monthly, etc)
    //     and to display the date on each frame
    private String timezone; // Example: "Australia/Brisbane"

    // Locale used to format dates.
    // Default: "en"
    private String locale; // Example: "en"

    private TimeIncrement videoTimeIncrement;
    private TimeIncrement frameTimeIncrement;

    // Used for one-off products
    private String startDate;
    private String endDate;

    private Map<String, NcAnimateRenderMapBean> maps;
    private Map<String, NcAnimateRenderVideoBean> videos;

    private NcAnimateRenderMetadataBean metadata;

    /**
     * Create a NcAnimate render bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>directoryUri</em>: Directory representing where the files
     *     ({@link AbstractNcAnimateRenderFileBean.RenderFileType#MAP} and
     *     {@link AbstractNcAnimateRenderFileBean.RenderFileType#VIDEO}) are stored.
     *     Supports protocol {@code s3://} and {@code file://}</li>
     *
     *   <li><em>frameDirectoryUri</em>: Directory representing where the frames
     *     ({@link AbstractNcAnimateRenderFileBean.RenderFileType#MAP} and
     *     {@link AbstractNcAnimateRenderFileBean.RenderFileType#VIDEO}) are stored.
     *     It's required for the {@code Download current video frame} feature
     *     to work.
     *     Supports protocol {@code s3://} and {@code file://}</li>
     *
     *   <li><em>paletteDirectoryUri</em>: Directory where the NetCDF colour palettes files are stored.
     *     File extension: {@code pal}</li>
     *
     *   <li><em>workingDirectory</em>: Directory used to create temporary files.
     *     If unspecified, {@code /tmp/ncanimate} is used.</li>
     *
     *   <li><em>ncanimateFrameJar</em>: Path to the NcAnimate frame jar file
     *     {@code ereefs-ncanimate2-frame-0.1-jar-with-dependencies.jar}.
     *     If the attribute is missing or represent a non existent file,
     *     NcAnimate will look in the same directory as NcAnimate jar file.
     *     If the path represent a directory, NcAnimate will try to find an
     *     appropriate file in the directory, using the search pattern
     *     {@code ereefs-ncanimate2-frame*-jar-with-dependencies.jar}.</li>
     *
     *   <li><em>definitionId</em>: The ID of the NcAnimate configuration;
     *     i.e. {@code NcAnimateConfigId}</li>
     *
     *   <li><em>scale</em>: The resolution multiplier.
     *     Number used to increase or reduce the output products' resolution.
     *     Manipulating the scale has the same effect as changing all sizing properties such as
     *     {@code width}, {@code height}, {@code position}, {@code padding}, {@code border size},
     *     {@code font size}, etc.</li>
     *
     *   <li><em>timezone</em>: the timezone to used to parse dates and render the output products.
     *     Example: {@code Australia/Brisbane}.
     *     Default: {@code UTC}.
     *     See: <a href="https://www.joda.org/joda-time/timezones.html" target="_blank">list of Joda timezone IDs</a></li>
     *
     *   <li><em>locale</em>: Locale ID used to format dates.
     *     Parsed using {@code java.util.Locale.forLanguageTag(String)}
     *     Default: {@code en}.</li>
     *
     *   <li><em>videoTimeIncrement</em>: the period of time represented by the video output files.</li>
     *
     *   <li><em>frameTimeIncrement</em>: the period of time between each frame of the video output files.
     *     For map files, this is the period of time represented by the map output files.</li>
     *
     *   <li><em>startDate</em>: Optional. Used to restrict how far back input data files should be considered.</li>
     *   <li><em>endDate</em>: Optional. Used to restrict input data files.</li>
     *   <li><em>maps</em>: {@code Map} of serialised {@link NcAnimateRenderMapBean}.</li>
     *   <li><em>videos</em>: {@code Map} of serialised {@link NcAnimateRenderVideoBean}.</li>
     *   <li><em>metadata</em>: serialised {@link NcAnimateRenderMetadataBean}.</li>
     * </ul>
     *
     * @param jsonRender {@code JSONWrapperObject} representing a NcAnimate render bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRenderBean(JSONWrapperObject jsonRender) throws Exception {
        super(jsonRender);
    }

    /**
     * Load the attributes of the NcAnimate render bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonRender {@code JSONWrapperObject} representing a NcAnimate render bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonRender) throws Exception {
        super.parse(jsonRender);
        if (jsonRender != null) {
            this.directoryUri = jsonRender.get(String.class, "directoryUri");
            this.frameDirectoryUri = jsonRender.get(String.class, "frameDirectoryUri");
            this.paletteDirectoryUri = jsonRender.get(String.class, "paletteDirectoryUri");
            this.workingDirectory = jsonRender.get(String.class, "workingDirectory");
            this.ncanimateFrameJar = jsonRender.get(String.class, "ncanimateFrameJar");

            this.definitionId = jsonRender.get(String.class, "definitionId");

            this.scale = jsonRender.get(Float.class, "scale");

            this.timezone = jsonRender.get(String.class, "timezone");
            this.locale = jsonRender.get(String.class, "locale");

            this.videoTimeIncrement = AbstractNcAnimateBean.parseTimeIncrement(
                    jsonRender.get(JSONWrapperObject.class, "videoTimeIncrement"));

            this.frameTimeIncrement = AbstractNcAnimateBean.parseTimeIncrement(
                    jsonRender.get(JSONWrapperObject.class, "frameTimeIncrement"));

            this.startDate = jsonRender.get(String.class, "startDate");
            this.endDate = jsonRender.get(String.class, "endDate");

            this.setMaps(jsonRender.get(JSONWrapperObject.class, "maps"));
            this.setVideos(jsonRender.get(JSONWrapperObject.class, "videos"));

            this.setMetadata(jsonRender.get(JSONWrapperObject.class, "metadata"));
        }
    }

    private void setMaps(JSONWrapperObject jsonMaps) throws Exception {
        Map<String, NcAnimateRenderMapBean> parsedMaps = new HashMap<String, NcAnimateRenderMapBean>();

        if (jsonMaps != null) {
            Set<String> mapIds = jsonMaps.keySet();
            for (String mapId : mapIds) {
                if (mapId != null && !mapId.isEmpty()) {
                    JSONWrapperObject jsonMap = jsonMaps.get(JSONWrapperObject.class, mapId);
                    if (jsonMap != null) {
                        NcAnimateRenderMapBean map = new NcAnimateRenderMapBean(jsonMap);
                        parsedMaps.put(mapId, map);
                    }
                }
            }
        }

        this.maps = parsedMaps.isEmpty() ? null : parsedMaps;
    }

    private void setVideos(JSONWrapperObject jsonVideos) throws Exception {
        Map<String, NcAnimateRenderVideoBean> parsedVideos = new HashMap<String, NcAnimateRenderVideoBean>();

        if (jsonVideos != null) {
            Set<String> videoIds = jsonVideos.keySet();
            for (String videoId : videoIds) {
                if (videoId != null && !videoId.isEmpty()) {
                    JSONWrapperObject jsonVideo = jsonVideos.get(JSONWrapperObject.class, videoId);
                    if (jsonVideo != null) {
                        NcAnimateRenderVideoBean video = new NcAnimateRenderVideoBean(jsonVideo);
                        parsedVideos.put(videoId, video);
                    }
                }
            }
        }

        this.videos = parsedVideos.isEmpty() ? null : parsedVideos;
    }

    private void setMetadata(JSONWrapperObject jsonMetadata) throws Exception {
        this.metadata = null;

        if (jsonMetadata != null) {
            this.metadata = new NcAnimateRenderMetadataBean(jsonMetadata);
        }
    }

    /**
     * Returns the URI of the directory where the rendered files
     * ({@code MAP} and {@code VIDEO}) are stored.
     * Supports protocol {@code s3://} and {@code file://}.
     *
     * @return the directory URI where the rendered files are stored.
     */
    public String getDirectoryUri() {
        return this.directoryUri;
    }

    /**
     * Returns the URI of the directory where the frames
     * ({@code MAP} and {@code VIDEO}) are stored.
     * Video frames are saved if and only if this property is set.
     * It's required for the {@code Download current video frame} feature
     * to work.
     * Supports protocol {@code s3://} and {@code file://}</li>
     *
     * @return the directory URI where the frames are stored.
     */
    public String getFrameDirectoryUri() {
        return this.frameDirectoryUri;
    }

    /**
     * Returns the URI of the directory where the NetCDF colour palettes files are stored.
     * File extension: {@code pal}.
     *
     * @return the directory URI where the NetCDF colour palettes files are stored.
     */
    public String getPaletteDirectoryUri() {
        return this.paletteDirectoryUri;
    }

    /**
     * Returns the path to the directory used to create temporary files.
     * If unspecified, {@code /tmp/ncanimate} is used.
     *
     * @return the directory path used to create temporary files.
     */
    public String getWorkingDirectory() {
        return this.workingDirectory;
    }

    /**
     * Returns the {@code File} to the directory used to create temporary files.
     * If unspecified, {@code /tmp/ncanimate} is used.
     *
     * @return the directory {@code File} used to create temporary files.
     */
    public File getWorkingDirectoryFile() {
        if (this.workingDirectory != null && !this.workingDirectory.isEmpty()) {
            File workingDir = new File(this.workingDirectory);
            if (Utils.prepareDirectory(workingDir)) {
                return workingDir;
            } else {
                LOGGER.warn(String.format("Invalid working directory %s. The directory could not be created. Default working directory will be used: %s",
                    this.workingDirectory,
                    DEFAULT_WORKING_DIRECTORY
                ));
            }
        }

        return DEFAULT_WORKING_DIRECTORY;
    }

    /**
     * Returns the path to the NcAnimate frame jar file
     * {@code ereefs-ncanimate2-frame-0.1-jar-with-dependencies.jar}.
     * If the attribute is missing or represent a non existent file,
     * NcAnimate will look in the same directory as NcAnimate jar file.
     * If the path represent a directory, NcAnimate will try to find an
     * appropriate file in the directory, using the search pattern
     * {@code ereefs-ncanimate2-frame*-jar-with-dependencies.jar}.
     *
     * @return the path to the NcAnimate frame jar file.
     */
    public String getNcanimateFrameJar() {
        return this.ncanimateFrameJar;
    }

    /**
     * Returns the ID of the NcAnimate configuration;
     * i.e. {@code NcAnimateConfigId}.
     *
     * @return the NcAnimate definition ID.
     */
    public String getDefinitionId() {
        return this.definitionId;
    }

    /**
     * Returns the resolution multiplier.
     * @return the resolution multiplier.
     */
    public float getScale() {
        return this.scale == null || this.scale <= 0 ? DEFAULT_SCALE : this.scale;
    }

    /**
     * Returns the timezone to used to parse dates and render the output products.
     * @return the timezone.
     */
    public String getTimezone() {
        return this.timezone;
    }

    /**
     * Returns Joda {@code DateTimeZone} timezone object used
     * to parse dates and render the output products.
     *
     * @return the Joda {@code DateTimeZone} timezone object.
     */
    public DateTimeZone getDateTimeZone() {
        if (this.timezone != null && !this.timezone.isEmpty()) {
            try {
                return DateTimeZone.forID(this.timezone);
            } catch(Exception ex) {
                LOGGER.error(String.format("Invalid timezone ID: %s, using default timezone: %s", this.timezone, DEFAULT_DATETIMEZONE.getID()));
            }
        }

        return DEFAULT_DATETIMEZONE;
    }

    /**
     * Returns the locale ID used to format dates.
     * @return the locale ID used to format dates.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Returns the {@code java.util.Locale} object used to format dates.
     * @return the {@code java.util.Locale} object used to format dates.
     */
    public Locale getLocaleObject() {
        if (this.locale != null && !this.locale.isEmpty()) {
            try {
                return Locale.forLanguageTag(this.locale);
            } catch(Exception ex) {
                LOGGER.error(String.format("Invalid locale tag: %s, using default locale: %s", this.locale, DEFAULT_LOCALE.getLanguage()));
            }
        }

        return DEFAULT_LOCALE;
    }

    /**
     * Returns the period of time represented by the video output files.
     * @return the video time increment.
     */
    public TimeIncrement getVideoTimeIncrement() {
        return this.videoTimeIncrement;
    }

    /**
     * Returns the period of time between each frame of the video output files.
     * For map files, returns the period of time represented by the map output files.
     *
     * @return the frame time increment.
     */
    public TimeIncrement getFrameTimeIncrement() {
        return this.frameTimeIncrement;
    }

    /**
     * Returns the start date.
     * @return the start date.
     */
    public String getStartDate() {
        return this.startDate;
    }

    /**
     * Returns the end date.
     * @return the end date.
     */
    public String getEndDate() {
        return this.endDate;
    }

    /**
     * Returns the {@code Map} of {@link NcAnimateRenderMapBean}.
     * @return the {@code Map} of {@link NcAnimateRenderMapBean}.
     */
    public Map<String, NcAnimateRenderMapBean> getMaps() {
        return this.maps;
    }

    /**
     * Returns the {@code Map} of {@link NcAnimateRenderVideoBean}.
     * @return the {@code Map} of {@link NcAnimateRenderVideoBean}.
     */
    public Map<String, NcAnimateRenderVideoBean> getVideos() {
        return this.videos;
    }

    /**
     * Returns the {@link NcAnimateRenderMetadataBean}.
     * @return the {@link NcAnimateRenderMetadataBean}.
     */
    public NcAnimateRenderMetadataBean getMetadata() {
        return this.metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("directoryUri", this.directoryUri);
        json.put("frameDirectoryUri", this.frameDirectoryUri);
        json.put("paletteDirectoryUri", this.paletteDirectoryUri);
        json.put("workingDirectory", this.workingDirectory);
        json.put("ncanimateFrameJar", this.ncanimateFrameJar);
        json.put("scale", this.scale);
        json.put("definitionId", this.definitionId);
        json.put("timezone", this.timezone);
        json.put("locale", this.locale);

        if (this.videoTimeIncrement != null) {
            json.put("videoTimeIncrement", this.videoTimeIncrement.toJSON());
        }
        if (this.frameTimeIncrement != null) {
            json.put("frameTimeIncrement", this.frameTimeIncrement.toJSON());
        }

        json.put("startDate", this.startDate);
        json.put("endDate", this.endDate);

        if (this.maps != null) {
            JSONObject jsonMaps = new JSONObject();
            for (Map.Entry<String, NcAnimateRenderMapBean> mapEntry : this.maps.entrySet()) {
                NcAnimateRenderMapBean map = mapEntry.getValue();
                if (map != null) {
                    jsonMaps.put(mapEntry.getKey(), map.toJSON());
                }
            }
            json.put("maps", jsonMaps);
        }

        if (this.videos != null) {
            JSONObject jsonVideos = new JSONObject();
            for (Map.Entry<String, NcAnimateRenderVideoBean> videoEntry : this.videos.entrySet()) {
                NcAnimateRenderVideoBean video = videoEntry.getValue();
                if (video != null) {
                    jsonVideos.put(videoEntry.getKey(), video.toJSON());
                }
            }
            json.put("videos", jsonVideos);
        }

        if (this.metadata != null) {
            json.put("metadata", this.metadata.toJSON());
        }

        return json.isEmpty() ? null : json;
    }
}
