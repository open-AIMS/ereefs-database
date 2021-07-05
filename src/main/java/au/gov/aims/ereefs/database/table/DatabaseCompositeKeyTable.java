/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.key.CompositePrimaryKey;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Object representing a database table
 * with a composite primary key.
 *
 * <p>Composite primary key reference: <a href="https://docs.mongodb.com/manual/core/index-compound/" target="_blank">https://docs.mongodb.com/manual/core/index-compound/</a></p>
 */
public class DatabaseCompositeKeyTable extends DatabaseTable {
    private String[] compositeKeyNames;

    /**
     * Creates an object representing a database table.
     *
     * <p>NOTE: This does NOT create a table in the database.</p>
     *
     * @param databaseClient the {@link DatabaseClient} used to query the database.
     * @param tableName the name of the database table.
     * @param cacheStrategy the cache strategy.
     * @param primaryKeyName the table primary key name. Usually {@code _id}.
     * @param compositeKeyNames the names of the attributes in the composite key.
     */
    public DatabaseCompositeKeyTable(DatabaseClient databaseClient, String tableName, CacheStrategy cacheStrategy, String primaryKeyName, String ... compositeKeyNames) {
        super(databaseClient, tableName, cacheStrategy, primaryKeyName);
        this.compositeKeyNames = compositeKeyNames;
    }

    /**
     * Returns the composite key values.
     * @return the composite key values.
     */
    public String[] getCompositeKeyNames() {
        return this.compositeKeyNames;
    }

    /**
     * @inheritDoc
     */
    @Override
    public CompositePrimaryKey getPrimaryKey(JSONObject json) {
        String tablePrimaryKey = this.getPrimaryKeyName();
        if (tablePrimaryKey == null) {
            throw new IllegalStateException(String.format("The table %s has no primary key", this.getTableName()));
        }

        if (!json.has(tablePrimaryKey)) {
            throw new IllegalArgumentException(String.format("JSON object doesn't contains the primary key %s:%n%s", tablePrimaryKey, json.toString(4)));
        }

        JSONObject primaryKeyValue = json.optJSONObject(tablePrimaryKey);
        if (primaryKeyValue == null) {
            throw new IllegalArgumentException(String.format("JSON object primary key %s is not a composite key", tablePrimaryKey));
        }

        if (this.compositeKeyNames == null || this.compositeKeyNames.length <= 0) {
            throw new IllegalStateException(String.format("The table %s composite key is invalid", this.getTableName()));
        }

        Map<String, Object> compositeKeyValueMap = new HashMap<String, Object>();
        for (String compositeKeyName : this.compositeKeyNames) {
            if (primaryKeyValue.has(compositeKeyName)) {
                compositeKeyValueMap.put(compositeKeyName, primaryKeyValue.opt(compositeKeyName));
            } else {
                throw new IllegalArgumentException(String.format("JSON object primary key %s doesn't contain the composite key %s", tablePrimaryKey, compositeKeyName));
            }
        }

        return this.getPrimaryKey(compositeKeyValueMap);
    }

    /**
     * Returns a primary key object for {@code Map} representing the composite primary key values.
     * @param compositeKeyValueMap the primary key values.
     * @return a primary key object.
     */
    public CompositePrimaryKey getPrimaryKey(Map<String, Object> compositeKeyValueMap) {
        return new CompositePrimaryKey(this.getPrimaryKeyName(), compositeKeyValueMap);
    }

    /**
     * Returns a primary key object for composite primary key values.
     * @param compositeKeyValues the primary key values.
     * @return a primary key object.
     */
    public CompositePrimaryKey getPrimaryKey(String ... compositeKeyValues) {
        return this.getPrimaryKey((Object[]) compositeKeyValues);
    }

    /**
     * Returns a primary key object for composite primary key values.
     * @param compositeKeyValues the primary key values.
     * @return a primary key object.
     */
    public CompositePrimaryKey getPrimaryKey(Object ... compositeKeyValues) {
        if (this.compositeKeyNames.length != compositeKeyValues.length) {
            throw new IllegalArgumentException(String.format("Wrong number of composite key value. Expected %d, was %d", this.compositeKeyNames.length, compositeKeyValues.length));
        }

        Map<String, Object> compositeKeyValueMap = new HashMap<String, Object>();
        for (int i=0; i<this.compositeKeyNames.length; i++) {
            compositeKeyValueMap.put(compositeKeyNames[i], compositeKeyValues[i]);
        }

        return new CompositePrimaryKey(this.getPrimaryKeyName(), compositeKeyValueMap);
    }

}
