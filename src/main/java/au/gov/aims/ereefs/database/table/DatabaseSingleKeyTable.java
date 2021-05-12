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
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.key.SinglePrimaryKey;
import org.json.JSONObject;

/**
 * Object representing a database table
 * with a single attribute primary key.
 */
public class DatabaseSingleKeyTable extends DatabaseTable {

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
    public DatabaseSingleKeyTable(DatabaseClient databaseClient, String tableName, CacheStrategy cacheStrategy, String primaryKeyName) {
        super(databaseClient, tableName, cacheStrategy, primaryKeyName);
    }

    /**
     * @inheritDoc
     */
    @Override
    public SinglePrimaryKey getPrimaryKey(JSONObject json) {
        if (this.getPrimaryKeyName() == null) {
            throw new IllegalStateException(String.format("The table %s has no primary key", this.getTableName()));
        }

        Object primaryKeyValue = json.opt(this.getPrimaryKeyName());
        if (primaryKeyValue == null) {
            throw new IllegalArgumentException(String.format("JSON object doesn't contains the primary key %s:%n%s", this.getPrimaryKeyName(), json.toString(4)));
        }

        return this.getPrimaryKey(primaryKeyValue);
    }

    /**
     * Returns a primary key object for a given primary key value.
     * @param primaryKeyValue the primary key value.
     * @return a primary key object.
     */
    public SinglePrimaryKey getPrimaryKey(Object primaryKeyValue) {
        return new SinglePrimaryKey(this.getPrimaryKeyName(), primaryKeyValue);
    }
}
