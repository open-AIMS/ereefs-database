/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Memory cache used to reduce request to the Database.
 *
 * <p>Needs to be enabled using enable().</p>
 *
 * <p>Memory is freed by calling clear() or disable().</p>
 */
public class DatabaseTableCache {
    private Map<PrimaryKey, JSONObject> cache;

    /**
     * Returns {@code true} if the cache is enabled; {@code false} otherwise.
     * @return {@code true} if the cache is enabled; {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.cache != null;
    }

    /**
     * Enable the cache.
     * Does nothing if the cache is already enabled.
     */
    public void enable() {
        if (this.cache == null) {
            this.cache = new HashMap<PrimaryKey, JSONObject>();
        }
    }

    /**
     * Disable the cache and clear cached memory.
     */
    public void disable() {
        if (this.cache != null) {
            this.cache.clear();
            this.cache = null;
        }
    }

    /**
     * Clear cached memory.
     */
    public void clear() {
        if (this.cache != null) {
            this.cache.clear();
        }
    }

    /**
     * Add a {@code JSONObject} entity to the cache.
     * @param key the entity key or ID.
     * @param entity the entity to add to the cache.
     */
    public void setCache(PrimaryKey key, JSONObject entity) {
        if (this.cache != null) {
            this.cache.put(key, entity);
        }
    }

    /**
     * Returns a cached {@code JSONObject} entity from the cache.
     * Returns null if the key is not found in the cache.
     *
     * @param key the key or ID of entity to retrieve from the cache.
     * @return the cached {@code JSONObject} entity or null.
     */
    public JSONObject getCache(PrimaryKey key) {
        if (this.cache != null) {
            return this.cache.get(key);
        }

        return null;
    }

    /**
     * Remove a {@code JSONObject} entity from the cache.
     *
     * @param key the key or ID of entity to remove from the cache.
     * @return the removed {@code JSONObject} entity or null.
     */
    public JSONObject removeCache(PrimaryKey key) {
        if (this.cache != null) {
            return this.cache.remove(key);
        }

        return null;
    }
}
