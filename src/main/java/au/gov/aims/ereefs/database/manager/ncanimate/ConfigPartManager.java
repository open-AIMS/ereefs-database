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
package au.gov.aims.ereefs.database.manager.ncanimate;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.manager.AbstractCompositeKeyManager;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.table.DatabaseCompositeKeyTable;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import com.mongodb.client.model.Filters;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Manager used to manipulate {@code ereefs-ncanimate2} configuration parts,
 * saved in the database.
 *
 * <p>Configuration parts are fragments of configuration which
 * can be re-used in multiple NcAnimate configuration files.</p>
 *
 * <p>See {@link au.gov.aims.ereefs.database.manager.AbstractManager}</p>
 */
public class ConfigPartManager extends AbstractCompositeKeyManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigPartManager.class);

    public static final String TABLE_NAME = "ncanimate_config_parts";
    private static final String TABLE_PRIMARY_ID_KEYNAME = "_id";
    private static final String TABLE_FIRST_COMPOSITE_ID_KEYNAME = "datatype";
    private static final String TABLE_SECOND_COMPOSITE_ID_KEYNAME = "id";

    /**
     * @deprecated Use ConfigPartManager(DatabaseClient dbClient, CacheStrategy cacheStrategy)
     */
    @Deprecated
    public ConfigPartManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code ConfigPartManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public ConfigPartManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_PRIMARY_ID_KEYNAME, TABLE_FIRST_COMPOSITE_ID_KEYNAME, TABLE_SECOND_COMPOSITE_ID_KEYNAME);
    }

    /**
     * Returns all the configuration parts of a given type.
     *
     * @param datatype the type of configuration part.
     * @return a {@link JSONObjectIterable} object used to request each configuration part one by one, as needed.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObjectIterable selectAll(Datatype datatype) throws Exception {
        if (datatype == null) {
            throw new IllegalArgumentException("Missing datatype");
        }

        DatabaseCompositeKeyTable table = this.getTable();
        JSONObjectIterable iterable = table.select(
            Filters.eq(TABLE_PRIMARY_ID_KEYNAME + "." + TABLE_FIRST_COMPOSITE_ID_KEYNAME, datatype.name()));

        if (iterable.isEmpty() && !table.exists()) {
            throw new RuntimeException(String.format("Table %s doesn't exists", TABLE_NAME));
        }

        return iterable;
    }

    /**
     * Select a configuration part from the database.
     *
     * @param datatype the type of configuration part.
     * @param id the id of the configuration part.
     * @return the {@code JSONObject} of the configuration part.
     * @throws Exception if the table doesn't exists or the database is unreachable.
     */
    public JSONObject select(Datatype datatype, String id) throws Exception {
        if (datatype == null) {
            throw new IllegalArgumentException("Missing datatype");
        }

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing id");
        }

        return this.select(datatype.name(), id);
    }

    /**
     * List of type of configuration part.
     */
    public enum Datatype {
        PANEL, LEGEND, CANVAS, LAYER, REGION, VARIABLE, INPUT, RENDER,
        BBOX, DEFAULTS, PADDING, POSITION, TEXT
    }
}
