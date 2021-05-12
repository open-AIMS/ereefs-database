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
package au.gov.aims.ereefs.bean.download;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class DownloadBeanTest {

    @Test
    public void testSerialiseDeserialise() {
        DownloadBean download = new DownloadBean();
        download.setId("downloads/gbr1_2-0");
        download.setEnabled(true);
        download.setFilenameTemplate("gbr1_simple_{year}-{month}-{day}.nc");
        download.addCatalogueUrl("http://dapds00.nci.org.au/thredds/catalog/fx3/gbr1_2.0/catalog.xml", null);

        OutputBean outputBean = new OutputBean();
        outputBean.setDestination("s3://buckets/gbr1_v2");
        outputBean.setDownloadDir("/tmp/netcdf");

        download.setOutput(outputBean);

        JSONObject expectedJsonDownload = download.toJSON();

        DownloadBean deserialisedDownload = new DownloadBean(expectedJsonDownload);
        JSONObject actualJsonDownload = deserialisedDownload.toJSON();


        Assert.assertEquals(expectedJsonDownload.toString(4), actualJsonDownload.toString(4));
    }

    @Test
    public void testGetFilenameTemplateRegexStrGbr1Exposure() {
        DownloadBean download = new DownloadBean(new JSONObject("{\n" +
                "    \"id\": \"downloads/gbr1_exposure\",\n" +
                "    \"enabled\": true,\n" +
                "    \"filenameTemplate\": \"gbr1_exposure_maps_{year}-{month}.nc\"\n" +
                "}\n"));

        String expectedPatternStr =
                Pattern.quote("gbr1_exposure_maps_") +
                "[0-9]{2,4}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote(".nc");

        String regexStr = download.getFilenameTemplateRegexStr();

        Assert.assertEquals(expectedPatternStr, regexStr);
    }

    @Test
    public void testGetFilenameTemplateRegexStrGbr1v2() {
        DownloadBean download = new DownloadBean(new JSONObject("{\n" +
                "    \"id\": \"downloads/gbr1_exposure\",\n" +
                "    \"enabled\": true,\n" +
                "    \"filenameTemplate\": \"gbr1_simple_{year}-{month}-{day}.nc\"\n" +
                "}\n"));

        String expectedPatternStr =
                Pattern.quote("gbr1_simple_") +
                "[0-9]{2,4}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote(".nc");

        String regexStr = download.getFilenameTemplateRegexStr();

        Assert.assertEquals(expectedPatternStr, regexStr);
    }

    @Test
    public void testGetFilenameTemplateRegexStrUnbalanced() {
        DownloadBean download = new DownloadBean(new JSONObject("{\n" +
                "    \"id\": \"downloads/gbr1_exposure\",\n" +
                "    \"enabled\": true,\n" +
                "    \"filenameTemplate\": \"gbr1}_simple_{year}-{month}-{day}.nc{\"\n" +
                "}\n"));

        String expectedPatternStr =
                Pattern.quote("gbr1}_simple_") +
                "[0-9]{2,4}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote(".nc{");

        String regexStr = download.getFilenameTemplateRegexStr();

        Assert.assertEquals(expectedPatternStr, regexStr);
    }

    @Test
    public void testGetFilenameTemplateRegexStrInvalidKeyword() {
        DownloadBean download = new DownloadBean(new JSONObject("{\n" +
                "    \"id\": \"downloads/gbr1_exposure\",\n" +
                "    \"enabled\": true,\n" +
                "    \"filenameTemplate\": \"{gbr1}_{}simple_{year}-{month}-{day}.nc}\"\n" +
                "}\n"));

        String expectedPatternStr =
                Pattern.quote("{gbr1}_{}simple_") +
                "[0-9]{2,4}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote("-") +
                "[0-9]{2}" +
                Pattern.quote(".nc}");

        String regexStr = download.getFilenameTemplateRegexStr();

        Assert.assertEquals(expectedPatternStr, regexStr);
    }

    @Test
    public void testGetFilenameTemplateRegexStrJustPlaceholder() {
        DownloadBean download = new DownloadBean(new JSONObject("{\n" +
                "    \"id\": \"downloads/gbr1_exposure\",\n" +
                "    \"enabled\": true,\n" +
                "    \"filenameTemplate\": \"{year}{month}{day}\"\n" +
                "}\n"));

        String expectedPatternStr =
                "[0-9]{2,4}" +
                "[0-9]{2}" +
                "[0-9]{2}";

        String regexStr = download.getFilenameTemplateRegexStr();

        Assert.assertEquals(expectedPatternStr, regexStr);
    }
}
