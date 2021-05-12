/*
 *  Copyright (C) 2020 Australian Institute of Marine Science
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
