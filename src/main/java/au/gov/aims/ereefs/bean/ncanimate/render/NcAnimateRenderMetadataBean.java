/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.ncanimate.render;

import au.gov.aims.ereefs.bean.ncanimate.AbstractNcAnimateBean;
import au.gov.aims.json.JSONWrapperAbstract;
import au.gov.aims.json.JSONWrapperArray;
import au.gov.aims.json.JSONWrapperObject;
import org.json.JSONObject;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NcAnimate render metadata bean part.
 *
 * <p>This NcAnimate configuration part is used in {@link NcAnimateRenderBean}.</p>
 *
 * <p>Used to group products ({@code MAP} and {@code VIDEO}) in the client.</p>
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "properties": {
 *         "targetHeight": "${ctx.targetHeight}"
 *     }
 * }</pre>
 *
 * <p>NOTE: Adding this metadata to a render bean would tell NcAnimate to add
 *     something like this in the product metadata: {@code "targetHeight": "-1.5"}
 *     or {@code "targetHeight": "-8.8"}.
 *     The client can then group or filter products ({@code MAP} and {@code VIDEO})
 *     by their {@code targetHeight} property.</p>
 */
public class NcAnimateRenderMetadataBean extends AbstractNcAnimateBean {
    private Map<String, Object> properties;

    /**
     * Create a NcAnimate render metadata bean part from a {@code JSONWrapperObject}.
     *
     * <p>Allowed attributes:</p>
     * <ul>
     *   <li><em>properties</em>: {@code JSONObject} containing
     *     an arbitrary set of properties to add to the metadata
     *     of files generated by NcAnimate.</li>
     * </ul>
     *
     * @param json {@code JSONWrapperObject} representing a NcAnimate render metadata bean part.
     * @throws Exception if the json object is malformed.
     */
    public NcAnimateRenderMetadataBean(JSONWrapperObject json) throws Exception {
        super(json);
    }

    /**
     * Load the attributes of the NcAnimate render metadata bean part from a {@code JSONWrapperObject}.
     *
     * @param jsonMetadata {@code JSONWrapperObject} representing a NcAnimate render metadata bean part.
     * @throws Exception if the json object is malformed.
     */
    @Override
    protected void parse(JSONWrapperObject jsonMetadata) throws Exception {
        super.parse(jsonMetadata);
        if (jsonMetadata != null) {
            this.setProperties(jsonMetadata.get(JSONWrapperObject.class, "properties"));
        }
    }

    private void setProperties(JSONWrapperObject jsonProperties) throws Exception {
        Map<String, Object> parsedProperties = new HashMap<String, Object>();

        if (jsonProperties != null) {
            Set<String> propertyIds = jsonProperties.keySet();
            for (String propertyId : propertyIds) {
                if (propertyId != null && !propertyId.isEmpty()) {
                    parsedProperties.put(propertyId, this.parseProperty(jsonProperties, propertyId));
                }
            }
        }

        this.properties = parsedProperties.isEmpty() ? null : parsedProperties;
    }

    private Object parseProperty(JSONWrapperAbstract jsonProperties, Object key) throws InvalidClassException {
        Class propertyClass = jsonProperties.getClass(key);

        // This is equivalent to "if (propertyClass instanceof String)",
        // if propertyClass was an instance of the class that it represent.
        if (String.class.isAssignableFrom(propertyClass)) {
            return jsonProperties.get(String.class, key);

        } else if (JSONWrapperObject.class.isAssignableFrom(propertyClass)) {
            JSONWrapperObject jsonValue = (JSONWrapperObject)jsonProperties.get(JSONWrapperObject.class, key);

            Map<String, Object> parsedValue = new HashMap<String, Object>();
            Set<String> valueKeys = jsonValue.keySet();
            for (String valueKey : valueKeys) {
                parsedValue.put(valueKey, this.parseProperty(jsonValue, valueKey));
            }

            return parsedValue;

        } else if (JSONWrapperArray.class.isAssignableFrom(propertyClass)) {
            JSONWrapperArray jsonValue = (JSONWrapperArray)jsonProperties.get(JSONWrapperArray.class, key);

            List<Object> parsedValue = new ArrayList<Object>();
            for (int i=0; i<jsonValue.length(); i++) {
                parsedValue.add(this.parseProperty(jsonValue, i));
            }

            return parsedValue;
        }

        return null;
    }

    /**
     * Returns the {@code Map} of metadata attributes to add to metadata of every generated files.
     * @return the {@code Map} of metadata attributes to add to metadata of every generated files.
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();

        if (this.properties != null && !this.properties.isEmpty()) {
            json.put("properties", this.properties);
        }

        return json.isEmpty() ? null : json;
    }
}
