/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManagerTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class NetCDFUtilsTest {

    @Test
    public void testScanningValidDataFile() throws Exception {
        URL netCDFFileUrl = ConfigManagerTestBase.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFile = new File(netCDFFileUrl.getFile());

        Assert.assertTrue(NetCDFUtils.scan(netCDFFile));
    }
}
