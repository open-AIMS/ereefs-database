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
 * Manager used to manipulate product definitions,
 * saved in the database.
 *
 * <p>There is 2 types of products:</p>
 * <ul>
 *   <li><em>ncaggregate</em>: Define the NcAggregate files which needs to be generated.</li>
 *   <li><em>ncanimate</em>: Summary of the group of NcAnimate files which can be generated
 *     using the NcAnimate configuration files present in the database, presented in a format
 *     which can be process by the eReefs system.</li>
 * </ul>
 */
public class ProductManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "product";
    private static final String TABLE_ID_HASH_KEYNAME = "_id";

    private static final String ENABLED_COLUMN_NAME = "enabled";

    public static final String NCANIMATE_PRODUCT_TYPE = "ncanimate";

    /**
     * @deprecated Use {@link ProductManager#(DatabaseClient, CacheStrategy)}
     * @param dbClient the {@link DatabaseClient} used to query the database.
     */
    @Deprecated
    public ProductManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code ProductManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public ProductManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_HASH_KEYNAME);
    }

    /**
     * Returns all the enabled product definitions from the database.
     *
     * @return a {@link JSONObjectIterable} object used to request each product definition one by one, as needed.
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
