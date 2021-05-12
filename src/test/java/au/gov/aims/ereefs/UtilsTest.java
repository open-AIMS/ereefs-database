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
package au.gov.aims.ereefs;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class UtilsTest {

    @Test
    public void testDeleteDirectory() throws IOException {
        File tempDir = new File("/tmp/databaseTests/deleteDirectory");

        // Clean-up before the test
        Utils.deleteDirectory(tempDir);
        Assert.assertFalse(String.format("The directory %s could not be deleted before starting the test", tempDir), tempDir.exists());
        Assert.assertTrue(String.format("The directory %s could not be created before starting the test", tempDir), tempDir.mkdirs());

        File originalFile = new File(tempDir, "textFile.txt");
        File symbolicLink = new File(tempDir, "link.txt");
        File brokenSymbolicLink = new File(tempDir, "brokenLink.txt");

        File subDir = new File(tempDir, "subDir");
        File subOriginalFile = new File(subDir, "subTextFile.txt");
        File subSymbolicLink = new File(subDir, "subLink.txt");
        File subBrokenSymbolicLink = new File(subDir, "subBrokenLink.txt");

        File dirSymbolicLink = new File(tempDir, "linkDir");
        File brokenDirSymbolicLink = new File(tempDir, "brokenSubDir");

        // Create original files
        FileUtils.writeStringToFile(originalFile, "Hello world!", StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(subOriginalFile, "Hello sub world!", StandardCharsets.UTF_8);

        // Create symbolic links
        Files.createSymbolicLink(symbolicLink.toPath(), originalFile.toPath());
        Files.createSymbolicLink(brokenSymbolicLink.toPath(), new File(tempDir, "missing.txt").toPath());

        Files.createSymbolicLink(subSymbolicLink.toPath(), subOriginalFile.toPath());
        Files.createSymbolicLink(subBrokenSymbolicLink.toPath(), new File(subDir, "missing.txt").toPath());

        Files.createSymbolicLink(dirSymbolicLink.toPath(), subDir.toPath());
        Files.createSymbolicLink(brokenDirSymbolicLink.toPath(), new File(tempDir, "missingDir").toPath());

        // Delete directory
        Assert.assertTrue("deleteDirectory returned false", Utils.deleteDirectory(tempDir));
        Assert.assertFalse(String.format("The directory %s still exists", tempDir), tempDir.exists());
    }

    @Test
    public void testRealSafeFilename() {
        String input = "downloads/gbr1_2.0/file.nc";
        String expectedOutput = "downloads_gbr1_2.0_file.nc";

        Assert.assertEquals(expectedOutput, Utils.safeFilename(input));
    }

    @Test
    public void testComplexSafeFilename() {
        String input = "a%@#$%^&b$c^d&e\\f*g(h)i[j]k{l}m<,>n?o!p`q'r\"sét|u+v#w@x~y=z/file.nc";
        String expectedOutput = "a_b_c_d_e_f_g_h_i_j_k_l_m_n_o_p_q_r_s_t_u_v_w_x_y_z_file.nc";

        Assert.assertEquals(expectedOutput, Utils.safeFilename(input));
    }

    @Test
    public void testGetFilename() throws URISyntaxException {
        Assert.assertNull(
            "Method getFilename doesn't handle null properly",
            Utils.getFilename(null)
        );

        Assert.assertEquals(
            "Wrong filename extracted from S3 URI",
            "layerFile.geojson",
            Utils.getFilename(new URI("s3://bucket/directory/layerFile.geojson"))
        );

        Assert.assertEquals(
            "Wrong filename extracted from File URI",
            "layerStyle.sld",
            Utils.getFilename(new URI("file:///tmp/directory/layerStyle.sld"))
        );

        Assert.assertEquals(
            "Wrong filename extracted from File",
            "palette.pal",
            Utils.getFilename(new URI("/palettes/palette.pal"))
        );
    }

    @Test
    public void testMd5sum() throws IOException, NoSuchAlgorithmException {
        // Check with a simple text file
        URL netCDFFileJsonUrl = UtilsTest.class.getClassLoader().getResource("netcdf/small.nc.json");
        File netCDFFileJson = new File(netCDFFileJsonUrl.getFile());

        // Calculated using native md5sum command line
        String expectedNetCDFFileJsonMd5sum = "e750e2a986d86fc7343284050f1d4137";
        String actualNetCDFFileJsonMd5sum = Utils.md5sum(netCDFFileJson);

        Assert.assertEquals(String.format("Wrong MD5SUM for file: %s", netCDFFileJson), expectedNetCDFFileJsonMd5sum, actualNetCDFFileJsonMd5sum);


        // Check another file (binary), just to be sure
        URL netCDFFileUrl = UtilsTest.class.getClassLoader().getResource("netcdf/small.nc");
        File netCDFFile = new File(netCDFFileUrl.getFile());

        // Calculated using native md5sum command line
        String expectedNetCDFFileMd5sum = "1a3dfb44f1ae56acf52ac39e6cb9a93e";
        String actualNetCDFFileMd5sum = Utils.md5sum(netCDFFile);

        Assert.assertEquals(String.format("Wrong MD5SUM for file: %s", netCDFFile), expectedNetCDFFileMd5sum, actualNetCDFFileMd5sum);
    }

    @Test
    public void testGetJarDirectory() {
        File jarDirectory = Utils.getJarDirectory();

        Assert.assertNotNull("jarDirectory is null", jarDirectory);

        // NOTE: The directory "ereefs-database" is named "ereefs-database_[BRANCH_NAME]" when running by Jenkins
        //     example: "ereefs-database_master"
        Assert.assertTrue(String.format("jarDirectory is wrong. Expected to end with: %s. Actual: %s", "/ereefs-database[^/]*", jarDirectory.getAbsolutePath()),
                Pattern.compile(".*/ereefs-database[^/]*$").matcher(jarDirectory.getAbsolutePath()).matches());
    }
}
