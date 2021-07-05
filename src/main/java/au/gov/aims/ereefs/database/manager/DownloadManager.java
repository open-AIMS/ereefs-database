/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.DatabaseTable;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import com.mongodb.client.model.Filters;

/**
 * Manager used to manipulate {@code ereefs-download-manager} configuration files,
 * saved in the database.
 */
public class DownloadManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "download";
    private static final String TABLE_ID_HASH_KEYNAME = "_id";

    private static final String ENABLED_COLUMN_NAME = "enabled";

    /**
     * @deprecated Use DownloadManager(DatabaseClient dbClient, CacheStrategy cacheStrategy)
     */
    @Deprecated
    public DownloadManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code DownloadManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public DownloadManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_HASH_KEYNAME);
    }

    /**
     * Returns all the enabled {@code ereefs-download-manager} configuration files from the database.
     *
     * @return a {@link JSONObjectIterable} object used to request each configuration file one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectAllEnabled() throws Exception {
        DatabaseTable table = this.getTable();
        JSONObjectIterable iterable = table.select(Filters.eq(ENABLED_COLUMN_NAME, true));
        if (iterable.isEmpty() && !table.exists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", TABLE_NAME));
        }

        return iterable;
    }
}
