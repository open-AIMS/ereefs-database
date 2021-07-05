/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.DatabaseSingleKeyTable;
import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import au.gov.aims.ereefs.database.table.key.SinglePrimaryKey;
import org.json.JSONObject;

/**
 * Managers are objects used to easily query documents
 * from the database, in {@code JSONObject} format.
 *
 * This class is used with database table which use
 * a single ID key; an ID represented by a single
 * JSON attribute.
 */
public abstract class AbstractSingleKeyManager extends AbstractManager<DatabaseSingleKeyTable> {

    /**
     * Create a manager using a {@link DatabaseClient} and a database table.
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *     "_id": "Alex Smith"
     * }</pre>
     *
     * <p>The above primary key would be created using:</p>
     * <pre class="code">
     * new AbstractCompositeKeyManager(dbClient, "employee", CacheStrategy.DISK, "_id");</pre>
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param tableName name of database table, also known as {@code collection}.
     * @param cacheStrategy the cache strategy to use.
     * @param tablePrimaryKeyName the field name containing the primary key of the table.
     *   Usually {@code _id}.
     */
    protected AbstractSingleKeyManager(DatabaseClient dbClient, String tableName, CacheStrategy cacheStrategy, String tablePrimaryKeyName) {
        super(dbClient, dbClient.getTable(tableName, cacheStrategy, tablePrimaryKeyName));
    }

    /**
     * Select a single document from the database table.
     *
     * @param idValue the document ID.
     * @return the {@code JSONObject} of the selected document.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject select(String idValue) throws Exception {
        SinglePrimaryKey primaryKey = this.getTable().getPrimaryKey(idValue);
        JSONObject json = this.getTable().select(primaryKey);

        if (json == null && !this.tableExists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", this.getTable().getTableName()));
        }

        return json;
    }

    /**
     * Delete a single document from the database table.
     *
     * @param primaryIdValue the document ID.
     * @return the {@code JSONObject} of the deleted document.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject delete(String primaryIdValue) throws Exception {
        PrimaryKey primaryKey = this.getTable().getPrimaryKey(primaryIdValue);
        JSONObject json = this.getTable().delete(primaryKey);

        if (json == null && !this.tableExists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", this.getTable().getTableName()));
        }

        return json;
    }

    /**
     * Test if a document exists in the database table.
     *
     * @param primaryIdValue the document ID.
     * @return {@code true} if the document exists; {@code false} otherwise.
     * @throws Exception if the database is unreachable.
     */
    public boolean exists(String primaryIdValue) throws Exception {
        PrimaryKey primaryKey = this.getTable().getPrimaryKey(primaryIdValue);
        return this.getTable().exists(primaryKey);
    }
}
