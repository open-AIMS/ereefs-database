/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate.render;

import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * NcAnimate render video bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateRenderBean}.</p>
 *
 * <p>Used to defined how to render a video file.</p>
 *
 * <p>It can generate the following file formats:</p>
 * <ul>
 *     <li><em>MP4</em>: <a href="https://en.wikipedia.org/wiki/MPEG-4_Part_14" target="_blank">MPEG-4 Part 14</a> video format.
 *         Portable video format which renders well in modern web browsers.
 *     </li>
 *     <li><em>WMV</em>: <a href="https://en.wikipedia.org/wiki/Windows_Media_Video" target="_blank">Windows Media Video</a> format.
 *         Alternative video format, which can be provided as a downloadable file.
 *         WMV files are easier to integrate in PowerPoint presentations than MP4 video files.
 *     </li>
 *     <li><em>ZIP</em>: <a href="https://en.wikipedia.org/wiki/ZIP_(file_format)" target="_blank">Archive file format</a>.
 *         NcAnimate can also generate a ZIP archive containing all the video frames,
 *         which can be provided as a downloadable file.<br/>
 *         WARNING: File archive can be surprisingly large. It's recommended to make
 *         each video frame available for download instead. Using JavaScript,
 *         a client can detect which frame is currently displayed and provide a download link
 *         to that frame.
 *         See {@link NcAnimateRenderBean#getFrameDirectoryUri()} for more details.
 *     </li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "format": "MP4",
 *     "fps": 15,
 *     "blockSize": [16, 16],
 *     "commandLines": [
 *         "/usr/bin/ffmpeg -y -r \"${ctx.renderFile.fps}\" -i \"${ctx.videoFrameDirectory}/${ctx.frameFilenamePrefix}_%05d.png\" -vcodec libx264 -profile:v baseline -pix_fmt yuv420p -crf 29 -vf \"pad=${ctx.productWidth}:${ctx.productHeight}:${ctx.padding.left}:${ctx.padding.top}:white\" \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\"",
 *         "/usr/bin/qt-faststart \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\" \"${ctx.outputFile}\"",
 *         "rm \"${ctx.outputDirectory}/temp_${ctx.outputFilename}\""
 *     ]
 * }</pre>
 */
public class NcAnimateRenderVideoBean extends AbstractNcAnimateRenderFileBean {
    private static final Logger LOGGER = Logger.getLogger(NcAnimateRenderVideoBean.class);

    private VideoFormat format;

    private Integer[] blockSize;
    private Integer fps;
    private List<String> commandLines;

    /**
     * Create a NcAnimate render video bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>format</em>: the file format to generate.
     *     <p>Example: {@code MP4}</p></li>
     *   <li><em>blockSize</em>: the video block size. Used with some low level MPEG baseline profiles.
     *     Provided as a {@code JSONArray}, First element is the block size width,
     *     second element is the block size height.
     *     <p>Example: {@code [16, 16]}</p>
     *     <p>See <a href="https://en.wikipedia.org/wiki/Advanced_Video_Coding#Features" target="_blank">https://en.wikipedia.org/wiki/Advanced_Video_Coding#Features</a>
     *     for more information.</p></li>
     *   <li><em>fps</em>: the video frame per second.</li>
     *   <li><em>commandLines</em>: list of command lines used to generate the video.</li>
     * </ul>
     *
     * <p>See {@link AbstractNcAnimateRenderFileBean#AbstractNcAnimateRenderFileBean(JSONWrapperObject)}
     *   for inherited attributes.</p>
     *
     * @param jsonRenderVideo {@code JSONWrapperObject} representing a NcAnimate render video bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRenderVideoBean(JSONWrapperObject jsonRenderVideo) throws Exception {
        super(jsonRenderVideo);
    }

    /**
     * Load the attributes of the NcAnimate render video bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonRenderVideo {@code JSONWrapperObject} representing a NcAnimate render video bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonRenderVideo) throws Exception {
        super.parse(jsonRenderVideo);
        if (jsonRenderVideo != null) {
            this.setFormat(jsonRenderVideo.get(String.class, "format"));
            this.setBlockSize(jsonRenderVideo.get(JSONWrapperArray.class, "blockSize"));
            this.fps = jsonRenderVideo.get(Integer.class, "fps");
            this.setCommandLines(jsonRenderVideo.get(JSONWrapperArray.class, "commandLines"));
        }
    }

    private void setFormat(String formatStr) {
        this.format = null;

        if (formatStr != null && !formatStr.isEmpty()) {
            try {
                this.format = VideoFormat.valueOf(formatStr.toUpperCase());
            } catch (Exception ex) {
                LOGGER.warn("Invalid video format: " + formatStr, ex);
            }
        }
    }

    private void setBlockSize(JSONWrapperArray jsonBlockSize) throws InvalidClassException {
        this.blockSize = null;

        if (jsonBlockSize != null) {
            List<Integer> blockSizeList = new ArrayList<Integer>();
            for (int i=0; i<jsonBlockSize.length(); i++) {
                Integer blockSizeValue = jsonBlockSize.get(Integer.class, i);
                if (blockSizeValue != null) {
                    blockSizeList.add(blockSizeValue);
                }
            }

            if (!blockSizeList.isEmpty()) {
                this.blockSize = blockSizeList.toArray(new Integer[0]);
            }
        }
    }

    private void setCommandLines(JSONWrapperArray jsonCommandLines) throws InvalidClassException {
        List<String> parsedCommandLines = new ArrayList<String>();

        if (jsonCommandLines != null) {
            for (int i=0; i<jsonCommandLines.length(); i++) {
                String commandLine = jsonCommandLines.get(String.class, i);
                if (commandLine != null && !commandLine.isEmpty()) {
                    parsedCommandLines.add(commandLine);
                }
            }
        }

        this.commandLines = parsedCommandLines.isEmpty() ? null : parsedCommandLines;
    }

    /**
     * Returns the video format.
     * @return the video format.
     */
    public VideoFormat getFormat() {
        return this.format;
    }

    /**
     * Returns the video block size.
     * @return the video block size.
     */
    public Integer[] getBlockSize() {
        return this.blockSize;
    }

    /**
     * Returns the video frame per second.
     * @return the video frame per second.
     */
    public Integer getFps() {
        return this.fps;
    }

    /**
     * Returns the list of command lines to execute to generate the video.
     * @return the list of command lines to execute to generate the video.
     */
    public List<String> getCommandLines() {
        return this.commandLines;
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
        return RenderFileType.VIDEO;
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

        if (this.blockSize != null) {
            json.put("blockSize", new JSONArray(this.blockSize));
        }
        json.put("fps", this.fps);
        if (commandLines != null) {
            json.put("commandLines", this.commandLines);
        }

        return json.isEmpty() ? null : json;
    }

    /**
     * List of supported video file format.
     */
    public enum VideoFormat {
        WMV("wmv"),
        MP4("mp4"),
        ZIP("zip");

        private String extension;

        private VideoFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return this.extension;
        }

        public static VideoFormat fromExtension(String extension) {
            if (extension == null) {
                return null;
            }

            for (VideoFormat videoFormat : VideoFormat.values()) {
                if (extension.equalsIgnoreCase(videoFormat.extension)) {
                    return videoFormat;
                }
            }

            return null;
        }
    }
}
