/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata.netcdf;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class NetCDFMetadataBeanTest {
    private static final Logger LOGGER = Logger.getLogger(NetCDFMetadataBeanTest.class);

    @Test
    public void testSerialiseDeserialise() {
        URL netCDFFileUrl = NetCDFMetadataBeanTest.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFile = new File(netCDFFileUrl.getFile());

        String definitionId = "downloads/small";
        String datasetId = "small.nc";
        URI fileURI = new File("/tmp/netcdfFiles/small.nc").toURI();

        long expectedLastModified = netCDFFile.lastModified();
        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, expectedLastModified);
        JSONObject expectedJsonMetadata = metadata.toJSON();

        NetCDFMetadataBean deserialisedMetadata = new NetCDFMetadataBean(expectedJsonMetadata);
        JSONObject actualJsonMetadata = deserialisedMetadata.toJSON();

        // Check if the lastModified is saved as a parsable DateTime
        String lastModifiedStr = expectedJsonMetadata.optString("lastModified", null);
        Assert.assertNotNull("The metadata JSON does not contain a lastModified date String.", lastModifiedStr);
        try {
            DateTime lastModified = DateTime.parse(lastModifiedStr);
            Assert.assertNotNull("Parsed lastModified date String is null.", lastModified);
            Assert.assertEquals(String.format("Wrong parsed lastModified date String: %s", lastModifiedStr),
                    expectedLastModified, lastModified.getMillis());
        } catch(Exception ex) {
            LOGGER.error(ex);
            Assert.fail(String.format("Error occurred while parsing the lastModified date String: %s", lastModifiedStr));
        }

        Assert.assertEquals(expectedJsonMetadata.toString(4), actualJsonMetadata.toString(4));
    }

    @Test
    public void testLoadMetadata() {
        URL netCDFFileUrl = NetCDFMetadataBeanTest.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFile = new File(netCDFFileUrl.getFile());
        Assert.assertTrue("The small.nc file can not be found.", netCDFFile.exists());
        Assert.assertTrue("The small.nc file is not readable.", netCDFFile.canRead());

        String definitionId = "downloads/small";
        String datasetId = "small.nc";
        URI fileURI = new File("/tmp/netcdfFiles/small.nc").toURI();

        long expectedLastModified = netCDFFile.lastModified();
        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, expectedLastModified);
        LOGGER.debug(metadata);

        Assert.assertNotNull("The metadata received from the small.nc file is null", metadata);

        String checksum = metadata.getChecksum();
        Assert.assertNotNull("The metadata checksum is null", checksum);
        Assert.assertEquals("The metadata checksum is wrong", "MD5:1a3dfb44f1ae56acf52ac39e6cb9a93e", checksum);

        // Test last modified
        long lastModified = metadata.getLastModified();
        Assert.assertEquals("Wrong last modified.", expectedLastModified, lastModified);

        Assert.assertEquals(NetCDFMetadataBean.getUniqueDatasetId(definitionId, datasetId), metadata.getId());
        Assert.assertEquals(definitionId, metadata.getDefinitionId());
        Assert.assertEquals(datasetId, metadata.getDatasetId());
        Assert.assertEquals(fileURI, metadata.getFileURI());
        Assert.assertEquals(netCDFFile.lastModified(), metadata.getLastModified());
        Assert.assertEquals(NetCDFMetadataBean.Status.VALID, metadata.getStatus());
        Assert.assertNull("Error message is not null", metadata.getErrorMessage());

        // Test some attributes
        JSONObject jsonAttributes = metadata.getAttributes();
        Assert.assertNotNull("Map of attributes is null", jsonAttributes);

        Assert.assertEquals("CF-1.0", jsonAttributes.optString("Conventions", null));
        Assert.assertEquals("http://marlin.csiro.au/geonetwork/srv/eng/search?&uuid=72020224-f086-434a-bbe9-a222c8e5cf0d",
                jsonAttributes.optString("metadata_link", null));
        Assert.assertEquals("GBR 4km resolution grid", jsonAttributes.optString("paramhead", null));
        Assert.assertEquals("prm/gbr4_pear_0.prm", jsonAttributes.optString("paramfile", null));
        Assert.assertEquals("v1.1 rev(5320M)", jsonAttributes.optString("shoc_version", null));
        Assert.assertEquals("GBR4 Hydro", jsonAttributes.optString("title", null));


        // Test some variables
        Map<String, VariableMetadataBean> variables = metadata.getVariableMetadataBeanMap();
        Assert.assertNotNull("Map of variables is null", variables);

        // Test variables wspeed_u:wspeed_v-mag and wspeed_u:wspeed_v-dir (wind components)
        VariableMetadataBean windMagVariable = variables.get("wspeed_u:wspeed_v-mag");
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-mag is null", windMagVariable);
        Assert.assertEquals("Wrong variable ID", "wspeed_u:wspeed_v-mag", windMagVariable.getId());
        Assert.assertTrue("Variable variable wspeed_u:wspeed_v-mag is not scalar", windMagVariable.isScalar());
        VariableMetadataBean windMagParentVariable = windMagVariable.getParent();
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-mag have no parent", windMagParentVariable);
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-mag parent", "wspeed_u:wspeed_v-group", windMagParentVariable.getId());

        VariableMetadataBean windDirVariable = variables.get("wspeed_u:wspeed_v-dir");
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-dir is null", windDirVariable);
        Assert.assertEquals("Wrong variable ID", "wspeed_u:wspeed_v-dir", windDirVariable.getId());
        Assert.assertTrue("Variable variable wspeed_u:wspeed_v-dir is not scalar", windDirVariable.isScalar());
        VariableMetadataBean windDirParentVariable = windDirVariable.getParent();
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-dir have no parent", windDirParentVariable);
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-dir parent", "wspeed_u:wspeed_v-group", windDirParentVariable.getId());

        // Test variable wspeed_u:wspeed_v-group (wind)
        VariableMetadataBean windVariable = variables.get("wspeed_u:wspeed_v-group");
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-group is null", windVariable);
        Assert.assertEquals("Wrong variable ID", "wspeed_u:wspeed_v-group", windVariable.getId());
        Assert.assertFalse("Variable variable wspeed_u:wspeed_v-group is not scalar", windVariable.isScalar());

        Assert.assertNull("Variable wspeed_u:wspeed_v-group have a parent", windVariable.getParent());
        Map<String, VariableMetadataBean> windVariableChildren = windVariable.getChildren();
        Assert.assertNotNull("Variable wspeed_u:wspeed_v-group have no children", windVariableChildren);
        Assert.assertTrue("Variable wspeed_u:wspeed_v-group is missing child role mag", windVariableChildren.containsKey("mag"));
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-group mag child ID", "wspeed_u:wspeed_v-mag", windVariableChildren.get("mag").getId());
        Assert.assertTrue("Variable wspeed_u:wspeed_v-group is missing child role dir", windVariableChildren.containsKey("dir"));
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-group dir child ID", "wspeed_u:wspeed_v-dir", windVariableChildren.get("dir").getId());
        Assert.assertTrue("Variable wspeed_u:wspeed_v-group is missing child role x", windVariableChildren.containsKey("x"));
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-group x child ID", "wspeed_u", windVariableChildren.get("x").getId());
        Assert.assertTrue("Variable wspeed_u:wspeed_v-group is missing child role y", windVariableChildren.containsKey("y"));
        Assert.assertEquals("Wrong variable wspeed_u:wspeed_v-group y child ID", "wspeed_v", windVariableChildren.get("y").getId());
        Assert.assertEquals("Wrong number of children for variable wspeed_u:wspeed_v-group", 4, windVariableChildren.size());

        // Test variable temp (temperature)
        VariableMetadataBean tempVariable = variables.get("temp");
        Assert.assertNotNull("Variable temp is null", tempVariable);
        Assert.assertEquals("Wrong variable ID", "temp", tempVariable.getId());
        Assert.assertTrue("Variable temp is not scalar", tempVariable.isScalar());

        Assert.assertNull("Variable botz have a parent", tempVariable.getParent());

        ParameterBean tempParameters = tempVariable.getParameterBean();
        Assert.assertNotNull("Variable temp have no parameter", tempParameters);
        Assert.assertEquals("Wrong temp parameter standardName", "Temperature", tempParameters.getStandardName());
        Assert.assertEquals("Wrong temp parameter description", "Temperature", tempParameters.getDescription());
        Assert.assertEquals("Wrong temp parameter units", "degrees C", tempParameters.getUnits());
        Assert.assertEquals("Wrong temp parameter title", "Temperature", tempParameters.getTitle());
        Assert.assertEquals("Wrong temp parameter variableId", "temp", tempParameters.getVariableId());

        JSONObject jsonTempAttributes = tempVariable.getAttributes();
        Assert.assertNotNull("Variable temp have no attributes", jsonTempAttributes);
        Assert.assertEquals("Wrong temp attribute coordinates",
                "time zc latitude longitude",
                jsonTempAttributes.optString("coordinates", null));
        Assert.assertEquals("Wrong temp attribute substanceOrTaxon_id",
                "http://sweet.jpl.nasa.gov/2.2/matrWater.owl#SaltWater",
                jsonTempAttributes.optString("substanceOrTaxon_id", null));
        Assert.assertEquals("Wrong temp attribute scaledQuantityKind_id",
                "http://environment.data.gov.au/def/property/sea_water_temperature",
                jsonTempAttributes.optString("scaledQuantityKind_id", null));
        Assert.assertEquals("Wrong temp attribute units",
                "degrees C",
                jsonTempAttributes.optString("units", null));
        Assert.assertEquals("Wrong temp attribute medium_id",
                "http://environment.data.gov.au/def/feature/ocean",
                jsonTempAttributes.optString("medium_id", null));
        Assert.assertEquals("Wrong temp attribute unit_id",
                "http://qudt.org/vocab/unit#DegreeCelsius",
                jsonTempAttributes.optString("unit_id", null));
        Assert.assertEquals("Wrong temp attribute long_name",
                "Temperature",
                jsonTempAttributes.optString("long_name", null));
        JSONArray jsonTempAttributeChunkSizes = jsonTempAttributes.optJSONArray("_ChunkSizes");
        Assert.assertNotNull("Variable temp have no attribute _ChunkSizes", jsonTempAttributeChunkSizes);
        Assert.assertEquals("Wrong temp attribute _ChunkSizes length", 4, jsonTempAttributeChunkSizes.length());
        Assert.assertEquals("Wrong temp attribute _ChunkSizes[0]", 1, jsonTempAttributeChunkSizes.optInt(0, -1));
        Assert.assertEquals("Wrong temp attribute _ChunkSizes[1]", 1, jsonTempAttributeChunkSizes.optInt(1, -1));
        Assert.assertEquals("Wrong temp attribute _ChunkSizes[2]", 109, jsonTempAttributeChunkSizes.optInt(2, -1));
        Assert.assertEquals("Wrong temp attribute _ChunkSizes[3]", 600, jsonTempAttributeChunkSizes.optInt(3, -1));

        HorizontalDomainBean tempHorizontalDomain = tempVariable.getHorizontalDomainBean();
        Assert.assertNotNull("Variable temp have no Horizontal Domain", tempHorizontalDomain);
        Assert.assertEquals("Wrong temp Horizontal Domain minLon", 142.16879272460938, tempHorizontalDomain.getMinLon(), 0.0000001);
        Assert.assertEquals("Wrong temp Horizontal Domain maxLon", 156.88563537597656, tempHorizontalDomain.getMaxLon(), 0.0000001);
        Assert.assertEquals("Wrong temp Horizontal Domain minLat", -28.6960218, tempHorizontalDomain.getMinLat(), 0.0000001);
        Assert.assertEquals("Wrong temp Horizontal Domain maxLat", -7.0119082, tempHorizontalDomain.getMaxLat(), 0.0000001);


        // Test variable salt (salinity)
        VariableMetadataBean saltVariable = variables.get("salt");
        Assert.assertNotNull("Variable salt is null", saltVariable);
        Assert.assertEquals("Wrong variable ID", "salt", saltVariable.getId());
        Assert.assertTrue("Variable salt is not scalar", saltVariable.isScalar());

        Assert.assertNull("Variable salt have a parent", saltVariable.getParent());


        // Test variable botz (bathymetry)
        VariableMetadataBean botzVariable = variables.get("botz");
        Assert.assertNotNull("Variable botz is null", botzVariable);
        Assert.assertEquals("Wrong variable ID", "botz", botzVariable.getId());
        Assert.assertTrue("Variable botz is not scalar", botzVariable.isScalar());

        Assert.assertNull("Variable botz have a parent", botzVariable.getParent());

        ParameterBean botzParameters = botzVariable.getParameterBean();
        Assert.assertNotNull("Variable botz have no parameter", botzParameters);
        Assert.assertEquals("Wrong botz parameter standardName", "depth", botzParameters.getStandardName());
        Assert.assertEquals("Wrong botz parameter description", "Depth of sea-bed", botzParameters.getDescription());
        Assert.assertEquals("Wrong botz parameter units", "metre", botzParameters.getUnits());
        Assert.assertEquals("Wrong botz parameter title", "depth", botzParameters.getTitle());
        Assert.assertEquals("Wrong botz parameter variableId", "botz", botzParameters.getVariableId());

        HorizontalDomainBean botzHorizontalDomain = botzVariable.getHorizontalDomainBean();
        Assert.assertNotNull("Variable botz have no Horizontal Domain", botzHorizontalDomain);
        Assert.assertEquals("Wrong botz Horizontal Domain minLon", 142.16879272460938, botzHorizontalDomain.getMinLon(), 0.0000001);
        Assert.assertEquals("Wrong botz Horizontal Domain maxLon", 156.88563537597656, botzHorizontalDomain.getMaxLon(), 0.0000001);
        Assert.assertEquals("Wrong botz Horizontal Domain minLat", -28.6960218, botzHorizontalDomain.getMinLat(), 0.0000001);
        Assert.assertEquals("Wrong botz Horizontal Domain maxLat", -7.0119082, botzHorizontalDomain.getMaxLat(), 0.0000001);

        Assert.assertNull("Variable botz have a Vertical Domain", botzVariable.getVerticalDomainBean());
    }
}
