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
package au.gov.aims.ereefs.bean.task;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a simple bean, used with the {@code ereefs-job-planner} project.
 * It represent a task that needs to be executed on the AWS infrastructure.
 * The bean define a sub-set of the possible attributes that can be found
 * in a {@code task}.
 *
 * <p>Example:</p>
 * <pre class="code">
 * {
 *     "_id" : "NcAnimateTask_00000000-1111-2222-3333-444444444444",
 *     "type" : "ncanimate",
 *     "jobId" : "Job_00000000-aaaa-5555-ffff-0123456789ab",
 *     "productDefinitionId" : "products__ncanimate__ereefs__gbr4_bgc_924__din_dip_dic_monthly",
 *     "status" : "SUCCESS",
 *     "stage" : "operational",
 *     "region" : "burdekin"
 * }</pre>
 */
public class TaskBean extends AbstractBean {
    private String id; // "AggregationTask_1"
    private String jobId; // "Job_1",

    // Used with NcAnimate and NcAnimateFrame
    private String productId;
    private String regionId;

    /**
     * @deprecated Not documented, seems to be unused. Maybe was changed to "timeInstants"?
     */
    @Deprecated
    private String startDate;

    /**
     * @deprecated Not documented, seems to be unused. Maybe was changed to "timeInstants"?
     */
    @Deprecated
    private String endDate;

    /**
     * @deprecated Not documented, seems to be unused. Maybe was changed to "dependsOn"?
     */
    @Deprecated
    private List<String> dependencies;

    private String outputFilename; // "file1",
    private String status; // "SUCCESS",
    private String type; // "AggregationTask"

    /**
     * Create a JobPlanner task bean from a JSON object.
     * @param jsonTask {@code JSONObject} representing a {@code TaskBean}.
     */
    public TaskBean(JSONObject jsonTask) {
        if (jsonTask != null) {
            this.id = jsonTask.optString("_id", null);
            this.jobId = jsonTask.optString("jobId", null);

            this.productId = jsonTask.optString("productDefinitionId", null);
            this.regionId = jsonTask.optString("region", null);
            this.startDate = jsonTask.optString("startDate", null);
            this.endDate = jsonTask.optString("endDate", null);

            this.setDependencies(jsonTask.optJSONArray("dependencies"));
            this.outputFilename = jsonTask.optString("outputFilename", null);
            this.status = jsonTask.optString("status", null);
            this.type = jsonTask.optString("type", null);
        }
    }

    private void setDependencies(JSONArray jsonDependencies) {
        this.dependencies = null;

        if (jsonDependencies != null) {
            this.dependencies = new ArrayList<String>();
            for (int i=0; i<jsonDependencies.length(); i++) {
                String dependencyStr = jsonDependencies.optString(i, null);
                if (dependencyStr != null && !dependencyStr.isEmpty()) {
                    this.dependencies.add(dependencyStr);
                }
            }
        }
    }

    /**
     * Returns the task ID.
     * @return the task ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the task's job ID.
     * @return the task's job ID.
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Returns the task's product ID.
     * @return the task's product ID.
     */
    public String getProductId() {
        return this.productId;
    }

    /**
     * Returns the task's region ID.
     * @return the task's region ID.
     */
    public String getRegionId() {
        return this.regionId;
    }

    @Deprecated
    public String getStartDate() {
        return this.startDate;
    }

    @Deprecated
    public String getEndDate() {
        return this.endDate;
    }

    @Deprecated
    public List<String> getDependencies() {
        return this.dependencies;
    }

    /**
     * Returns the output filename.
     * @return the output filename.
     */
    public String getOutputFilename() {
        return this.outputFilename;
    }

    /**
     * Returns ths job's status.
     * @return ths job's status.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns the job's type.
     *
     * <p>Example: {@code ncanimate}, {code ncaggregate}</p>
     *
     * @return the job's type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("_id", this.id);
        json.put("jobId", this.jobId);

        json.put("productDefinitionId", this.productId);
        json.put("region", this.regionId);
        json.put("startDate", this.startDate);
        json.put("endDate", this.endDate);

        if (this.dependencies != null && !this.dependencies.isEmpty()) {
            json.put("dependencies", this.dependencies);
        }
        json.put("outputFilename", this.outputFilename);
        json.put("status", this.status);
        json.put("type", this.type);

        return json;
    }
}
