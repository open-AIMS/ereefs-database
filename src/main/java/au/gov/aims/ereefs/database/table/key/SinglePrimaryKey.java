/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table.key;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.Map;

/**
 * Object representing a single attribute
 * primary key in a database table.
 *
 * <p>The key value attribute is usually named {@code _id}.</p>
 */
public class SinglePrimaryKey extends PrimaryKey {

    /**
     * Creates a primary key using an attribute name and value.
     *
     * @param keyName the name of the ID attribute. Usually {@code _id}.
     * @param keyValue the value of the ID.
     */
    public SinglePrimaryKey(String keyName, Object keyValue) {
        super(SinglePrimaryKey.buildKeyValueMap(keyName, keyValue));
    }

    private static Map<String, Object> buildKeyValueMap(String keyName, Object keyValue) {
        Map<String, Object> keyValueMap = new HashMap<String, Object>();
        keyValueMap.put(keyName, keyValue);
        return keyValueMap;
    }

    /**
     * Returns the name of the ID attribute.
     * @return the name of the ID attribute.
     */
    public String getKeyName() {
        Map<String, Object> values = this.getKeyValues();
        for (String key : values.keySet()) {
            return key;
        }
        return null;
    }

    /**
     * Returns the value of the ID.
     * @return the value of the ID.
     */
    public Object getKeyValue() {
        String keyName = this.getKeyName();
        if (keyName == null) {
            return null;
        }
        return this.getKeyValues().get(keyName);
    }

    /**
     * Returns a {@code Bson} filter to use to get the
     * document associated with this ID.
     *
     * @return the ID {@code Bson} filter.
     */
    public Bson getFilter() {
        return Filters.eq(this.getKeyName(), this.getKeyValue());
    }
}
