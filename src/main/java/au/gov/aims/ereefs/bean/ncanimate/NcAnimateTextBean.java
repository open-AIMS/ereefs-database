/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * NcAnimate text bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateCanvasBean},
 * {@link NcAnimatePanelBean} and in {@link NcAnimateLegendBean}.</p>
 *
 * <p>Used to style and positioned a string of text.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "text": "${ctx.region.label}",
 *     "fontSize": 25,
 *     "position": {
 *         "top": 28,
 *         "left": 16
 *     },
 *     "bold": true,
 *     "italic": false
 * }</pre>
 */
public class NcAnimateTextBean extends AbstractNcAnimateBean {
    private List<String> text; // Text to display. If patterns on the first one is not defined, use the next one and so on.
    private Integer fontSize;
    private String fontColour;
    private Boolean bold;
    private Boolean italic;
    private NcAnimatePositionBean position;

    /**
     * Create a NcAnimate text bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>fontSize</em>: the font size. Default: 20.</li>
     *   <li><em>fontColour</em>: the colour of the text. Default: #000000.</li>
     *   <li><em>bold</em>: {@code true} to render the text in bold. Default: {@code false}</li>
     *   <li><em>italic</em>: {@code true} to render the text in italic. Default: {@code false}</li>
     *   <li><em>position</em>: {@link NcAnimatePositionBean} defining the position
     *       where the text should be rendered.</li>
     *   <li><em>text</em>: text to render. Can be specified as a string or an array of string.
     *       If an array of string is provided, NcAnimate will render the first text if all
     *       placeholders pattern defined are found. If not, it skip to the next one and so on.
     *   </li>
     * </ul>
     *
     * @param jsonText {@code JSONWrapperObject} representing a NcAnimate text bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateTextBean(JSONWrapperObject jsonText) throws Exception {
        super(jsonText);
    }

    /**
     * Create a NcAnimate text bean part from an ID and a {@code JSONWrapperObject}.
     *
     * <p>See {@link #NcAnimateTextBean(JSONWrapperObject)}.</p>
     *
     * @param id the NcAnimate text bean part ID.
     * @param jsonText {@code JSONWrapperObject} representing a NcAnimate text bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateTextBean(String id, JSONWrapperObject jsonText) throws Exception {
        super(new NcAnimateIdBean(ConfigPartManager.Datatype.TEXT, id), jsonText);
    }

    /**
     * Load the attributes of the NcAnimate text bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonText {@code JSONWrapperObject} representing a NcAnimate text bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonText) throws Exception {
        super.parse(jsonText);
        if (jsonText != null) {
            this.fontSize = jsonText.get(Integer.class, "fontSize");
            this.fontColour = jsonText.get(String.class, "fontColour");
            this.bold = jsonText.get(Boolean.class, "bold");
            this.italic = jsonText.get(Boolean.class, "italic");
            this.setPosition(jsonText.get(JSONWrapperObject.class, "position"));
            this.setText(jsonText);
        }
    }

    private void setPosition(JSONWrapperObject jsonPosition) throws Exception {
        this.position = null;

        if (jsonPosition != null) {
            this.position = new NcAnimatePositionBean(jsonPosition);
        }
    }

    private void setText(JSONWrapperObject jsonText) throws InvalidClassException {
        this.text = null;

        if (jsonText.has("text")) {
            this.text = new ArrayList<String>();
            Class textsClass = jsonText.getClass("text");
            if (String.class.equals(textsClass)) {
                String textLine = jsonText.get(String.class, "text");
                if (textLine != null) {
                    this.text.add(textLine);
                }
            } else if (JSONWrapperArray.class.equals(textsClass)) {
                JSONWrapperArray jsonArray = jsonText.get(JSONWrapperArray.class, "text");
                if (jsonArray != null) {
                    for (int i=0; i<jsonArray.length(); i++) {
                        String textLine = jsonArray.get(String.class, i);
                        if (textLine != null) {
                            this.text.add(textLine);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the list of text.
     * @return the list of text.
     */
    public List<String> getText() {
        return this.text;
    }

    /**
     * Returns the font size.
     * @return the font size.
     */
    public Integer getFontSize() {
        return this.fontSize;
    }

    /**
     * Returns the colour to use to render the text.
     * @return the text colour.
     */
    public String getFontColour() {
        return this.fontColour;
    }

    /**
     * Returns {@code true} if the text should be rendered bold; {@code false} otherwise.
     * @return {@code true} if the text should be rendered bold.
     */
    public Boolean getBold() {
        return this.bold;
    }

    /**
     * Returns {@code true} if the text should be rendered italic; {@code false} otherwise.
     * @return {@code true} if the text should be rendered italic.
     */
    public Boolean getItalic() {
        return this.italic;
    }

    /**
     * Returns the position of the text.
     * @return the position of the text.
     */
    public NcAnimatePositionBean getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        json.put("fontSize", this.fontSize);
        json.put("fontColour", this.fontColour);
        json.put("bold", this.bold);
        json.put("italic", this.italic);
        if (this.position != null) {
            json.put("position", this.position.toJSON());
        }
        if (this.text != null && !this.text.isEmpty()) {
            JSONArray jsonTexts = new JSONArray();
            for (String textLine : this.text) {
                jsonTexts.put(textLine);
            }
            json.put("text", jsonTexts);
        }

        return json.isEmpty() ? null : json;
    }
}
