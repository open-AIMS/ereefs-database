/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class NetCDFUtilsTestManual {

    /**
     * A corrupted file was detected by NcAggregate.
     * It contains unreadable data.
     * We updated the system to provide better NetCDF data corruption detection.
     * @throws Exception
     */
    @Test(expected = IllegalStateException.class)
    @Ignore
    public void testDetectingCorruptedFile() throws Exception {
        // 30 secs => Corrupted
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly/gbr1_simple_2016-08-14_BAD.nc");
        Assert.assertFalse(NetCDFUtils.scan(netCDFFile));
    }

    /**
     * Test a corrupted NetCDF BGC file.
     * 2020-02-17: A corrupted file was detected by the DownloadManager.
     *     We downloaded the file locally, verify that its MD5 match
     *     with the MD5 reported by the DownloadManager and proceed to the scan.
     *     The file is indeed corrupted.
     *     Last modified: 2019-10-08T04:36:15Z
     * @throws Exception
     */
    @Test(expected = IOException.class)
    @Ignore
    public void testDetectingBGCCorruptedFile() throws Exception {
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/bgc/GBR4_H2p0_B3p1_Cq3b_Dhnd/daily/baseline_gbr4_bgc_all_simple_2014-01_BAD.nc");
        Assert.assertFalse(NetCDFUtils.scan(netCDFFile));
    }

    @Test
    @Ignore
    public void testScanningValidGBR1File() throws Exception {
        // 2 mins => Good
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr1/hydro/hourly/gbr1_simple_2014-12-03.nc");
        Assert.assertTrue(NetCDFUtils.scan(netCDFFile));
    }

    @Test
    @Ignore
    public void testScanningValidGBR4File() throws Exception {
        // 7 mins => Good
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/ereefs/gbr4_v2/hydro/hourly/gbr4_simple_2014-12.nc");
        Assert.assertTrue(NetCDFUtils.scan(netCDFFile));
    }

    /**
     * The data scanner do NOT currently support GRIB files.
     * Run this test after adding support.
     * @throws Exception
     */
    @Test
    @Ignore
    public void testScanningValidNoaaFile() throws Exception {
        // 1 sec => Can't validate GRIB file yet...
        File netCDFFile = new File("/home/glafond/Desktop/TMP_INPUT/netcdf/noaa/multi_1.glo_30m.dp.201811.grb2");
        Assert.assertTrue(NetCDFUtils.scan(netCDFFile));
    }
}
