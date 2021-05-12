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
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.AbstractSingleKeyManager;

/**
 * Manager used to manipulate {@code ereefs-ncanimate2} configuration files,
 * saved in the database.
 *
 * <p>See {@link au.gov.aims.ereefs.database.manager.AbstractManager}</p>
 */
public class ConfigManager extends AbstractSingleKeyManager {
    public static final String TABLE_NAME = "ncanimate_config";
    private static final String TABLE_ID_KEYNAME = "_id";

    /**
     * @deprecated Use {@link ConfigManager(DatabaseClient, CacheStrategy)}
     */
    @Deprecated
    public ConfigManager(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Construct a {@code ConfigManager}.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public ConfigManager(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        super(dbClient, TABLE_NAME, cacheStrategy, TABLE_ID_KEYNAME);
    }
}
