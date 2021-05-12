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
