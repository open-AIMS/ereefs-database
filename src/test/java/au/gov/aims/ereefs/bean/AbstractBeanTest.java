/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AbstractBeanTest {

    @Test
    public void testSafeIdValue() {
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put("a-zA-Z0-9-_/", "a-zA-Z0-9-_/");
        expectedMap.put("downloads/gbr4_v2_redownload/fx3-gbr4_v2/gbr4_simple_2018-10.nc", "downloads/gbr4_v2_redownload/fx3-gbr4_v2/gbr4_simple_2018-10_nc");
        expectedMap.put("${a.b}\\c&", "_a_b_c_");

        for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
            Assert.assertEquals("AbstractBean.safeIdValue returned unexpected results.",
                    expectedEntry.getValue(), AbstractBean.safeIdValue(expectedEntry.getKey()));
        }
    }
}
