/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager.ncanimate;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.AbstractSingleKeyManager;

/**
 * Manager used to manipulate {@code ereefs-ncanimate2} configuration files,
 * saved in the database.
 *
 * <p>See {@link au.gov.aims.ereefs.database.manager.AbstractManager}</p>
 */
public class ConfigManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "ncanimate_config";
    private static final String TABLE_ID_KEYNAME = "_id";

    /**
     * @deprecated Use {@link ConfigManager(DatabaseClient, CacheStrategy)}
     */
    @Deprecated
    public ConfigManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code ConfigManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public ConfigManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_KEYNAME);
    }
}
