/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
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
