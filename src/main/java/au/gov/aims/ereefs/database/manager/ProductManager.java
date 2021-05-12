/*
 *  Copyright (C) 2019 Australian Institute of Marine Science
 *
 *  Contact: Gael Lafond <g.lafond@aims.gov.au>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
     * @deprecated Use ProductManager(DatabaseClient dbClient, CacheStrategy cacheStrategy)
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
