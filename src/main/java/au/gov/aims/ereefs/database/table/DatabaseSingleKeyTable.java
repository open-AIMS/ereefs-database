/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.key.SinglePrimaryKey;
import org.json.JSONObject;

/**
 * Object representing a database table
 * with a single attribute primary key.
 */
public class DatabaseSingleKeyTable extends DatabaseTable {

    /**
     * Creates an object representing a database table.
     *
     * <p>NOTE: This does NOT create a table in the database.</p>
     *
     * @param databaseClient the {@link DatabaseClient} used to query the database.
     * @param tableName the name of the database table.
     * @param cacheStrategy the cache strategy.
     * @param primaryKeyName the table primary key name. Usually {@code _id}.
     */
    public DatabaseSingleKeyTable(DatabaseClient databaseClient, String tableName, CacheStrategy cacheStrategy, String primaryKeyName) {
        super(databaseClient, tableName, cacheStrategy, primaryKeyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SinglePrimaryKey getPrimaryKey(JSONObject json) {
        if (this.getPrimaryKeyName() == null) {
            throw new IllegalStateException(String.format("The table %s has no primary key", this.getTableName()));
        }

        Object primaryKeyValue = json.opt(this.getPrimaryKeyName());
        if (primaryKeyValue == null) {
            throw new IllegalArgumentException(String.format("JSON object doesn't contains the primary key %s:%n%s", this.getPrimaryKeyName(), json.toString(4)));
        }

        return this.getPrimaryKey(primaryKeyValue);
    }

    /**
     * Returns a primary key object for a given primary key value.
     * @param primaryKeyValue the primary key value.
     * @return a primary key object.
     */
    public SinglePrimaryKey getPrimaryKey(Object primaryKeyValue) {
        return new SinglePrimaryKey(this.getPrimaryKeyName(), primaryKeyValue);
    }
}
