/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONObject;
import uk.ac.rdg.resc.edal.metadata.Parameter;

/**
 * Bean representing a category of a parameter.
 * It's used with the {@link ParameterBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class CategoryBean extends AbstractBean {
    private String id;
    private String label;
    private String colour;
    private String description;

    /**
     * Construct a {@code CategoryBean} from a EDAL {@code Parameter.Category} object.
     * Used when parsing the metadata returned by the UCAR library.
     * @param category EDAL Parameter.Category object.
     */
    public CategoryBean(Parameter.Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category parameter is null.");
        }

        this.id = category.getId();
        this.label = category.getLabel();
        this.colour = category.getColour();
        this.description = category.getDescription();
    }

    /**
     * Construct a {@code CategoryBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     * @param jsonCategory JSON serialised CategoryBean.
     */
    public CategoryBean(JSONObject jsonCategory) {
        if (jsonCategory == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.id = jsonCategory.optString("id", null);
        this.label = jsonCategory.optString("label", null);
        this.colour = jsonCategory.optString("colour", null);
        this.description = jsonCategory.optString("description", null);
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonCategory = new JSONObject();

        jsonCategory.put("id", this.id);
        jsonCategory.put("label", this.label);
        jsonCategory.put("colour", this.colour);
        jsonCategory.put("description", this.description);

        return jsonCategory;
    }

    /**
     * Returns the {@code CategoryBean} ID.
     * @return the {@code CategoryBean} ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the {@code CategoryBean} label.
     * @return the {@code CategoryBean} label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the {@code CategoryBean} colour.
     * @return the {@code CategoryBean} colour.
     */
    public String getColour() {
        return this.colour;
    }

    /**
     * Returns the {@code CategoryBean} description.
     * @return the {@code CategoryBean} description.
     */
    public String getDescription() {
        return this.description;
    }
}
