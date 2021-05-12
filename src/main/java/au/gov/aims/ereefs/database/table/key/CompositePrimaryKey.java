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
package au.gov.aims.ereefs.database.table.key;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.Iterator;
import java.util.Map;

/**
 * Object representing a composite primary key
 * in a database table.
 *
 * <p>The primary key is identified with a name, usually {@code _id}.
 * Its value is a JSON object containing an arbitrary number of key values.</p>
 *
 * <p>Composite primary key reference: <a href="https://docs.mongodb.com/manual/core/index-compound/" target="_blank">https://docs.mongodb.com/manual/core/index-compound/</a></p>
 */
public class CompositePrimaryKey extends PrimaryKey {
    private String primaryKeyName;

    /**
     * Creates a primary key using an attribute name and composite value.
     *
     * @param primaryKeyName the name of the ID attribute. Usually {@code _id}.
     * @param compositeKeyValues the composite value of the ID, represented as a {@code Map}.
     */
    public CompositePrimaryKey(String primaryKeyName, Map<String, Object> compositeKeyValues) {
        super(compositeKeyValues);
        this.primaryKeyName = primaryKeyName;
    }

    /**
     * Returns a {@code Bson} filter to use to get the
     * document associated with this ID.
     *
     * @return the ID {@code Bson} filter.
     */
    public Bson getFilter() {
        return Filters.and(new Iterable<Bson>() {
            @Override
            public Iterator<Bson> iterator() {
                Map<String, Object> keyValues = CompositePrimaryKey.this.getKeyValues();
                String prefix = CompositePrimaryKey.this.primaryKeyName + ".";
                Iterator<String> keyIterator = keyValues.keySet().iterator();
                return new Iterator<Bson>() {
                    @Override
                    public boolean hasNext() {
                        return keyIterator.hasNext();
                    }

                    @Override
                    public Bson next() {
                        String key = keyIterator.next();
                        return Filters.eq(prefix + key, keyValues.get(key));
                    }
                };
            }
        });
    }
}
