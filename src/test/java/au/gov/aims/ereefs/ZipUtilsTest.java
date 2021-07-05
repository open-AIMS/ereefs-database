/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs;

import au.gov.aims.ereefs.bean.NetCDFUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ZipUtilsTest {
    private static final Logger LOGGER = Logger.getLogger(ZipUtilsTest.class);

    @Test
    public void testUnzipNetCDFFile() throws Exception {
        LOGGER.info("Starting test: testUnzipNetCDFFile");

        URL zippedNetCDFFileUrl = ZipUtilsTest.class.getClassLoader().getResource("netcdf/IMOS_OceanCurrent_HV_20110901T000000Z_GSLA_FV02_NRT00_C-20111104T011656Z.nc.gz");
        File zippedNetCDFFileOrig = new File(zippedNetCDFFileUrl.getFile());
        LOGGER.info(String.format("Resource file: %s%n%s", zippedNetCDFFileOrig, zippedNetCDFFileOrig.canRead() ? "Exists" : "Doesn't exist or not readable"));

        File tempDir = new File("/tmp/netcdf");
        tempDir.mkdirs();
        File zippedNetCDFFileCopy = new File(tempDir, zippedNetCDFFileOrig.getName());

        Files.copy(zippedNetCDFFileOrig.toPath(), zippedNetCDFFileCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info(String.format("Temporary file copy: %s%n%s", zippedNetCDFFileCopy, zippedNetCDFFileCopy.canRead() ? "Exists" : "Doesn't exist or not readable"));

        File unzippedNetCDFFile = ZipUtils.unzipFile(zippedNetCDFFileCopy);
        LOGGER.info(String.format("Unzipped file: %s%n%s", unzippedNetCDFFile, unzippedNetCDFFile.canRead() ? "Exists" : "Doesn't exist or not readable"));

        boolean valid = NetCDFUtils.scan(unzippedNetCDFFile);

        Assert.assertTrue("Unzipped NetCDF file is invalid", valid);
    }

}
