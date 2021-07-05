/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representing a database table, also knows as
 * {@code collection} in document databases.
 *
 * <p>It manages cache and reconnection to the database
 * automatically.</p>
 */
public abstract class DatabaseTable {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTable.class);

    private DatabaseClient databaseClient;
    private String tableName;

    private String primaryKeyName;

    // Cache used to reduce request to the Database.
    // Cache is freed by calling clearCache().
    private CacheStrategy cacheStrategy;
    private File cacheDirectory; // Used with DISK cache
    private DatabaseTableCache memoryCache; // Used with MEMORY cache

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
    public DatabaseTable(DatabaseClient databaseClient, String tableName, CacheStrategy cacheStrategy, String primaryKeyName) {
        this.databaseClient = databaseClient;
        this.tableName = tableName;
        this.primaryKeyName = primaryKeyName;

        this.memoryCache = null;
        this.cacheDirectory = null;
        this.setCacheStrategy(cacheStrategy);
    }

    /**
     * Returns the name of the database table.
     * @return the name of the database table.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Returns the table primary key object.
     * @return the table primary key object.
     */
    public String getPrimaryKeyName() {
        return this.primaryKeyName;
    }

    /**
     * @deprecated Use {@link #setCacheStrategy(CacheStrategy)} with {@link CacheStrategy#MEMORY}
     */
    @Deprecated
    public void enableCache() {
        this.setCacheStrategy(CacheStrategy.MEMORY);
    }

    /**
     * @deprecated Use {@link #setCacheStrategy(CacheStrategy)} with {@link CacheStrategy#NONE}
     */
    @Deprecated
    public void disableCache() {
        this.setCacheStrategy(CacheStrategy.NONE);
    }

    /**
     * Empty the cache.
     * @throws IOException if something goes wrong while emptying the
     *   {@link CacheStrategy#DISK} cache, if {@link CacheStrategy#DISK} cache is used.
     */
    public void clearCache() throws IOException {
        switch (cacheStrategy) {
            case MEMORY:
                this.memoryCache.clear();
                break;

            case DISK:
                File cacheDir = this.getCacheDirectory();
                Utils.deleteDirectory(cacheDir);
                break;
        }
    }

    /**
     * Set the cache strategy.
     * @param cacheStrategy the cache strategy.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        if (cacheStrategy == null) {
            cacheStrategy = CacheStrategy.NONE;
        }
        if (!cacheStrategy.equals(this.cacheStrategy)) {
            if (this.cacheStrategy != null) {
                try {
                    this.clearCache();
                } catch (IOException e) {
                    LOGGER.error(String.format("Error occurred while clearing old cache type %s", this.cacheStrategy.name()), e);
                }
            }

            // Initialise cache
            switch (cacheStrategy) {
                case MEMORY:
                    this.memoryCache = new DatabaseTableCache();
                    this.memoryCache.enable();
                    break;

                case DISK:
                    File cacheDir = this.getCacheDirectory();
                    cacheDir.mkdirs();
                    break;
            }

            this.cacheStrategy = cacheStrategy;
        }
    }

    /**
     * Returns the cache strategy.
     * @return the cache strategy.
     */
    public CacheStrategy getCacheStrategy() {
        return this.cacheStrategy;
    }

    /**
     * Returns the cache, if {@link CacheStrategy#MEMORY} is used.
     * @return the memory cache.
     */
    public DatabaseTableCache getMemoryCache() {
        return this.memoryCache;
    }

    /**
     * Returns default the directory used for {@link CacheStrategy#DISK} cache.
     * @return default the directory used for {@link CacheStrategy#DISK} cache.
     */
    public static File getDatabaseCacheDirectory() {
        String tmpDirStr = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirStr);
        return new File(tmpDir, "ereefs-db-cache");
    }

    /**
     * Returns the directory used for {@link CacheStrategy#DISK} cache.
     * @return the directory used for {@link CacheStrategy#DISK} cache.
     */
    private File getCacheDirectory() {
        if (this.cacheDirectory == null) {
            this.cacheDirectory = new File(
                    DatabaseTable.getDatabaseCacheDirectory(),
                    Utils.safeFilename(this.tableName));
        }

        return this.cacheDirectory;
    }

    private MongoCollection<Document> getTable(MongoDatabase database) {
        // Request the table everytime, since it might have been deleted
        return database.getCollection(this.tableName, Document.class);
    }

    /**
     * Check if the table exists.
     *
     * <p>This method needs to query the database. Try to use it only
     * when you suspect the database to not exist, such as when a query
     * returns nothing.</p>
     *
     * <p>NOTE: "this.getTable() != null" does NOT work.</p>
     *
     * @return {@code true} is the table exists; {@code false} otherwise.
     * @throws Exception if the database is unreachable.
     */
    public boolean exists() throws Exception {
        if (this.tableName == null || this.tableName.isEmpty()) {
            return false;
        }

        boolean exists = false;

        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);

                long start = System.currentTimeMillis();
                for (String collectionName : database.listCollectionNames()) {
                    if (this.tableName.equals(collectionName)) {
                        exists = true;
                        break;
                    }
                }
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: List collection names in %d sec (%d ms)", elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }

        return exists;
    }

    /**
     * Check if a document exists in the table.
     * @param primaryKey the document's primary key.
     * @return {@code true} if a document with specified ID exists in the DB; {@code false} otherwise.
     * @throws Exception if the database is unreachable.
     */
    public boolean exists(PrimaryKey primaryKey) throws Exception {
        boolean exists = false;
        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                MongoCollection<Document> table = this.getTable(database);

                long start = System.currentTimeMillis();
                FindIterable<Document> findIterable = table.find(primaryKey.getFilter());
                if (findIterable != null) {
                    Document first = findIterable.first();
                    if (first != null) {
                        exists = true;
                    }
                }
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: Check if a record ID exists %s from %s in %d sec (%d ms)",
                        primaryKey.toJSON().toString(), this.tableName, elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }

        return exists;
    }

    /**
     * Insert a document in the database.
     * @param json the document to insert in the database table.
     * @throws Exception if the database is unreachable.
     */
    public void insert(JSONObject json) throws Exception {
        this.insert(json, true);
    }

    /**
     * Insert a document in the database.
     * @param json the document to insert in the database table.
     * @param safe {@code false} to bypass ID safety check. Default {@code true}.
     * @throws Exception if the database is unreachable.
     */
    public void insert(JSONObject json, boolean safe) throws Exception {
        if (safe) {
            PrimaryKey pk = this.getPrimaryKey(json);
            if (!pk.validate()) {
                throw new IllegalArgumentException(String.format("Invalid primary key: %s", pk));
            }
        }

        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                MongoCollection<Document> table = this.getTable(database);

                long start = System.currentTimeMillis();
                table.insertOne(Document.parse(json.toString()));
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: Insert record into %s in %d sec (%d ms)", this.tableName, elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }
    }

    /**
     * Update a document in the database.
     * @param json the document to update.
     * @param primaryKey the primary key object, before updating the document.
     * @throws Exception if the database is unreachable.
     */
    public void update(JSONObject json, PrimaryKey primaryKey) throws Exception {
        this.update(json, primaryKey, true);
    }

    /**
     * Update a document in the database.
     * @param json the document to update.
     * @param primaryKey the primary key object, before updating the document.
     * @param safe {@code false} to bypass ID safety check. Default {@code true}.
     * @throws Exception if the database is unreachable.
     */
    public void update(JSONObject json, PrimaryKey primaryKey, boolean safe) throws Exception {
        PrimaryKey newPrimaryKey = this.getPrimaryKey(json);
        if (safe) {
            if (!newPrimaryKey.validate()) {
                throw new IllegalArgumentException(String.format("Invalid primary key: %s", newPrimaryKey));
            }
        }

        this.removeFromCache(primaryKey);

        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                MongoCollection<Document> table = this.getTable(database);

                long start = System.currentTimeMillis();
                table.replaceOne(
                        primaryKey.getFilter(),
                        Document.parse(json.toString()));
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: Update record from %s in %d sec (%d ms)", this.tableName, elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }

        if (success) {
            this.addToCache(newPrimaryKey, json);
        }
    }

    /**
     * Delete a document from the database.
     * @param primaryKey the primary key object of the document to delete.
     * @return the {@code JSONObject} of the deleted document.
     * @throws Exception if the database is unreachable.
     */
    public JSONObject delete(PrimaryKey primaryKey) throws Exception {
        this.removeFromCache(primaryKey);
        JSONObject deletedJson = null;

        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                MongoCollection<Document> table = this.getTable(database);

                long start = System.currentTimeMillis();
                Document deleted = table.findOneAndDelete(primaryKey.getFilter());
                if (deleted != null) {
                    deletedJson = new JSONObject(deleted.toJson());
                }
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: Delete record from %s in %d sec (%d ms)", this.tableName, elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }

        return deletedJson;
    }

    /**
     * Returns an iterable object used to loop though all the documents from the database table.
     *
     * <p>NOTE: The iterable object send a query to the database for each object.</p>
     *
     * @return an iterable object used to loop though all the documents from the database table.
     * @throws Exception if the database is unreachable.
     */
    public JSONObjectIterable selectAll() throws Exception {
        return this.select((Bson)null);
    }

    /**
     * Returns an iterable object used to loop though a list of documents from the database table.
     *
     * @param filter {@code Bson} filter to filter the documents.
     * @return an iterable object used to loop though the documents that match the filter.
     * @throws Exception if the database is unreachable.
     */
    public JSONObjectIterable select(Bson filter) throws Exception {
        List<PrimaryKey> primaryKeys = this.selectPrimaryKeys(filter);
        return new JSONObjectIterable(this, primaryKeys.iterator());
    }

    /**
     * Return the list of document's primary keys that match a {@code Bson} filter.
     * @param filter {@code Bson} filter to filter the documents.
     * @return list of document's primary keys.
     * @throws Exception if the database is unreachable.
     */
    public List<PrimaryKey> selectPrimaryKeys(Bson filter) throws Exception {
        List<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();
        boolean success = false;
        for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                MongoCollection<Document> table = this.getTable(database);

                long start = System.currentTimeMillis();
                FindIterable<Document> findIterable = filter == null ? table.find() : table.find(filter);
                if (findIterable != null) {
                    for (Document document : findIterable) {
                        JSONObject json = new JSONObject(document.toJson());
                        PrimaryKey primaryKey = this.getPrimaryKey(json);
                        if (primaryKey != null) {
                            primaryKeys.add(primaryKey);
                        }
                    }
                }
                success = true;
                long end = System.currentTimeMillis();
                int elapseMs = (int)(end - start);
                int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                LOGGER.debug(String.format("DB Debug: Select record IDs from %s using filter: \"%s\" in %d sec (%d ms)",
                        this.tableName,
                        (filter == null ? "NULL" : filter.toString()),
                        elapseSec, elapseMs));
            } catch(OutOfMemoryError ex) {
                throw ex;
            } catch(Exception ex) {
                int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, this.tableName, delay), ex);

                if (attempt == this.databaseClient.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.databaseClient.resolveServerAddr();
            }
        }

        return primaryKeys;
    }

    /**
     * Returns a single document matching a primary key.
     *
     * @param primaryKey the document's primary key.
     * @return the matching document, or null if no document is using the provided primary key.
     * @throws Exception if the database is unreachable.
     */
    public JSONObject select(PrimaryKey primaryKey) throws Exception {
        return select(primaryKey, null);
    }

    /**
     * Returns a single document matching a primary key.
     *
     * <p>This method is used internally to avoid retrieving
     * document from the cache or caching selected document.</p>
     *
     * @param primaryKey the document's primary key.
     * @param cacheStrategyOverwrite the cache strategy to use.
     *     Set to {@code CacheStrategy#NONE} to bypass the cache.
     * @return the matching document, or null if no document is using the provided primary key.
     * @throws Exception if the database is unreachable.
     */
    public JSONObject select(PrimaryKey primaryKey, CacheStrategy cacheStrategyOverwrite) throws Exception {
        JSONObject json = this.retrieveFromCache(primaryKey, cacheStrategyOverwrite);

        if (json == null) {
            boolean success = false;
            for (int attempt=1; attempt<=this.databaseClient.getDbRetryAttempts() && !success; attempt++) {
                try (MongoClient mongoClient = this.databaseClient.getMongoClient()) {
                    MongoDatabase database = this.databaseClient.getMongoDatabase(mongoClient);
                    MongoCollection<Document> table = this.getTable(database);

                    long start = System.currentTimeMillis();
                    FindIterable<Document> findIterable = table.find(primaryKey.getFilter());
                    if (findIterable == null) {
                        json = null;
                    } else {
                        Document first = findIterable.first();
                        if (first == null) {
                            json = null;
                        } else {
                            json = new JSONObject(first.toJson());
                        }
                    }
                    this.addToCache(primaryKey, json);
                    success = true;
                    long end = System.currentTimeMillis();
                    int elapseMs = (int)(end - start);
                    int elapseSec = (int)Math.round((elapseMs) / 1000.0);
                    LOGGER.debug(String.format("DB Debug: Select record ID %s from %s in %d sec (%d ms)",
                            primaryKey.toJSON().toString(), this.tableName, elapseSec, elapseMs));
                } catch(OutOfMemoryError ex) {
                    throw ex;
                } catch(Exception ex) {
                    int delay = this.databaseClient.getDbInitialDelayBetweenAttempt() * attempt;
                    LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                            attempt, this.tableName, delay), ex);

                    if (attempt == this.databaseClient.getDbRetryAttempts()) {
                        LOGGER.error("Maximum number of attempt reached.");
                        throw new Exception("Maximum number of attempt reached.", ex);
                    }
                    Thread.sleep(delay * 1000);
                    this.databaseClient.resolveServerAddr();
                }
            }
        }

        return json;
    }

    private JSONObject retrieveFromCache(PrimaryKey primaryKey, CacheStrategy cacheStrategyOverwrite) throws IOException {
        JSONObject json = null;

        CacheStrategy cacheStrategy = this.cacheStrategy;
        if (cacheStrategyOverwrite != null) {
            cacheStrategy = cacheStrategyOverwrite;
        }

        switch (cacheStrategy) {
            case MEMORY:
                json = this.memoryCache.getCache(primaryKey);
                break;

            case DISK:
                File cachedFile = this.getCacheFile(primaryKey);
                if (cachedFile.canRead()) {
                    String jsonString = new String(Files.readAllBytes(cachedFile.toPath()));
                    if (!jsonString.isEmpty()) {
                        json = new JSONObject(jsonString);
                    }
                }
                break;

            // null or NONE, do nothing. The method will return null
        }

        return json;
    }

    private JSONObject removeFromCache(PrimaryKey primaryKey) throws IOException {
        JSONObject json = null;

        switch (cacheStrategy) {
            case MEMORY:
                json = this.memoryCache.removeCache(primaryKey);
                break;

            case DISK:
                File cachedFile = this.getCacheFile(primaryKey);
                if (cachedFile.canRead()) {
                    String jsonString = new String(Files.readAllBytes(cachedFile.toPath()));
                    if (!jsonString.isEmpty()) {
                        json = new JSONObject(jsonString);
                    }
                    cachedFile.delete();
                }
                break;

            // null or NONE, do nothing. The method will return null
        }

        return json;
    }

    private void addToCache(PrimaryKey primaryKey, JSONObject json) throws IOException {
        if (json != null) {
            switch (this.cacheStrategy) {
                case MEMORY:
                    this.memoryCache.setCache(primaryKey, json);
                    break;

                case DISK:
                    File cachedFile = this.getCacheFile(primaryKey);
                    FileUtils.writeStringToFile(cachedFile, json.toString(), StandardCharsets.UTF_8);
                    break;
            }
        }
    }

    private File getCacheFile(PrimaryKey primaryKey) {
        String filename = Utils.safeFilename(primaryKey.toJSON().toString()) + ".json";
        File cacheDirectory = this.getCacheDirectory();
        return new File(cacheDirectory, filename);
    }

    /**
     * Returns the primary key from a {@code JSONObject} document, retrieved from the database.
     * @param json the document retrieved from the database.
     * @return the document's primary key object.
     */
    public abstract PrimaryKey getPrimaryKey(JSONObject json);
}
