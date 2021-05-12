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

import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * An {@code Iterable} object used to iterate through a list
 * of {@code JSONObject} found in the database.
 *
 * <p>It takes a {@link PrimaryKey} {@code Iterator} at creation.
 * For each iteration of the {@code JSONObjectIterable}, it send a
 * request to the database to get the {@code JSONObject} document
 * for the next {@link PrimaryKey}.</p>
 *
 * <p>It manages reconnection to the database automatically.</p>
 */
public class JSONObjectIterable implements Iterable<JSONObject> {
    private static final Logger LOGGER = Logger.getLogger(JSONObjectIterable.class);

    private Iterator<PrimaryKey> primaryKeyIterator;
    private DatabaseTable databaseTable;

    /**
     * Creates a {@code JSONObjectIterable} from an {@code Iterator} of {@link PrimaryKey}
     * and a {@link DatabaseTable} object.
     *
     * <p>The {@link DatabaseTable} is used to query the database and retrieve
     * {@code JSONObject} as requested.</p>
     *
     * @param databaseTable the database table object used to query {@code JSONObject} documents.
     * @param primaryKeyIterator an {@code Iterator} of {@link PrimaryKey} object.
     */
    public JSONObjectIterable(DatabaseTable databaseTable, Iterator<PrimaryKey> primaryKeyIterator) {
        this.primaryKeyIterator = primaryKeyIterator;
        this.databaseTable = databaseTable;
    }

    /**
     * Returns {@code true} if there is no element left
     * to iterate; {@code false} otherwise.
     *
     * @return {@code true} if the {@code Iterable} is empty.
     */
    public boolean isEmpty() {
        return !this.primaryKeyIterator.hasNext();
    }

    /**
     * Returns an iterator over elements of type {@code JSONObject}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<JSONObject> iterator() {
        return new Iterator<JSONObject>() {
            @Override
            public boolean hasNext() {
                return JSONObjectIterable.this.primaryKeyIterator.hasNext();
            }

            @Override
            public JSONObject next() {
                PrimaryKey primaryKey = JSONObjectIterable.this.primaryKeyIterator.next();
                if (primaryKey == null) {
                    return null;
                }

                JSONObject json = null;
                try {
                    json = JSONObjectIterable.this.databaseTable.select(primaryKey);
                } catch(Exception ex) {
                    LOGGER.error(String.format("Error occurred while selecting the JSON document for table: %s, ID: %s",
                            JSONObjectIterable.this.databaseTable.getTableName(), primaryKey.toString()), ex);
                }

                return json;
            }
        };
    }

}
