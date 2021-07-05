/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONObject;
import ucar.nc2.Variable;
import uk.ac.rdg.resc.edal.dataset.plugins.VectorPlugin;
import uk.ac.rdg.resc.edal.domain.HorizontalDomain;
import uk.ac.rdg.resc.edal.domain.TemporalDomain;
import uk.ac.rdg.resc.edal.domain.VerticalDomain;
import uk.ac.rdg.resc.edal.metadata.Parameter;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean representing a variable of a NetCDF metadata.
 * It's part of the {@link NetCDFMetadataBean} which is used with
 * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 */
public class VariableMetadataBean extends AbstractBean {
    private String id = null;
    private ParameterBean parameterBean = null;
    private HorizontalDomainBean horizontalDomainBean = null;
    private VerticalDomainBean verticalDomainBean = null;
    private TemporalDomainBean temporalDomainBean = null;
    private Boolean scalar = null;

    private String parentId = null;
    private String role = null;

    private JSONObject attributes;

    private VariableMetadataBean parent = null;
    // Key: role ("mag", "dir", etc)
    private Map<String, VariableMetadataBean> children;

    /**
     * Construct a {@code VariableMetadataBean} from a EDAL {@code VariableMetadata} object
     * and a UCAR {@code Variable} object.
     * Used when parsing the metadata returned by the UCAR library.
     *
     * @param variableMetadata EDAL VariableMetadata object.
     * @param variable UCAR Variable object.
     */
    public VariableMetadataBean(VariableMetadata variableMetadata, Variable variable) {
        if (variableMetadata == null) {
            throw new IllegalArgumentException("VariableMetadata parameter is null.");
        }

        this.children = new HashMap<String, VariableMetadataBean>();

        this.id = variableMetadata.getId();

        Parameter parameter = variableMetadata.getParameter();
        if (parameter != null) {
            this.parameterBean = new ParameterBean(parameter);
        }

        HorizontalDomain horizontalDomain = variableMetadata.getHorizontalDomain();
        if (horizontalDomain != null) {
            this.horizontalDomainBean = new HorizontalDomainBean(horizontalDomain);
        }

        VerticalDomain verticalDomain = variableMetadata.getVerticalDomain();
        if (verticalDomain != null) {
            this.verticalDomainBean = new VerticalDomainBean(verticalDomain);
        }

        TemporalDomain temporalDomain = variableMetadata.getTemporalDomain();
        if (temporalDomain != null) {
            this.temporalDomainBean = new TemporalDomainBean(temporalDomain);
        }

        this.scalar = variableMetadata.isScalar();

        VariableMetadata parent = variableMetadata.getParent();
        if (parent != null) {
            this.parentId = parent.getId();
        }

        // Find the variable role (see: VectorPlugin.doProcessVariableMetadata)
        //   MAG_ROLE, DIR_ROLE, "x", "y" or null
        this.role = VariableMetadataBean.getVariableRole(variableMetadata);

        this.attributes = this.loadAttributes(variable);
    }

    /**
     * Construct a {@code VariableMetadataBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonVariableMetadata JSON serialised VariableMetadataBean.
     */
    public VariableMetadataBean(JSONObject jsonVariableMetadata) {
        if (jsonVariableMetadata == null) {
            throw new IllegalArgumentException("JSONObject parameter is null.");
        }

        this.children = new HashMap<String, VariableMetadataBean>();

        this.id = jsonVariableMetadata.optString("id", null);

        JSONObject jsonParameter = jsonVariableMetadata.optJSONObject("parameter");
        if (jsonParameter != null) {
            this.parameterBean = new ParameterBean(jsonParameter);
        }

        JSONObject jsonHorizontalDomain = jsonVariableMetadata.optJSONObject("horizontalDomain");
        if (jsonHorizontalDomain != null) {
            this.horizontalDomainBean = new HorizontalDomainBean(jsonHorizontalDomain);
        }

        JSONObject jsonVerticalDomain = jsonVariableMetadata.optJSONObject("verticalDomain");
        if (jsonVerticalDomain != null) {
            this.verticalDomainBean = new VerticalDomainBean(jsonVerticalDomain);
        }

        JSONObject jsonTemporalDomain = jsonVariableMetadata.optJSONObject("temporalDomain");
        if (jsonTemporalDomain != null) {
            this.temporalDomainBean = new TemporalDomainBean(jsonTemporalDomain);
        }

        if (jsonVariableMetadata.has("scalar")) {
            this.scalar = jsonVariableMetadata.optBoolean("scalar");
        }

        this.attributes = jsonVariableMetadata.optJSONObject("attributes");

        this.parentId = jsonVariableMetadata.optString("parentId", null);
        this.role = jsonVariableMetadata.optString("role", null);
    }

