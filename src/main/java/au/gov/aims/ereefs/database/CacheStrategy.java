/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database;

/**
 * List of supported type of database cache.
 */
public enum CacheStrategy {
    /**
     * No cache
     */
    NONE,

    /**
     * Cache as JSON file on disk
     */
    DISK,

    /**
     * Cache in memory (RAM)
     */
    MEMORY
}
