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
package au.gov.aims.ereefs.bean.metadata.netcdf;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ManualNetCDFMetadataBeanTest {
    private static final Logger LOGGER = Logger.getLogger(ManualNetCDFMetadataBeanTest.class);

    /**
     * Test used to manually check NetCDF file metadata.
     * It's not intended to be run as a Unit test.
     */
    @Test
    @Ignore
    public void testManuallyCheckMetadata() {
        // Good files; should return proper metadata
        //File netCDFFileOrig = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly/gbr1_simple_2014-12-01.nc");
        //File netCDFFileOrig = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly/gbr1_simple_2014-12-02.nc");
        File netCDFFileOrig = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/hourly/gbr4_simple_2014-12.nc");

        // Random = should return stacktrace
        //File netCDFFileOrig = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/random_data.nc");


        String definitionId = "downloads/ereefs";
        String datasetId = netCDFFileOrig.getName();

        long expectedLastModified = netCDFFileOrig.lastModified();
        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, netCDFFileOrig.toURI(), netCDFFileOrig, expectedLastModified, false);
        LOGGER.debug(metadata);

        // Test last modified
        long lastModified = metadata.getLastModified();
        Assert.assertEquals("Wrong last modified.", expectedLastModified, lastModified);

        Assert.assertEquals("Wrong metadata status", NetCDFMetadataBean.Status.VALID, metadata.getStatus());
    }

    /**
     * There used to be a bug with the NetCDFMetadata class.
     * Deleting the NetCDF file would not free the disk space until the Java app terminate
     * (due to a "bug" in the NetCDF library).
     * This can cause a lot of issues with large NetCDF files.
     * This test verify that the bug did not reemerge.
     *
     * <p>It works by looking at the disk space before and after
     * deleting the NetCDF file that was read by the NetCDF library
     * as it was before copying it.</p>
     *
     * <p>Unfortunately, it can not be run by Jenkins since there is too much disk activity on
     * the Jenkins server at the moment of running the test:
     *     Files get created using disk space, while this test expect the be able to reclaim the
     *     disk space used by the deleted file.</p>
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testLoadMetadataFreeDiskSpace() throws Exception {
        URL netCDFFileUrl = NetCDFMetadataBeanTest.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());

        File tempDir = new File("/tmp");
        File tempNetCDFFile = new File(tempDir, "small.nc.copy");

        FileUtils.copyFile(netCDFFileOrig, tempNetCDFFile);


        String definitionId = "downloads/gbr1_v2";
        String datasetId = "gbr1_simple_2014-12-02.nc";
        URI fileURI = new File("/tmp/netcdfFiles/gbr1_simple_2014-12-02.nc").toURI();

        // Get file metadata
        NetCDFMetadataBean metadata = NetCDFMetadataBean.create(definitionId, datasetId, fileURI, tempNetCDFFile, tempNetCDFFile.lastModified());
        LOGGER.debug(metadata);

        // Basic check to ensure the metadata is not empty
        Assert.assertNotNull("Metadata is null", metadata);

        JSONObject jsonAttributes = metadata.getAttributes();
        Assert.assertNotNull("Metadata attributes is null", jsonAttributes);
        Assert.assertEquals("Wrong number of metadata attributes", 7, jsonAttributes.length());

        Map<String, VariableMetadataBean> variables = metadata.getVariableMetadataBeanMap();
        Assert.assertNotNull("Metadata variables is null", variables);
        Assert.assertEquals("Wrong number of metadata variables", 14, variables.size());


        int MB = 1024 * 1024;
        // Some useful debugging info, outputted directly in the console
        System.out.println("BEFORE DELETING: " + tempNetCDFFile);
        System.out.println(String.format("    Total space: %d MB", tempDir.getTotalSpace() / MB));
        System.out.println(String.format("    Free space: %d MB", tempDir.getFreeSpace() / MB));
        System.out.println(String.format("    Usable space: %d MB", tempDir.getUsableSpace() / MB));

        long fileSize = tempNetCDFFile.length();
        long diskSpaceBefore = tempDir.getFreeSpace();
        Assert.assertTrue("Could not delete the file: " + tempNetCDFFile, tempNetCDFFile.delete());

        // Wait (it can take a few milliseconds for the OS to adjust its available space)
        long diskSpaceAfter = tempDir.getFreeSpace();
        long freedSpace = diskSpaceAfter - diskSpaceBefore;
        int tries = 20;
        while (freedSpace < fileSize) {
            LOGGER.info(String.format("Freed space (%d) < deleted file size (%d). Missing %d. Waiting...", freedSpace, fileSize, (fileSize - freedSpace)));
            Thread.sleep(1000);
            // Check if disk space was freed
            diskSpaceAfter = tempDir.getFreeSpace();
            freedSpace = diskSpaceAfter - diskSpaceBefore;

            tries--;
            if (tries <= 0) {
                break;
            }
        }

        // Used to compare with the debugging info outputted before deleting the file
        System.out.println("AFTER DELETING: " + tempNetCDFFile);
        System.out.println(String.format("    Total space: %d MB", tempDir.getTotalSpace() / MB));
        System.out.println(String.format("    Free space: %d MB", tempDir.getFreeSpace() / MB));
        System.out.println(String.format("    Usable space: %d MB", tempDir.getUsableSpace() / MB));

        Assert.assertTrue("Could not reclaim disk space after deleting the NetCDF file.",
                freedSpace >= fileSize);

        // NOTE: Use a delta of 10k
        Assert.assertEquals("Could not reclaim disk space after deleting the NetCDF file.",
                freedSpace, fileSize, 10 * 1024);
    }
}