    private JSONObject loadAttributes(Variable variable) {
        if (variable == null) {
            return null;
        }

        return NetCDFMetadataBean.loadAttributes(variable.getAttributes());
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject jsonVariableMetadata = new JSONObject();

        jsonVariableMetadata.put("id", this.id);

        if (this.parameterBean != null) {
            jsonVariableMetadata.put("parameter", this.parameterBean.toJSON());
        }

        if (this.horizontalDomainBean != null) {
            jsonVariableMetadata.put("horizontalDomain", this.horizontalDomainBean.toJSON());
        }

        if (this.verticalDomainBean != null) {
            jsonVariableMetadata.put("verticalDomain", this.verticalDomainBean.toJSON());
        }

        if (this.temporalDomainBean != null) {
            jsonVariableMetadata.put("temporalDomain", this.temporalDomainBean.toJSON());
        }

        jsonVariableMetadata.put("scalar", this.scalar);

        if (this.parent != null) {
            jsonVariableMetadata.put("parentId", this.parent.getId());
        }

        jsonVariableMetadata.put("role", this.role);

        if (this.attributes != null && !this.attributes.isEmpty()) {
            jsonVariableMetadata.put("attributes", this.attributes);
        }

        return jsonVariableMetadata;
    }

    // There is no getter for the variable role (see: VectorPlugin.doProcessVariableMetadata)
    private static String getVariableRole(VariableMetadata variableMetadata) {
        if (variableMetadata == null) {
            return null;
        }

        String variableId = variableMetadata.getId();
        if (variableId == null) {
            return null;
        }

        VariableMetadata parent = variableMetadata.getParent();
        if (parent == null) {
            return null;
        }

        VariableMetadata directionVariableMetadata = parent.getChildWithRole(VectorPlugin.DIR_ROLE);
        if (directionVariableMetadata != null && variableId.equals(directionVariableMetadata.getId())) {
            return VectorPlugin.DIR_ROLE;
        }

        VariableMetadata magnitudeVariableMetadata = parent.getChildWithRole(VectorPlugin.MAG_ROLE);
        if (magnitudeVariableMetadata != null && variableId.equals(magnitudeVariableMetadata.getId())) {
            return VectorPlugin.MAG_ROLE;
        }

        VariableMetadata xVariableMetadata = parent.getChildWithRole("x");
        if (xVariableMetadata != null && variableId.equals(xVariableMetadata.getId())) {
            return "x";
        }

        VariableMetadata yVariableMetadata = parent.getChildWithRole("y");
        if (yVariableMetadata != null && variableId.equals(yVariableMetadata.getId())) {
            return "y";
        }

        return null;
    }

    protected void setParent(VariableMetadataBean parent) {
        this.parent = parent;
        this.parent.children.put(this.role, this);
    }

    /**
     * Returns the {@code VariableMetadataBean} ID.
     * @return the {@code VariableMetadataBean} ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the {@code VariableMetadataBean} parameter.
     * @return the {@code VariableMetadataBean} parameter.
     */
    public ParameterBean getParameterBean() {
        return this.parameterBean;
    }

    /**
     * Returns the {@code VariableMetadataBean} horizontal domain.
     * @return the {@code VariableMetadataBean} horizontal domain.
     */
    public HorizontalDomainBean getHorizontalDomainBean() {
        return this.horizontalDomainBean;
    }

    /**
     * Returns the {@code VariableMetadataBean} vertical domain.
     * @return the {@code VariableMetadataBean} vertical domain.
     */
    public VerticalDomainBean getVerticalDomainBean() {
        return this.verticalDomainBean;
    }

    /**
     * Returns the {@code VariableMetadataBean} temporal domain.
     * @return the {@code VariableMetadataBean} temporal domain.
     */
    public TemporalDomainBean getTemporalDomainBean() {
        return this.temporalDomainBean;
    }

    /**
     * Returns {@code true} if the {@code VariableMetadataBean} is scalar variable.
     * {@code false} if it's a vector variable.
     *
     * @return {@code true} if the {@code VariableMetadataBean} is scalar variable.
     */
    public Boolean isScalar() {
        return this.scalar;
    }

    /**
     * Returns the {@code VariableMetadataBean} parent variable ID.
     * @return the {@code VariableMetadataBean} parent variable ID.
     */
    protected String getParentId() {
        return this.parentId;
    }

    /**
     * Returns the {@code VariableMetadataBean} parent variable.
     * @return the {@code VariableMetadataBean} parent variable.
     */
    public VariableMetadataBean getParent() {
        return this.parent;
    }

    /**
     * Returns the {@code VariableMetadataBean} map of children variables.
     * @return the {@code VariableMetadataBean} map of children variables.
     */
    public Map<String, VariableMetadataBean> getChildren() {
        return this.children;
    }

    /**
     * Returns the {@code VariableMetadataBean} role.
     * @return the {@code VariableMetadataBean} role.
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Returns the tree of attributes for the {@code VariableMetadataBean}.
     * @return the tree of attributes.
     */
    public JSONObject getAttributes() {
        return this.attributes;
    }
}
