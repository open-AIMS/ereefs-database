/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager.ncanimate;

import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConfigPartManagerTest extends ConfigManagerTestBase {

    @Before
    public void insertData() throws Exception {
        super.insertDummyData();
        super.insertFakeHourlyData(100);
    }

    @Test
    public void testSelectAll() throws Exception {
        int expectedLayerCount = 6;
        int expectedCanvasCount = 1;
        int expectedBBoxCount = 0;

        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        // Layers
        JSONObjectIterable layers = configPartManager.selectAll(ConfigPartManager.Datatype.LAYER);
        Assert.assertNotNull("Layers query returned null", layers);
        Assert.assertFalse("Layers query returned empty result", layers.isEmpty());

        int layerCount = 0;
        for (JSONObject jsonLayer : layers) {
            layerCount++;
        }
        Assert.assertEquals("Layers query returned unexpected number of layers", expectedLayerCount, layerCount);

        // Canvas
        JSONObjectIterable canvas = configPartManager.selectAll(ConfigPartManager.Datatype.CANVAS);
        Assert.assertNotNull("Canvas query returned null", canvas);
        Assert.assertFalse("Canvas query returned empty result", canvas.isEmpty());

        int canvasCount = 0;
        for (JSONObject jsonCanvas : canvas) {
            canvasCount++;
        }
        Assert.assertEquals("Canvas query returned unexpected number of canvas", expectedCanvasCount, canvasCount);

        // BBOX
        JSONObjectIterable bboxes = configPartManager.selectAll(ConfigPartManager.Datatype.BBOX);
        Assert.assertNotNull("BBOX query returned null", bboxes);
        Assert.assertTrue("BBOX query returned none empty result", bboxes.isEmpty());

        int bboxesCount = 0;
        for (JSONObject jsonBBoxes : bboxes) {
            bboxesCount++;
        }
        Assert.assertEquals("BBOX query returned unexpected number of BBOX", expectedBBoxCount, bboxesCount);
    }

    @Test
    public void testSelectGbr4Layer() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        String gbr4LayerId = "ereefs-model_gbr4-v2";

        JSONObject jsonGbr4Layer = configPartManager.select(ConfigPartManager.Datatype.LAYER, gbr4LayerId);
        Assert.assertNotNull(String.format("Missing layer ID %s", gbr4LayerId), jsonGbr4Layer);
        JSONObject id = jsonGbr4Layer.optJSONObject("_id");
        Assert.assertEquals("Wrong gbr4 layer ID", gbr4LayerId, id.optString("id", null));
        Assert.assertEquals("Wrong gbr4 layer datatype", "LAYER", id.optString("datatype", null));
        Assert.assertEquals("Wrong gbr4 layer type", "NETCDF", jsonGbr4Layer.optString("type", null));

        Assert.assertEquals("Wrong gbr4 layer input", "downloads/gbr4_v2", jsonGbr4Layer.optString("input", null));

        String gbr4LayerLastModified = jsonGbr4Layer.optString("lastModified", null);
        Assert.assertEquals("Wrong gbr4 layer config lastModified.", "2019-08-14T16:10:00.000+08:00", gbr4LayerLastModified);
    }

    @Test
    public void testSelectCitiesLayer() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        String citiesLayerId = "cities";

        JSONObject jsonCitiesLayer = configPartManager.select(ConfigPartManager.Datatype.LAYER, citiesLayerId);
        Assert.assertNotNull(String.format("Missing layer ID %s", citiesLayerId), jsonCitiesLayer);
        JSONObject id = jsonCitiesLayer.optJSONObject("_id");
        Assert.assertEquals("Wrong cities layer ID", citiesLayerId, id.optString("id", null));
        Assert.assertEquals("Wrong cities layer datatype", "LAYER", id.optString("datatype", null));
        Assert.assertEquals("Wrong cities layer type", "CSV", jsonCitiesLayer.optString("type", null));

        Assert.assertEquals("Wrong cities layer datasource", "s3://ncanimate/layers/World_NE_10m-cities_V3_Ranked_NRM.csv", jsonCitiesLayer.optString("datasource", null));
        Assert.assertEquals("Wrong cities layer style", "s3://ncanimate/styles/World_NE_10m-cities_V3_Ranked_qld.sld", jsonCitiesLayer.optString("style", null));
        Assert.assertEquals("Wrong cities layer longitudeColumn", "LONGITUDE", jsonCitiesLayer.optString("longitudeColumn", null));
        Assert.assertEquals("Wrong cities layer latitudeColumn", "LATITUDE", jsonCitiesLayer.optString("latitudeColumn", null));

        String citiesLayerLastModified = jsonCitiesLayer.optString("lastModified", null);
        Assert.assertEquals("Wrong cities layer config lastModified.", "2019-08-14T16:10:00.000+08:00", citiesLayerLastModified);
    }

    @Test
    public void testSelectWMSLayer() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        String wmsLayerId = "wms";

        JSONObject jsonWMSLayer = configPartManager.select(ConfigPartManager.Datatype.LAYER, wmsLayerId);
        Assert.assertNotNull(String.format("Missing layer ID %s", wmsLayerId), jsonWMSLayer);
        JSONObject id = jsonWMSLayer.optJSONObject("_id");
        Assert.assertEquals("Wrong wms layer ID", wmsLayerId, id.optString("id", null));
        Assert.assertEquals("Wrong wms layer datatype", "LAYER", id.optString("datatype", null));
        Assert.assertEquals("Wrong wms layer type", "WMS", jsonWMSLayer.optString("type", null));

        Assert.assertEquals("Wrong wms layer server", "https://maps.eatlas.org.au/maps/ows", jsonWMSLayer.optString("server", null));
        Assert.assertEquals("Wrong wms layer layerName", "ea:basemap", jsonWMSLayer.optString("layerName", null));
        Assert.assertEquals("Wrong wms layer styleName", "nolabel", jsonWMSLayer.optString("styleName", null));

        String wmsLayerLastModified = jsonWMSLayer.optString("lastModified", null);
        Assert.assertEquals("Wrong wms layer config lastModified.", "2019-08-14T16:10:00.000+08:00", wmsLayerLastModified);
    }

    @Test
    public void testSelectRegion() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        String regionId = "qld";
        JSONObject jsonRegion = configPartManager.select(ConfigPartManager.Datatype.REGION, regionId);

        System.out.println(jsonRegion);
        Assert.assertNotNull("Could not retrieve Queensland region", jsonRegion);
    }
}
