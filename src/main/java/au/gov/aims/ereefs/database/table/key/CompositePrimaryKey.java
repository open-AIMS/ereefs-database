/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
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
