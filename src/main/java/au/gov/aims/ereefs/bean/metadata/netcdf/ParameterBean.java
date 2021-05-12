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
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONObject;
import uk.ac.rdg.resc.edal.metadata.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean representing the parameter of a variable.
 * It's used with the {@link VariableMetadataBean}.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class ParameterBean extends AbstractBean {
    private String variableId;
    private String title;
    private String description;
    private String units;
    private String standardName;

    private Map<Integer, CategoryBean> categoryBeanMap = null;

    /**
     * Construct a {@code ParameterBean} from a EDAL {@code Parameter} object.
     * Used when parsing the metadata returned by the UCAR library.
     *
     * @param parameter EDAL Parameter object.
     */
    public ParameterBean(Parameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter parameter is null.");
        }

        this.variableId = parameter.getVariableId();
        this.title = parameter.getTitle();
        this.description = parameter.getDescription();
        this.units = parameter.getUnits();
        this.standardName = parameter.getStandardName();

        Map<Integer, Parameter.Category> categories = parameter.getCategories();
        if (categories != null && !categories.isEmpty()) {
            this.categoryBeanMap = new HashMap<Integer, CategoryBean>();
            for (Map.Entry<Integer, Parameter.Category> categoryEntry : categories.entrySet()) {
                Integer key = categoryEntry.getKey();
                Parameter.Category category = categoryEntry.getValue();
                if (key != null && category != null) {
                    this.categoryBeanMap.put(key, new CategoryBean(categoryEntry.getValue()));
                }
            }
        }
    }

    /**
     * Construct a {@code ParameterBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonParameter JSON serialised ParameterBean.
     */
    public ParameterBean(JSONObject jsonParameter) {
        if (jsonParameter == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.variableId = jsonParameter.optString("variableId", null);
        this.title = jsonParameter.optString("title", null);
        this.description = jsonParameter.optString("description", null);
        this.units = jsonParameter.optString("units", null);
        this.standardName = jsonParameter.optString("standardName", null);

        if (jsonParameter.has("categories")) {
            this.categoryBeanMap = new HashMap<Integer, CategoryBean>();

            JSONObject jsonCategories = jsonParameter.optJSONObject("categories");
            for (String key : jsonCategories.keySet()) {
                if (key != null) {
                    Integer intKey = Integer.parseInt(key);
                    CategoryBean category = new CategoryBean(jsonCategories.optJSONObject(key));
                    this.categoryBeanMap.put(intKey, category);
                }
            }
        }
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     *
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonParameter = new JSONObject();

        jsonParameter.put("variableId", this.variableId);
        jsonParameter.put("title", this.title);
        jsonParameter.put("description", this.description);
        jsonParameter.put("units", this.units);
        jsonParameter.put("standardName", this.standardName);

        if (this.categoryBeanMap != null && !this.categoryBeanMap.isEmpty()) {
            JSONObject jsonCategories = new JSONObject();

            for (Map.Entry<Integer, CategoryBean> categoryEntry : this.categoryBeanMap.entrySet()) {
                Integer key = categoryEntry.getKey();
                CategoryBean category = categoryEntry.getValue();
                if (key != null && category != null) {
                    jsonCategories.put(key.toString(), category.toJSON());
                }
            }

            jsonParameter.put("categories", jsonCategories);
        }

        return jsonParameter;
    }

    /**
     * Returns the {@code ParameterBean} variable ID.
     * @return the {@code ParameterBean} variable ID.
     */
    public String getVariableId() {
        return this.variableId;
    }

    /**
     * Returns the {@code ParameterBean} title.
     * @return the {@code ParameterBean} title.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the {@code ParameterBean} description.
     * @return the {@code ParameterBean} description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the {@code ParameterBean} units.
     * @return the {@code ParameterBean} units.
     */
    public String getUnits() {
        return this.units;
    }

    /**
     * Returns the {@code ParameterBean} standardName.
     * @return the {@code ParameterBean} standardName.
     */
    public String getStandardName() {
        return this.standardName;
    }

    /**
     * Returns the {@code Map} of {@code CategoryBean}.
     * @return the {@code Map} of {@code CategoryBean}.
     */
    public Map<Integer, CategoryBean> getCategoryBeanMap() {
        return this.categoryBeanMap;
    }
}
