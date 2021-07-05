/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.ereefs.bean.task.TaskBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.TaskManager;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Helper class used to simplify interaction with the database.
 *
 * <p>This class relates to the {@link TaskBean}
 * used with the {@code ereefs-ncanimate2} project.
 * It's a minimal representation of a task object,
 * defining only the element needed by the
 * {@code ereefs-ncanimate2} project.</p>
 */
public class TaskHelper {
    private DatabaseClient dbClient;
    private TaskManager taskManager;

    /**
     * @deprecated Use {@link TaskHelper#(DatabaseClient, CacheStrategy)}
     * @param dbClient the {@link DatabaseClient} used to query the database.
     */
    @Deprecated
    public TaskHelper(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Creates a {@code TaskHelper} using a database client and a cache strategy.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public TaskHelper(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        this.dbClient = dbClient;
        this.taskManager = new TaskManager(this.dbClient, cacheStrategy);
    }

    /**
     * Set the {@link TaskManager} cache strategy.
     * @param cacheStrategy the new cache strategy.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.taskManager.setCacheStrategy(cacheStrategy);
    }

    /**
     * Clear the {@link TaskManager} cache.
     * @throws IOException if something goes wrong while clearing the disk cache.
     */
    public void clearCache() throws IOException {
        this.taskManager.clearCache();
    }

    /**
     * Returns the {@link DatabaseClient} that was set in the constructor.
     * @return the {@link DatabaseClient}.
     */
    public DatabaseClient getDbClient() {
        return this.dbClient;
    }

    /**
     * Retrieve a {@link TaskBean} from the database
     * for a given {@link TaskBean} ID.
     *
     * @param taskId the task ID.
     * @return the {@link TaskBean} or null if not found.
     * @throws Exception if the database is unreachable.
     */
    public TaskBean getTask(String taskId) throws Exception {
        JSONObject jsonTask = this.taskManager.select(taskId);
        if (jsonTask != null) {
            return new TaskBean(jsonTask);
        }

        return null;
    }

}
