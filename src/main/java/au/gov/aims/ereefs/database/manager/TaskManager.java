/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;

/**
 * Manager used to manipulate task definitions,
 * saved in the database.
 *
 * <p>Example of tasks</p>
 * <ul>
 *     <li>A Download Manager task to download latest NetCDF files.</li>
 *     <li>A NcAggregate task to generate an aggregated NetCDF file from a downloaded NetCDF file.</li>
 *     <li>A NcAnimate task to generate a suite of video and static maps from a suite of NetCDF files and or GRIB files.</li>
 * </ul>
 */
public class TaskManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "task";
    private static final String TABLE_ID_HASH_KEYNAME = "_id";

    /**
     * @deprecated Use {@link TaskManager#(DatabaseClient, CacheStrategy)}
     * @param dbClient the {@link DatabaseClient} used to query the database.
     */
    @Deprecated
    public TaskManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code TaskManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public TaskManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_HASH_KEYNAME);
    }
}
