/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.download;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is a simple bean, used with the {@code ereefs-download-manager} project.
 * It represent the documents found in the MongoDB collection {@code download}.
 * It's describing a Download definition for a THREDDS catalogue.
 */
public class DownloadBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(DownloadBean.class);

    private String id;
    private boolean enabled;

    private String filenameTemplate;
    private String filenameRegexStr;

    // List of catalogue URLs.
    // NOTE: Download definitions usually contain only one catalogue URL.
    private List<CatalogueUrlBean> catalogueUrls;
    private OutputBean output;

    /**
     * Empty constructor. Use setters to set attributes.
     */
    public DownloadBean() {}

    /**
     * Create a DownloadBean from a JSON definition of a THREDDS server.
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *     "id": "downloads_gbr4_v2",
     *     "enabled": true,
     *     "filenameTemplate": "gbr4_simple_{year}-{month}.nc",
     *     "catalogueUrl": "http://dapds00.nci.org.au/thredds/catalog/fx3/gbr4_v2/catalog.xml",
     *     "output": {
     *         "destination": "s3://ereefs-nci-mirror/gbr4_v2",
     *         "downloadDir": "/tmp/netcdf/gbr4_v2"
     *     }
     * }</pre>
     *
     * @param jsonDownload {@code JSONObject} representing a {@code DownloadBean} definition.
     */
    public DownloadBean(JSONObject jsonDownload) {
        if (jsonDownload != null) {
            this.id = jsonDownload.optString("_id", null);
            this.enabled = jsonDownload.optBoolean("enabled", false);

            this.filenameTemplate = jsonDownload.optString("filenameTemplate", null);
            this.filenameRegexStr = jsonDownload.optString("filenameRegex", null);

            this.catalogueUrls = null;
            this.addCatalogueUrl(jsonDownload.optString("catalogueUrl", null), null);
            this.addCatalogueUrls(jsonDownload.optJSONArray("catalogueUrls"));
            this.setOutput(jsonDownload.optJSONObject("output"));
        }
    }

    /**
     * Returns the ID of the DownloadBean definition.
     * @return The DownloadBean ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the DownloadBean definition ID
     * @param id A unique identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the enable value.
     * @return {@code true} if the DownloadBean definition is unable; {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     * @param enabled Set to {@code true} to enable the DownloadBean definition.
     * Set to {@code false} to disable this DownloadBean definition.
     * The DownloadManager ignore DownloadBean definition with {@code enabled} set to {@code false}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the filename template if set.
     * See {@link #setFilenameTemplate(String)} for more details.
     * See {@link #setFilenameRegexStr(String)} for other way of filtering files.
     * @return the filename template filter if set; {@code null} otherwise.
     */
    public String getFilenameTemplate() {
        return this.filenameTemplate;
    }

    /**
     * Filter files to download from the THREDDS server.
     *
     * <p>Accepted patterns are:</p>
     * <ul>
     *   <li><em>{year}</em>: Look for 2 to 4 digit numbers</li>
     *   <li><em>{month}</em>: Look for 2 digit numbers</li>
     *   <li><em>{day}</em>: Look for 2 digit numbers</li>
     * </ul>
     *
     * <p>For example, if the THREDDS server contains the following files:</p>
     * <pre class="code">
     * ├── file_2020-01.nc
     * ├── file_2020-02.nc
     * ├── ...
     * ├── file_2020-12.nc
     * └── statistic_2020.nc
     * </pre>
     *
     * <p>and the following template filter is used:</p>
     * <pre class="code">file_{year}-{month}.nc</pre>
     *
     * <p>the DownloadManager will only download the following files:</p>
     * <pre class="code">
     * ├── file_2020-01.nc
     * ├── file_2020-02.nc
     * ├── ...
     * └── file_2020-12.nc
     * </pre>
     *
     * <p>See {@link #setFilenameRegexStr(String)} for other way of filtering files.</p>
     *
     * @param filenameTemplate the filename template filter.
     */
    public void setFilenameTemplate(String filenameTemplate) {
        this.filenameTemplate = filenameTemplate;
    }

    /**
     * Get the filename regular expression if set.
     * See {@link #setFilenameRegexStr(String)} for more details.
     * See {@link #setFilenameTemplate(String)} for other way of filtering files.
     * @return the filename regular expression if set; {@code null} otherwise.
     */
    public String getFilenameRegexStr() {
        return this.filenameRegexStr;
    }

    /**
     * Filter files to download from the THREDDS server using regular expression.
     *
     * <p>For more information about regular expression, see:
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html" target="_blank">https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html</a></p>
     *
     * <p>For example, if the THREDDS server contains the following files:</p>
     * <pre class="code">
     * ├── file_2020-01.nc
     * ├── file_2020-02.nc
     * ├── ...
     * ├── file_2020-12.nc
     * └── statistic_2020.nc
     * </pre>
     *
     * <p>and the following regular expression filter is used:</p>
     * <pre class="code">file_[0-9]{4}-[0-9]{2}\.nc</pre>
     *
     * <p>the DownloadManager will only download the following files:</p>
     * <pre class="code">
     * ├── file_2020-01.nc
     * ├── file_2020-02.nc
     * ├── ...
     * └── file_2020-12.nc
     * </pre>
     *
     * @param filenameRegexStr the filename regular expression filter.
     */
    public void setFilenameRegexStr(String filenameRegexStr) {
        this.filenameRegexStr = filenameRegexStr;
    }

    /**
     * Get the filename template regular expression if the filenameTemplate property is set.
     * See {@link #setFilenameTemplate(String)} for more details.
     *
     * <p>This is an internal method use to visualise how the filenameTemplate
     * get converted to regular expression. It can be used to diagnose
     * erroneous filenameTemplate.</p>
     *
     * @return the filename template regular expression filter if
     * filenameTemplate is set; {@code null} otherwise.
     */
    public String getFilenameTemplateRegexStr() {
        if (this.filenameTemplate == null || this.filenameTemplate.isEmpty()) {
            return null;
        }

        // Find the index of the first placeholder (example: {year})
        int openIdx = this.filenameTemplate.indexOf('{');
        if (openIdx < 0) {
            // There is no placeholder...
            return Pattern.quote(this.filenameTemplate);
        }

        StringBuilder regexSb = new StringBuilder();
        int closeIdx = this.filenameTemplate.indexOf('}', openIdx + 1);
        if (closeIdx < 0) {
            // There is no placeholder...
            return Pattern.quote(this.filenameTemplate);
        }
        int lastCloseIdx = -1;

        // Loop through the string and find all the placeholders
        while(openIdx >= 0 && closeIdx >= 0) {
            // Add the text between placeholders. Example: {year}-{month} <== Add the "-"
            // Or the text before the first placeholder. Example: gbr1_{year} <== Add "gbr1_"
            if (openIdx > lastCloseIdx + 1) {
                regexSb.append(Pattern.quote(this.filenameTemplate.substring(lastCloseIdx + 1, openIdx)));
            }

            // Isolate the keyword between the curly brackets of the placeholder.
            // Example: {year} ==> keyword = year
            String keyword = this.filenameTemplate.substring(openIdx + 1, closeIdx);
            if (keyword.length() > 0) {
                // Translate keyword into regex
                if ("year".equals(keyword.toLowerCase())) {
                    regexSb.append("[0-9]{2,4}");
                } else if ("month".equals(keyword.toLowerCase())) {
                    regexSb.append("[0-9]{2}");
                } else if ("day".equals(keyword.toLowerCase())) {
                    regexSb.append("[0-9]{2}");
                } else {
                    // Invalid keyword; add it as is
                    regexSb.append(Pattern.quote("{" + keyword + "}"));
                }
            } else {
                // There is no keyword, just empty brackets.
                // Add them back to the regex
                regexSb.append(Pattern.quote("{}"));
            }

            // Find the index of the next placeholder, starting from the end of the current placeholder
            openIdx = this.filenameTemplate.indexOf('{', closeIdx + 1);

            // Find the index of the first closing bracket following the open bracket found above
            lastCloseIdx = closeIdx;
            if (openIdx >= 0) {
                closeIdx = this.filenameTemplate.indexOf('}', openIdx + 1);
            } else {
                closeIdx = -1;
            }
        }

        if (lastCloseIdx + 1 < this.filenameTemplate.length()) {
            regexSb.append(Pattern.quote(this.filenameTemplate.substring(lastCloseIdx + 1)));
        }

        String regexStr = regexSb.toString();

        // Cleanup the regex (join quoted text together)
        return regexStr.replace("\\E\\Q", "");
    }

    /**
     * @deprecated Use {@link #getFilenameRegex()}
     * @return the file filter regular expression if
     *     a filter property is set; {@code null} otherwise.
     */
    @Deprecated
    public Pattern getFilenameTemplateRegex() {
        return this.getFilenameRegex();
    }

    /**
     * Get the file filter regular expression, if one of the filter property is set.
     * <ul>
     *   <li>filenameTemplate</li>
     *   <li>filenameRegexStr</li>
     * </ul>
     *
     * <p>This is an internal method use by the DownloadManager to
     * filter the files found on the THREDDS server.</p>
     *
     * @return the file filter regular expression if
     * a filter property is set; {@code null} otherwise.
     */
    public Pattern getFilenameRegex() {
        String regexStr = this.getFilenameRegexStr();
        if (regexStr != null) {
            return Pattern.compile(regexStr);
        }

        String templateRegexStr = this.getFilenameTemplateRegexStr();
        if (templateRegexStr != null) {
            return Pattern.compile(templateRegexStr);
        }

        return null;
    }

    /**
     * Get the list of {@link CatalogueUrlBean}.
     * It's the list of server URLs the DownloadManager
     * needs to crawl to get all the wanted files from
     * the THREDDS server.
     * The list usually contains a single URL. Multiple
     * URLs can be specified when the files are spread
     * across multiple sub-folders on the THREDDS server.
     *
     * @return the list of {@link CatalogueUrlBean}.
     */
    public List<CatalogueUrlBean> getCatalogueUrls() {
        return this.catalogueUrls;
    }

    /**
     * Add a {@link CatalogueUrlBean} to the list.
     * @param catalogueUrl the {@link CatalogueUrlBean} to add to the list.
     */
    public void addCatalogueUrl(CatalogueUrlBean catalogueUrl) {
        if (this.catalogueUrls == null) {
            this.catalogueUrls = new ArrayList<CatalogueUrlBean>();
        }
        this.catalogueUrls.add(catalogueUrl);
    }

    /**
     * Add a {@link CatalogueUrlBean} to the list.
     * @param catalogueUrlStr the THREDDS server URL to add to the list.
     * @param subDir the sub-folder in which the files needs to be downloaded.
     * See {@link CatalogueUrlBean#setSubDirectory(String)} for more information.
     */
    public void addCatalogueUrl(String catalogueUrlStr, String subDir) {
        if ((catalogueUrlStr != null && !catalogueUrlStr.isEmpty()) ||
                (subDir != null && !subDir.isEmpty())) {
            CatalogueUrlBean catalogueUrl = new CatalogueUrlBean();
            catalogueUrl.setCatalogueUrl(catalogueUrlStr);
            catalogueUrl.setSubDirectory(subDir);
            this.addCatalogueUrl(catalogueUrl);
        }
    }

    /**
     * Add a list of catalogue URL, specified as a {@code JSONArray}.
     * Each element of the {@code JSONArray} is a {@code JSONObject}
     * containing the properties {@code catalogueUrl} and {@code subDirectory}.
     *
     * <p>Example:</p>
     * <pre class="code">
     * [
     *   {
     *     "catalogueUrl": "https://thredds.server.com/thredds/2020/catalog.xml",
     *     "subDirectory": "year-2020"
     *   }, {
     *     "catalogueUrl": "https://thredds.server.com/thredds/2021/catalog.xml",
     *     "subDirectory": "year-2021"
     *   }
     * ]</pre>
     *
     * @param catalogueUrlsArray list of catalogue URL, specified as a {@code JSONArray}.
     */
    public void addCatalogueUrls(JSONArray catalogueUrlsArray) {
        if (catalogueUrlsArray != null && !catalogueUrlsArray.isEmpty()) {
            for (int i=0; i<catalogueUrlsArray.length(); i++) {
                JSONObject catalogueUrlObj = catalogueUrlsArray.optJSONObject(i);
                if (catalogueUrlObj != null) {
                    String catalogueUrl = catalogueUrlObj.optString("catalogueUrl", null);
                    String subDirectory = catalogueUrlObj.optString("subDirectory", null);
                    this.addCatalogueUrl(catalogueUrl, subDirectory);
                }
            }
        }
    }

    /**
     * Return the {@link OutputBean} which describe where the files are downloaded.
     * @return the {@link OutputBean}.
     */
    public OutputBean getOutput() {
        return this.output;
    }

    /**
     * Set the {@link OutputBean} which describe where the files are downloaded.
     * @param output the {@link OutputBean}.
     */
    public void setOutput(OutputBean output) {
        this.output = output;
    }

    /**
     * Set the {@link OutputBean} using a {@code JSONObject}.
     * @param jsonOutput a {@code JSONObject} representing an {@link OutputBean}.
     */
    public void setOutput(JSONObject jsonOutput) {
        this.output = jsonOutput == null ? null : new OutputBean(jsonOutput);
    }


    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("_id", this.id);
        json.put("enabled", this.enabled);
        json.put("filenameTemplate", this.filenameTemplate);
        json.put("filenameRegex", this.filenameRegexStr);

        if (this.catalogueUrls != null && !this.catalogueUrls.isEmpty()) {
            JSONArray catalogueUrlArray = new JSONArray();
            for (CatalogueUrlBean catalogueUrl : this.catalogueUrls) {
                catalogueUrlArray.put(catalogueUrl.toJSON());
            }
            json.put("catalogueUrls", catalogueUrlArray);
        }

        if (this.output != null) {
            json.put("output", this.output.toJSON());
        }

        return json;
    }
}
