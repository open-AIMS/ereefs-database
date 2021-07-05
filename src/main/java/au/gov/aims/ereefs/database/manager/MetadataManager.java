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
 * Manager used to manipulate metadata documents,
 * saved in the database.
 *
 * <p>This is used with:</p>
 * <ul>
 *   <li>NetCDF file metadata: {@link MetadataType#NETCDF}</li>
 *   <li>NcAggregate output file metadata: {@link MetadataType#NETCDF}</li>
 *   <li>NcAnimate output file metadata: {@link MetadataType#NCANIMATE_PRODUCT}</li>
 * </ul>
 */
public class MetadataManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "metadata";
    private static final String TABLE_ID_HASH_KEYNAME = "_id";

    private static final String TYPE_COLUMN_NAME = "type";
    private static final String DEFINITIONID_COLUMN_NAME = "definitionId";
    private static final String STATUS_COLUMN_NAME = "status";

    /**
     * @deprecated Use MetadataManager(DatabaseClient dbClient, CacheStrategy cacheStrategy)
     */
    @Deprecated
    public MetadataManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code MetadataManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public MetadataManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_HASH_KEYNAME);
    }

    /**
     * Returns all the metadata documents of a given type.
     *
     * @param type the type of metadata document.
     * @return a {@link JSONObjectIterable} object used to request each metadata document one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectAll(MetadataType type) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Missing type");
        }

        DatabaseTable table = this.getTable();

        JSONObjectIterable iterable = table.select(Filters.eq(TYPE_COLUMN_NAME, type.name()));
        if (iterable.isEmpty() && !table.exists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", TABLE_NAME));
        }

        return iterable;
    }

    /**
     * Returns all the metadata documents of a given type,
     * associated with a give definition ID.
     *
     * @param type the type of metadata document.
     * @param definitionId the definition ID.
     * @return a {@link JSONObjectIterable} object used to request each metadata document one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectByDefinitionId(MetadataType type, String definitionId) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Missing type");
        }

        if (definitionId == null || definitionId.isEmpty()) {
            throw new IllegalArgumentException("Missing definition id");
        }

        DatabaseTable table = this.getTable();
        JSONObjectIterable iterable = table.select(
            Filters.and(
                Filters.eq(TYPE_COLUMN_NAME, type.name()),
                Filters.eq(DEFINITIONID_COLUMN_NAME, definitionId)
            )
        );
        if (iterable.isEmpty() && !table.exists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", TABLE_NAME));
        }

        return iterable;
    }

    /**
     * Returns the metadata documents for all the valid NetCDF files
     * of a given type, associated with a give definition ID.
     *
     * @param type the type of metadata document.
     * @param definitionId the definition ID.
     * @return a {@link JSONObjectIterable} object used to request each metadata document one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectValidByDefinitionId(MetadataType type, String definitionId) throws Exception {
        return this.selectByDefinitionIdAndStatus(type, definitionId, "VALID");
    }

    /**
     * Returns all the metadata documents of a given type,
     * associated with a give definition ID, for a given status.
     *
     * @param type the type of metadata document.
     * @param definitionId the definition ID.
     * @param status status of the file associated with the metadata document.
     * @return a {@link JSONObjectIterable} object used to request each metadata document one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectByDefinitionIdAndStatus(MetadataType type, String definitionId, String status) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Missing type");
        }

        if (definitionId == null || definitionId.isEmpty()) {
            throw new IllegalArgumentException("Missing definition id");
        }

        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Missing status");
        }

        DatabaseTable table = this.getTable();
        JSONObjectIterable iterable = table.select(
            Filters.and(
                Filters.eq(TYPE_COLUMN_NAME, type.name()),
                Filters.eq(DEFINITIONID_COLUMN_NAME, definitionId),
                Filters.eq(STATUS_COLUMN_NAME, status)
            )
        );
        if (iterable.isEmpty() && !table.exists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", TABLE_NAME));
        }

        return iterable;
    }

    /**
     * List of type of metadata document.
     */
    public enum MetadataType {
        /**
         * Metadata extracted from NetCDF files
         */
        NETCDF,

        /**
         * Metadata of files produced by NcAnimate
         */
        NCANIMATE_PRODUCT
    }
}
