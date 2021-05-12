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

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigManagerTest extends ConfigManagerTestBase {

    @Before
    public void insertData() throws Exception {
        super.insertDummyData();
        super.insertFakeHourlyData(100);
    }

    @Test
    public void testSelectConfig() throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CACHE_STRATEGY);

        String productId = "gbr4_v2_temp-wind-salt-current";

        JSONObject config = configManager.select(productId);
        Assert.assertNotNull(String.format("NcAnimate configuration %s is missing", productId), config);
        Assert.assertEquals("Wrong NcAnimate config ID", productId, config.optString("_id", null));

        String configLastModifiedStr = config.optString("lastModified", null);
        Assert.assertNotNull("NcAnimate config lastModified is null", configLastModifiedStr);
        long configLastModified = DateTime.parse(configLastModifiedStr).getMillis();
        Assert.assertEquals("Wrong NcAnimate config lastModified.", 1565164080000L, configLastModified);
    }
}
