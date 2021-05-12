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
package au.gov.aims.ereefs.database.manager;

import au.gov.aims.ereefs.database.DatabaseTestBase;
import au.gov.aims.ereefs.database.table.DatabaseTableTest;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import com.amazonaws.util.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MetadataManagerTest extends DatabaseTestBase {

    @Test
    public void testInsertUpdateMetadata() throws Exception {
        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);

        // Insert metadata
        InputStream smallMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/small.nc.json");
        String smallMetadataStr = IOUtils.toString(smallMetadataInputStream);
        JSONObject smallMetadataJson = new JSONObject(smallMetadataStr);
        JSONObject saved = metadataManager.save(smallMetadataJson);
        System.out.println("SAVED: " + (saved == null ? "NULL" : saved.toString(4)));

        InputStream gbr1MetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/gbr1.nc.json");
        String gbr1MetadataStr = IOUtils.toString(gbr1MetadataInputStream);
        JSONObject gbr1MetadataJson = new JSONObject(gbr1MetadataStr);
        metadataManager.save(gbr1MetadataJson);

        InputStream randomMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/random_data.nc.json");
        String randomMetadataStr = IOUtils.toString(randomMetadataInputStream);
        JSONObject randomMetadataJson = new JSONObject(randomMetadataStr);
        metadataManager.save(randomMetadataJson);


        // Retrieve saved metadata
        JSONObject found = metadataManager.select("downloads/small/small_nc");
        Assert.assertNotNull("No metadata found", found);

        Map<String, JSONObject> jsonMetadatadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonMetadatas = metadataManager.selectByDefinitionId(
                MetadataManager.MetadataType.NETCDF, "downloads/small");

        Assert.assertNotNull(jsonMetadatas);
        for (JSONObject jsonMetadata : jsonMetadatas) {
            jsonMetadatadMap.put(jsonMetadata.optString("_id"), jsonMetadata);
        }
        JSONObject jsonMetadata = jsonMetadatadMap.get("downloads/small/small_nc");
        Assert.assertNotNull("Missing metadata ID: downloads/small/small_nc", jsonMetadata);


        // Update metadata
        JSONObject jsonAttributes = smallMetadataJson.optJSONObject("attributes");
        jsonAttributes.put("newAttribute", "newValue");
        jsonAttributes.put("title", "GBR4 Hydrodynamic v2.0");
        metadataManager.save(smallMetadataJson);


        // Retrieve saved metadata
        JSONObject updated = metadataManager.select("downloads/small/small_nc");
        Assert.assertNotNull("No metadata found", updated);

        JSONObject jsonUpdatedAttributes = updated.optJSONObject("attributes");
        Assert.assertEquals("The MetadataManager could not update a NetCDF metadata. The new metadata newAttribute attribute was not found.",
                "newValue", jsonUpdatedAttributes.optString("newAttribute"));
        Assert.assertEquals("The MetadataManager could not update a NetCDF metadata. The updated metadata title attribute was not found.",
                "GBR4 Hydrodynamic v2.0", jsonUpdatedAttributes.optString("title"));
    }

    @Test
    public void testSelectValidNetCDFMetadata() throws Exception {
        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);

        InputStream smallMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/small.nc.json");
        String smallMetadataStr = IOUtils.toString(smallMetadataInputStream);
        JSONObject smallMetadataJson = new JSONObject(smallMetadataStr);
        metadataManager.save(smallMetadataJson);

        InputStream gbr1MetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/gbr1.nc.json");
        String gbr1MetadataStr = IOUtils.toString(gbr1MetadataInputStream);
        JSONObject gbr1MetadataJson = new JSONObject(gbr1MetadataStr);
        metadataManager.save(gbr1MetadataJson);

        InputStream randomMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/random_data.nc.json");
        String randomMetadataStr = IOUtils.toString(randomMetadataInputStream);
        JSONObject randomMetadataJson = new JSONObject(randomMetadataStr);
        metadataManager.save(randomMetadataJson);


        Map<String, JSONObject> jsonMetadatadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonMetadatas = metadataManager.selectValidByDefinitionId(
                MetadataManager.MetadataType.NETCDF, "downloads/small");

        Assert.assertNotNull(jsonMetadatas);
        for (JSONObject jsonMetadata : jsonMetadatas) {
            jsonMetadatadMap.put(jsonMetadata.optString("_id"), jsonMetadata);
        }
        Assert.assertNotNull("No valid metadata found for definition ID: downloads/small", jsonMetadatadMap);
        JSONObject foundSmall = jsonMetadatadMap.get("downloads/small/small_nc");
        Assert.assertNotNull("Metadata ID downloads/small/small_nc was not found", foundSmall);

        Assert.assertEquals("Wrong number of valid metadata found for definition ID: downloads/small", 1, jsonMetadatadMap.size());
    }

    /**
     * This test verify if the system still works as expected when it have to
     * work with IDs which doesn't follows ID standards.
     * @throws Exception
     */
    @Test
    public void testSelectNetCDFMetadataWithInvalidId() throws Exception {
        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);

        InputStream smallMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/small.nc.json");
        String smallMetadataStr = IOUtils.toString(smallMetadataInputStream);
        JSONObject smallMetadataJson = new JSONObject(smallMetadataStr);
        smallMetadataJson.put("_id", "downloads/small/small.nc"); // old IDs used to have dots in them
        DatabaseTableTest.unchecked_insert(metadataManager.getTable(), smallMetadataJson);

        InputStream gbr1MetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/gbr1.nc.json");
        String gbr1MetadataStr = IOUtils.toString(gbr1MetadataInputStream);
        JSONObject gbr1MetadataJson = new JSONObject(gbr1MetadataStr);
        gbr1MetadataJson.put("_id", "downloads/gbr1_v2/gbr1_simple_2014-12-02.nc"); // old IDs used to have dots in them
        DatabaseTableTest.unchecked_insert(metadataManager.getTable(), gbr1MetadataJson);

        InputStream randomMetadataInputStream = DownloadManagerTest.class.getClassLoader().getResourceAsStream("metadata/random_data.nc.json");
        String randomMetadataStr = IOUtils.toString(randomMetadataInputStream);
        JSONObject randomMetadataJson = new JSONObject(randomMetadataStr);
        metadataManager.save(randomMetadataJson);


        Map<String, JSONObject> jsonMetadatadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonMetadatas = metadataManager.selectAll(
                MetadataManager.MetadataType.NETCDF);

        Assert.assertNotNull(jsonMetadatas);
        for (JSONObject jsonMetadata : jsonMetadatas) {
            jsonMetadatadMap.put(jsonMetadata.optString("_id"), jsonMetadata);
        }

        JSONObject foundSmall = jsonMetadatadMap.get("downloads/small/small.nc");
        Assert.assertNotNull("Metadata ID downloads/small/small.nc was not found", foundSmall);

        JSONObject foundGBR1Simple = jsonMetadatadMap.get("downloads/gbr1_v2/gbr1_simple_2014-12-02.nc");
        Assert.assertNotNull("Metadata ID downloads/gbr1_v2/gbr1_simple_2014-12-02.nc was not found", foundGBR1Simple);

        JSONObject foundRandom = jsonMetadatadMap.get("downloads/small/random_data_nc");
        Assert.assertNotNull("Metadata ID downloads/small/random_data_nc was not found", foundRandom);

        Assert.assertEquals("Wrong number of NetCDF metadata found in the database", 3, jsonMetadatadMap.size());
    }
}
