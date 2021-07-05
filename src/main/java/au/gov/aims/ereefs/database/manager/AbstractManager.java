/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import au.gov.aims.ereefs.database.table.DatabaseTable;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Managers are objects used to easily query documents
 * from the database, in {@code JSONObject} format.
 *
 * @param <T> Database class, used to differentiate between
 *   single key and composite key.
 */
public abstract class AbstractManager<T extends DatabaseTable> {
    protected DatabaseClient dbClient;
    private T table;

    /**
     * Create a manager using a {@link DatabaseClient} and a {@link DatabaseTable}.
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param table the {@link DatabaseTable} object to query.
     */
    protected AbstractManager(DatabaseClient dbClient, T table) {
        this.dbClient = dbClient;
        this.table = table;
    }

    /**
     * Returns {@code true} if the table, aka collection, exists; {@code false} otherwise.
     * @return {@code true} if the table exists.
     * @throws Exception if the database is unreachable.
     */
    public boolean tableExists() throws Exception {
        return this.table.exists();
    }

    /**
     * @deprecated Use {@link #setCacheStrategy(CacheStrategy)}
     * with {@link CacheStrategy#MEMORY} or {@link CacheStrategy#DISK}.
     */
    @Deprecated
    public void enableCache() {
        this.table.enableCache();
    }

    /**
     * @deprecated Use {@link #setCacheStrategy(CacheStrategy)}
     * with {@link CacheStrategy#NONE} or {@code null}.
     */
    @Deprecated
    public void disableCache() {
        this.table.disableCache();
    }

    /**
     * Set database cache strategy.
     *
     * <p>Cache is used to get pre-requested document without requesting the database.
     *   It's primarily used to reduce load on the database server.</p>
     * <ul>
     *   <li><em>{@link CacheStrategy#MEMORY}</em>: store documents in memory, for quick access. Used with unit tests.</li>
     *   <li><em>{@link CacheStrategy#DISK}</em>: store documents on disk, to be access by
     *     multiple independent processes running on the same computer.</li>
     *   <li><em>{@link CacheStrategy#NONE}</em>: disable cache.</li>
     * </ul>
     *
     * @param cacheStrategy the cache strategy to use.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.table.setCacheStrategy(cacheStrategy);
    }

    /**
     * Empty the database cache.
     * @throws IOException if something goes wrong while deleting
     *   files from the {@link CacheStrategy#DISK} cache.
     */
    public void clearCache() throws IOException {
        this.table.clearCache();
    }

    /**
     * Returns the {@link DatabaseTable} object queried by this manager.
     * The {@link DatabaseTable} object is the object that represent
     * the {@code collection} in a document database.
     *
     * @return the {@link DatabaseTable} object.
     */
    public T getTable() {
        return this.table;
    }

    /**
     * Insert or update a document in the database.
     *
     * <p>Updates the document if the document's ID exists in the database.
     *   Insert a new document otherwise.</p>
     *
     * @param json the {@code JSONObject} representing the document to save in the database.
     * @return the document, as it is in the database after been saved.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject save(JSONObject json) throws Exception {
        return this.save(json, true);
    }

    /**
     * Insert or update a document in the database.
     *
     * <p>Updates the document if the document's ID exists in the database.
     *   Insert a new document otherwise.</p>
     *
     * @param json the {@code JSONObject} representing the document to save in the database.
     * @param safe {@code false} to bypass ID safety check. Default {@code true}.
     * @return the document, as it is in the database after been saved.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject save(JSONObject json, boolean safe) throws Exception {
        if (json == null) {
            throw new IllegalArgumentException("JSON is null");
        }

        PrimaryKey key = this.table.getPrimaryKey(json);
        JSONObject existingRecord = this.table.select(key, CacheStrategy.NONE);
        if (existingRecord == null) {
            if (!this.tableExists()) {
                throw new RuntimeException(String.format("Table %s doesn't exists", this.table.getTableName()));
            }
            this.table.insert(json, safe);
        } else {
            this.table.update(json, key, safe);
        }

        return this.table.select(key, CacheStrategy.NONE);
    }

    /**
     * Returns all the documents from the database table.
     *
     * @return a {@link JSONObjectIterable} object used to request each document one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectAll() throws Exception {
        JSONObjectIterable iterable = this.table.selectAll();

        if (iterable.isEmpty() && !this.tableExists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", this.table.getTableName()));
        }

        return iterable;
    }
}
