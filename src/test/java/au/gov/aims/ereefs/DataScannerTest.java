/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DataScannerTest {

    @Test
    public void testUnzipNetCDFFile() throws Exception {
        URL netCDFFileUrl = DataScannerTest.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFileOrig = new File(netCDFFileUrl.getFile());
        Assert.assertTrue("Original NetCDF file does not exist", netCDFFileOrig.exists());

        File tempDir = new File("/tmp/netcdf");
        tempDir.mkdirs();
        File netCDFFileCopy = new File(tempDir, netCDFFileOrig.getName());

        Files.copy(netCDFFileOrig.toPath(), netCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Assert.assertTrue("NetCDF file copy does not exist", netCDFFileCopy.exists());

        boolean valid = NetCDFUtils.scan(netCDFFileCopy);

        Assert.assertTrue("NetCDF file is invalid", valid);
    }

}
