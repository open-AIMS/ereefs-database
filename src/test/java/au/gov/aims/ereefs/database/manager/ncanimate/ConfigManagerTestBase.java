/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.manager.ncanimate;

import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.database.DatabaseTestBase;
import au.gov.aims.ereefs.database.manager.DownloadManager;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ereefs.helper.TestHelper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfigManagerTestBase extends DatabaseTestBase {
    private static final Logger LOGGER = Logger.getLogger(ConfigManagerTestBase.class);

    protected long beforeSaveData;
    protected long afterSaveData;

    @After
    public void clearCache() {
        NcAnimateConfigHelper.clearMetadataCache();
    }

    public void insertDummyData() throws Exception {
        this.beforeSaveData = new Date().getTime();

        this.insertDummyNetCDFFiles();
        this.insertTestDownloads();
        this.insertTestConfigParts();
        this.insertTestConfigs();

        this.afterSaveData = new Date().getTime();
    }

    public void insertFakeHourlyData(int nbHours) throws Exception {
        this.insertFakeHourlyNetCDFFiles(nbHours);

        this.afterSaveData = new Date().getTime();
    }

    public void insertFakeMonthlyData(int nbMonths) throws Exception {
        this.insertFakeMonthlyNetCDFFiles(nbMonths);

        this.afterSaveData = new Date().getTime();
    }

    private void insertDummyNetCDFFiles() throws Exception {
        URL netCDFFileUrl = ConfigManagerTestBase.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());
        Assert.assertTrue(String.format("The NetCDF file doesn't exist: %s", netCDFFileOrig), netCDFFileOrig.exists());

        String definitionId = "downloads/gbr4_v2";
        String datasetId = "gbr4_small.nc";
        URI fileURI = new File("/tmp/netcdfFiles/gbr4_small.nc").toURI();

        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileOrig, netCDFFileOrig.lastModified());

        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);
        metadataManager.save(metadata.toJSON());
    }

    private void insertFakeHourlyNetCDFFiles(int nbHours) throws Exception {
        URL netCDFFileUrl = ConfigManagerTestBase.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());
        Assert.assertTrue(String.format("The NetCDF file doesn't exist: %s", netCDFFileOrig), netCDFFileOrig.exists());

        String definitionId = "downloads/gbr4_v2";
        String datasetId = "gbr4_small.nc";
        URI fileURI = new File("/tmp/netcdfFiles/gbr4_small.nc").toURI();

        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileOrig, netCDFFileOrig.lastModified());
        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);

        NetCDFMetadataBean.Status metadataStatus = metadata.getStatus();
        Assert.assertEquals(
                String.format("Corrupted NetCDF file: %s%n%s", netCDFFileOrig, metadata.toJSON().toString(4)),
                NetCDFMetadataBean.Status.VALID,
                metadataStatus);

        String errorMessage = metadata.getErrorMessage();
        Assert.assertNull(
                String.format("Error occurred while extracting NetCDF file metadata: %s%n%s", netCDFFileOrig, metadata.toJSON().toString(4)),
                errorMessage);

        // Create a bunch of fake files based on the real one
        for (int i=0; i<nbHours; i++) {
            JSONObject jsonFakeMetadata = metadata.toJSON();

            DateTime startDate = new DateTime(2010, 9, 1, 5, 0).plusHours(i);
            DateTime endDate = new DateTime(2010, 9, 1, 5, 0).plusHours(i);

            List<String> fakeTimeValues = new ArrayList<String>();
            fakeTimeValues.add(startDate.toString());
            fakeTimeValues.add(endDate.toString());

            // Alter properties
            String fakeDatasetId = String.format("FAKE_small_%d_%d-%d-%d_%dh%d.nc", i, startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), startDate.getHourOfDay(), startDate.getMinuteOfHour());
            jsonFakeMetadata.put("id", NetCDFMetadataBean.getUniqueDatasetId(definitionId, fakeDatasetId));
            jsonFakeMetadata.put("datasetId", datasetId);

            // Change dates on variables
            JSONObject jsonFakeVariables = jsonFakeMetadata.optJSONObject("variables");
            if (jsonFakeVariables != null) {
                for (String fakeVariableId : jsonFakeVariables.keySet()) {
                    JSONObject jsonFakeVariable = jsonFakeVariables.optJSONObject(fakeVariableId);
                    JSONObject jsonFakeTemporalDomain = jsonFakeVariable.optJSONObject("temporalDomain");
                    if (jsonFakeTemporalDomain != null) {
                        jsonFakeTemporalDomain.put("minDate", startDate.toString());
                        jsonFakeTemporalDomain.put("maxDate", endDate.toString());
                        jsonFakeTemporalDomain.put("timeValues", fakeTimeValues);
                    }
                }
            }

            metadataManager.save(jsonFakeMetadata);
        }
    }

    private void insertFakeMonthlyNetCDFFiles(int nbMonths) throws Exception {
        URL netCDFFileUrl = ConfigManagerTestBase.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());

        String definitionId = "downloads/gbr4_v2";
        String datasetId = "gbr4_small.nc";
        URI fileURI = new File("/tmp/netcdfFiles/gbr4_small.nc").toURI();

        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFileOrig, netCDFFileOrig.lastModified());
        MetadataManager metadataManager = new MetadataManager(this.getDatabaseClient(), CACHE_STRATEGY);

        // Create a bunch of fake files based on the real one
        for (int i=0; i<nbMonths; i++) {
            JSONObject jsonFakeMetadata = metadata.toJSON();

            DateTime startDate = new DateTime(2010, 9, 1, 0, 0).plusMonths(i);
            DateTime endDate = new DateTime(2010, 9, 1, 0, 0).plusMonths(i+1).minusHours(1);

            List<String> fakeTimeValues = new ArrayList<String>();
            DateTime fakeDate = startDate;
            while (fakeDate.compareTo(endDate) < 0) {
                fakeTimeValues.add(fakeDate.toString());
                fakeDate = fakeDate.plusHours(1);
            }

            // Alter properties
            String fakeDatasetId = String.format("FAKE_small_%d_%d-%d-%d.nc", i, startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth());
            jsonFakeMetadata.put("id", NetCDFMetadataBean.getUniqueDatasetId(definitionId, fakeDatasetId));
            jsonFakeMetadata.put("datasetId", datasetId);

            // Change dates on variables
            JSONObject jsonFakeVariables = jsonFakeMetadata.optJSONObject("variables");
            for (String fakeVariableId : jsonFakeVariables.keySet()) {
                JSONObject jsonFakeVariable = jsonFakeVariables.optJSONObject(fakeVariableId);
                JSONObject jsonFakeTemporalDomain = jsonFakeVariable.optJSONObject("temporalDomain");
                if (jsonFakeTemporalDomain != null) {
                    jsonFakeTemporalDomain.put("minDate", startDate.toString());
                    jsonFakeTemporalDomain.put("maxDate", endDate.toString());
                    jsonFakeTemporalDomain.put("timeValues", fakeTimeValues);
                }
            }

            metadataManager.save(jsonFakeMetadata);
        }
    }



    private void insertTestDownloads() throws Exception {
        DownloadManager downloadManager = new DownloadManager(this.getDatabaseClient(), CACHE_STRATEGY);

        TestHelper.insertTestConfigs(downloadManager, "download", "download");
    }

    private void insertTestConfigParts() throws Exception {
        ConfigPartManager configPartManager = new ConfigPartManager(this.getDatabaseClient(), CACHE_STRATEGY);

        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/canvas",    "canvas");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/input",     "input");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/layers",    "layer");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/legends",   "legend");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/panels",    "panel");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/regions",   "region");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/render",    "render");
        TestHelper.insertTestConfigs(configPartManager, "ncanimate/configParts/variables", "variable");
    }

    private void insertTestConfigs() throws Exception {
        ConfigManager configManager = new ConfigManager(this.getDatabaseClient(), CACHE_STRATEGY);

        TestHelper.insertTestConfigs(configManager, "ncanimate", "NcAnimate configuration");
    }
}
