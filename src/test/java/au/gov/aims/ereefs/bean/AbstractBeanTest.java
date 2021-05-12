/*
 *  Copyright (C) 2021 Australian Institute of Marine Science
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
