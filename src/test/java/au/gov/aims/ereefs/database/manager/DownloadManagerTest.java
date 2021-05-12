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
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import au.gov.aims.json.JSONUtils;
import com.amazonaws.util.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DownloadManagerTest extends DatabaseTestBase {
    private static final Logger LOGGER = Logger.getLogger(DownloadManagerTest.class);

    @Test
    public void testSelectEmptyDB() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);
        downloadManager.clearCache();

        JSONObject jsonDownload = downloadManager.select("downloads/gbr4_v2");
        Assert.assertNull(jsonDownload);
    }

    @Test
    public void testInsertDownload() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);

        JSONObject inserted = downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_2-0.json"))));

        System.out.println(inserted);

        Assert.assertNotNull(inserted);
    }

    @Test
    public void testInsertSelectDownload() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_2-0.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_bgc_924.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_v2.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_bgc_924.json"))));


        JSONObject jsonNotFoundDownload = downloadManager.select("NOT_FOUND");
        Assert.assertNull(jsonNotFoundDownload);

        JSONObject jsonGbr1v2Download = downloadManager.select("downloads/gbr1_2-0");
        Assert.assertNotNull(jsonGbr1v2Download);

        JSONObject jsonGbr1BgcDownload = downloadManager.select("downloads/gbr1_bgc_924");
        Assert.assertNotNull(jsonGbr1BgcDownload);

        JSONObject jsonGbr4v2Download = downloadManager.select("downloads/gbr4_v2");
        Assert.assertNotNull(jsonGbr4v2Download);

        JSONObject jsonGbr4BgcDownload = downloadManager.select("downloads/gbr4_bgc_924");
        Assert.assertNotNull(jsonGbr4BgcDownload);

        // TODO Test some attributes


        Map<String, JSONObject> jsonDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonDownloads = downloadManager.selectAll();
        Assert.assertNotNull(jsonDownloads);
        for (JSONObject jsonDownload : jsonDownloads) {
            jsonDownloadMap.put(jsonDownload.optString("_id"), jsonDownload);
        }
        Assert.assertEquals(4, jsonDownloadMap.size());

        LOGGER.debug(jsonDownloadMap.get("downloads/gbr4_v2").toString(4));

        Map<String, JSONObject> jsonEnabledDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonEnabledDownloads = downloadManager.selectAllEnabled();
        Assert.assertNotNull(jsonEnabledDownloads);
        for (JSONObject jsonEnabledDownload : jsonEnabledDownloads) {
            jsonEnabledDownloadMap.put(jsonEnabledDownload.optString("_id"), jsonEnabledDownload);
        }
        Assert.assertEquals(3, jsonEnabledDownloadMap.size());

        LOGGER.debug(jsonEnabledDownloadMap.get("downloads/gbr4_v2").toString(4));
    }

    @Test
    public void testInsertDeleteDownload() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);


        JSONObject jsonNotFoundDownload = downloadManager.delete("NOT_FOUND");
        Assert.assertNull(jsonNotFoundDownload);


        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_2-0.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_bgc_924.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_v2.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_bgc_924.json"))));


        JSONObject jsonGbr1v2Download = downloadManager.delete("downloads/gbr1_2-0");
        Assert.assertNotNull(jsonGbr1v2Download);

        JSONObject jsonGbr1v2AgainDownload = downloadManager.delete("downloads/gbr1_2-0");
        Assert.assertNull(jsonGbr1v2AgainDownload);


        Map<String, JSONObject> jsonDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonDownloads = downloadManager.selectAll();
        Assert.assertNotNull(jsonDownloads);
        for (JSONObject jsonDownload : jsonDownloads) {
            jsonDownloadMap.put(jsonDownload.optString("_id"), jsonDownload);
        }
        Assert.assertEquals(3, jsonDownloadMap.size());

        LOGGER.debug(jsonDownloadMap.get("downloads/gbr4_v2").toString(4));


        Map<String, JSONObject> jsonEnabledDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonEnabledDownloads = downloadManager.selectAllEnabled();
        Assert.assertNotNull(jsonEnabledDownloads);
        for (JSONObject jsonEnabledDownload : jsonEnabledDownloads) {
            jsonEnabledDownloadMap.put(jsonEnabledDownload.optString("_id"), jsonEnabledDownload);
        }
        Assert.assertEquals(2, jsonEnabledDownloadMap.size());

        System.out.println(jsonEnabledDownloadMap.get("downloads/gbr4_v2").toString(4));
    }


    @Test
    public void testInsertUpdateDownload() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);


        JSONObject jsonNotFoundDownload = downloadManager.delete("NOT_FOUND");
        Assert.assertNull(jsonNotFoundDownload);


        String jsonGbr1v2DownloadStr = IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_2-0.json"));
        downloadManager.save(new JSONObject(jsonGbr1v2DownloadStr));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_bgc_924.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_v2.json"))));

        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_bgc_924.json"))));


        JSONObject jsonGbr1v2DownloadUpdated = new JSONObject(jsonGbr1v2DownloadStr);
        jsonGbr1v2DownloadUpdated.put("enabled", false);
        jsonGbr1v2DownloadUpdated.put("newProperty", "value");
        // Verify if we can use reserved keywords in the JSON object
        jsonGbr1v2DownloadUpdated.put("data", "Reserved Keyword!");
        JSONObject updatedJsonItem = downloadManager.save(jsonGbr1v2DownloadUpdated);

        Assert.assertTrue(updatedJsonItem.has("enabled"));
        Assert.assertFalse(updatedJsonItem.optBoolean("enabled", false));

        Assert.assertTrue(updatedJsonItem.has("newProperty"));
        Assert.assertEquals("value", updatedJsonItem.optString("newProperty", null));

        Assert.assertTrue(updatedJsonItem.has("data"));
        Assert.assertEquals("Reserved Keyword!", updatedJsonItem.optString("data", null));


        Map<String, JSONObject> jsonDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonDownloads = downloadManager.selectAll();
        Assert.assertNotNull(jsonDownloads);
        for (JSONObject jsonDownload : jsonDownloads) {
            jsonDownloadMap.put(jsonDownload.optString("_id"), jsonDownload);
        }
        Assert.assertEquals(4, jsonDownloadMap.size());


        Map<String, JSONObject> jsonEnabledDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonEnabledDownloads = downloadManager.selectAllEnabled();
        Assert.assertNotNull(jsonEnabledDownloads);
        for (JSONObject jsonEnabledDownload : jsonEnabledDownloads) {
            jsonEnabledDownloadMap.put(jsonEnabledDownload.optString("_id"), jsonEnabledDownload);
        }
        Assert.assertEquals(2, jsonEnabledDownloadMap.size());
    }


    @Test
    public void testSelectAllEnabled() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);


        // Enabled
        JSONObject jsonGbr1v2Download = new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_2-0.json")));
        downloadManager.save(jsonGbr1v2Download);

        // Disabled
        downloadManager.save(new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr1_bgc_924.json"))));

        // Enabled
        JSONObject jsonGbr4v2Download = new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_v2.json")));
        downloadManager.save(jsonGbr4v2Download);

        // Enabled
        JSONObject jsonGbr4bgcDownload = new JSONObject(IOUtils.toString(
                DownloadManagerTest.class.getClassLoader().getResourceAsStream("download/gbr4_bgc_924.json")));
        downloadManager.save(jsonGbr4bgcDownload);


        Map<String, JSONObject> jsonEnabledDownloadMap = new HashMap<String, JSONObject>();
        JSONObjectIterable jsonEnabledDownloads = downloadManager.selectAllEnabled();
        Assert.assertNotNull(jsonEnabledDownloads);
        for (JSONObject jsonEnabledDownload : jsonEnabledDownloads) {
            jsonEnabledDownloadMap.put(jsonEnabledDownload.optString("_id"), jsonEnabledDownload);
        }

        Assert.assertNotNull("List of enabled download definitions is null", jsonEnabledDownloadMap);
        Assert.assertEquals("List of enabled download definitions is null", 3, jsonEnabledDownloadMap.size());

        for (Map.Entry<String, JSONObject> enabledDownloadEntry : jsonEnabledDownloadMap.entrySet()) {
            String downloadId = enabledDownloadEntry.getKey();
            Assert.assertNotNull("Null download definition ID found in the database", downloadId);

            JSONObject downloadJson = enabledDownloadEntry.getValue();
            Assert.assertNotNull(String.format("Download definition ID %s has a null JSON document", downloadId), downloadJson);

            LOGGER.debug(String.format("ID: %s Value: %s", downloadId, downloadJson.toString(4)));

            switch (downloadId) {
                case "downloads/gbr1_2-0":
                    Assert.assertTrue(
                            String.format(
                                "The JSON document associated with download definition ID %s is wrong.%nExpected: %s%nFound: %s",
                                downloadId,
                                jsonGbr1v2Download.toString(4),
                                downloadJson.toString(4)
                            ),
                            JSONUtils.equals(jsonGbr1v2Download, downloadJson)
                    );
                    break;

                case "downloads/gbr4_v2":
                    Assert.assertTrue(
                            String.format(
                                "The JSON document associated with download definition ID %s is wrong.%nExpected: %s%nFound: %s",
                                downloadId,
                                jsonGbr4v2Download.toString(4),
                                downloadJson.toString(4)
                            ),
                            JSONUtils.equals(jsonGbr4v2Download, downloadJson)
                    );
                    break;

                case "downloads/gbr4_bgc_924":
                    Assert.assertTrue(
                            String.format(
                                "The JSON document associated with download definition ID %s is wrong.%nExpected: %s%nFound: %s",
                                downloadId,
                                jsonGbr4bgcDownload.toString(4),
                                downloadJson.toString(4)
                            ),
                            JSONUtils.equals(jsonGbr4bgcDownload, downloadJson)
                    );
                    break;

                default:
                    Assert.fail(String.format("Unexpected download definition ID: %s%n%s", downloadId, downloadJson.toString(4)));
            }
        }
    }
}
