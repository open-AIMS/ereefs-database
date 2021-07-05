/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.DatabaseCompositeKeyTable;
import au.gov.aims.ereefs.database.table.key.CompositePrimaryKey;
import org.json.JSONObject;

/**
 * Managers are objects used to easily query documents
 * from the database, in {@code JSONObject} format.
 *
 * This class is used with database table which use
 * a composite ID key; an ID composed of multiple
 * JSON attributes.
 */
public abstract class AbstractCompositeKeyManager extends AbstractManager<DatabaseCompositeKeyTable> {

    /**
     * Create a manager using a {@link DatabaseClient} and a database table.
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *     "_id": {
     *         "firstname": "Alex",
     *         "lastname": "Smith"
     *     }
     * }</pre>
     *
     * <p>The above primary key would be created using:</p>
     * <pre class="code">
     * new AbstractCompositeKeyManager(dbClient, "employee", CacheStrategy.DISK, "_id", "firstname", "lastname");</pre>
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param tableName name of database table, also known as {@code collection}.
     * @param cacheStrategy the cache strategy to use.
     * @param primaryKeyName the field name containing the primary key attributes
     *   for the table. Usually {@code _id}.
     * @param compositeKeyNames the field names for the primary key attributes.
     */
    protected AbstractCompositeKeyManager(DatabaseClient dbClient, String tableName, CacheStrategy cacheStrategy, String primaryKeyName, String ... compositeKeyNames) {
        super(dbClient, dbClient.getTable(tableName, cacheStrategy, primaryKeyName, compositeKeyNames));
    }

    /**
     * Select a single document from the database table.
     *
     * @param compositeKeyValues the document ID values.
     * @return the {@code JSONObject} of the selected document.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject select(String ... compositeKeyValues) throws Exception {
        CompositePrimaryKey primaryKey = this.getTable().getPrimaryKey(compositeKeyValues);
        JSONObject json = this.getTable().select(primaryKey);

        if (json == null && !this.tableExists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", this.getTable().getTableName()));
        }

        return json;
    }

    /**
     * Delete a single document from the database table.
     *
     * @param compositeKeyValues the document ID values.
     * @return the {@code JSONObject} of the deleted document.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject delete(String ... compositeKeyValues) throws Exception {
        CompositePrimaryKey primaryKey = this.getTable().getPrimaryKey(compositeKeyValues);
        JSONObject json = this.getTable().delete(primaryKey);

        if (json == null && !this.tableExists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", this.getTable().getTableName()));
        }

        return json;
    }

    /**
     * Test if a document exists in the database table.
     *
     * @param compositeKeyValues the document ID values.
     * @return {@code true} if the document exists; {@code false} otherwise.
     * @throws Exception if the database is unreachable.
     */
    public boolean exists(String ... compositeKeyValues) throws Exception {
        CompositePrimaryKey primaryKey = this.getTable().getPrimaryKey(compositeKeyValues);
        return this.getTable().exists(primaryKey);
    }
}
