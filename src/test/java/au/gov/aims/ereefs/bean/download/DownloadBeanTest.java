/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
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
