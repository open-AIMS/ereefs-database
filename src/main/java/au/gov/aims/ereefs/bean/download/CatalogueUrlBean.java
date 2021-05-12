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
package au.gov.aims.ereefs.bean.download;

import au.gov.aims.ereefs.bean.AbstractBean;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.net.URL;

/**
 * Bean representing a THREDDS XML catalogue file URL.
 * It's part of the {@link DownloadBean} which is used with the {@code ereefs-download-manager} project.
 */
public class CatalogueUrlBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(CatalogueUrlBean.class);

    // URL of the THREDDS XML catalogue file
    private URL catalogueUrl;

    // Optional sub-directory is which NetCDF files are downloaded.
    // Used when the files are spread across multiple catalogues, and the filenames are not unique.
    private String subDirectory;

    /**
     * Empty constructor. Use setters to set attributes.
     */
    public CatalogueUrlBean() {}

    /**
     * Create a THREDDS catalogue bean from a JSON object.
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *   "catalogueUrl": "https://thredds.aodn.org.au/thredds/catalog/IMOS/SRS/SST/ghrsst/L3S-6d/ngt/2011/catalog.xml",
     *   "subDirectory": "2011"
     * }</pre>
     *
     * @param jsonCatalogue {@code JSONObject} representing a THREDDS catalogue.
     */
    public CatalogueUrlBean(JSONObject jsonCatalogue) {
        if (jsonCatalogue != null) {
            this.setCatalogueUrl(jsonCatalogue.optString("catalogueUrl", null));
            this.subDirectory = jsonCatalogue.optString("subDirectory", null);
        }
    }

    /**
     * Set the catalogue URL.
     *
     * <p>Example:</p>
     * <pre class="code">https://dapds00.nci.org.au/thredds/catalog/fx3/gbr4_v2/catalog.xml</pre>
     * @param catalogueUrlStr String representing the THREDDS catalogue URL
     */
    public void setCatalogueUrl(String catalogueUrlStr) {
        this.catalogueUrl = null;
        if (catalogueUrlStr != null && !catalogueUrlStr.isEmpty()) {
            try {
                this.setCatalogueUrl(new URL(catalogueUrlStr));
            } catch (Exception ex) {
                LOGGER.warn("Invalid catalogue URL: " + catalogueUrlStr);
            }
        }
    }

    /**
     * Set the THREDDS catalogue URL.
     *
     * <p>Example:</p>
     * <pre class="code">https://dapds00.nci.org.au/thredds/catalog/fx3/gbr4_v2/catalog.xml</pre>
     * @param catalogueUrl URL of the THREDDS catalogue
     */
    public void setCatalogueUrl(URL catalogueUrl) {
        this.catalogueUrl = catalogueUrl;
    }

    /**
     * Returns the THREDDS catalogue URL.
     * @return The THREDDS catalogue URL.
     */
    public URL getCatalogueUrl() {
        return this.catalogueUrl;
    }

    /**
     * Set the sub directory.
     *
     * <p>This attribute is optional. It can usually be ignored.</p>
     *
     * <p>This attribute is use to work around issues with some THREDDS catalogues.
     * Sometimes, the files are divided into sub folders in the THREDDS server,
     * each of them having different set of files.
     * The files could be downloaded into a single folder, but this would cause
     * issue if filenames are re-used.</p>
     *
     * <p>Example:</p>
     * <pre class="code">
     * https://thredds.server.com/thredds
     * ├── 2020
     * │   ├── file_01.nc
     * │   ├── file_02.nc
     * │   ├── file_03.nc
     * │   ├── ...
     * │   └── file_12.nc
     * └── 2021
     *     ├── file_01.nc
     *     ├── file_02.nc
     *     ├── file_03.nc
     *     ├── ...
     *     └── file_12.nc
     * </pre>
     *
     * <p>The {@code subDirectory} attribute is used to download the files of
     * each catalogue in a different folder. The value of {@code subDirectory}
     * do not have to match the folder name on the THREDDS server.</p>
     *
     * <p>Example:</p>
     * <pre class="code">
     * {
     *   "_id": "example",
     *   "enabled": true,
     *   "catalogueUrls": [
     *     {
     *       "catalogueUrl": "https://thredds.server.com/thredds/2020/catalog.xml",
     *       "subDirectory": "year-2020"
     *     }, {
     *       "catalogueUrl": "https://thredds.server.com/thredds/2021/catalog.xml",
     *       "subDirectory": "year-2021"
     *     }
     *   ],
     *   "output": {
     *     "destination": "s3://my-s3-bucket/thredds-server",
     *     "downloadDir": "/tmp/netcdf"
     *   }
     * }</pre>
     *
     * <p>This will tell the DownloadManager to download the files from the THREDDS
     * server in the following folder structure on AWS S3:</p>
     * <pre class="code">
     * s3://my-s3-bucket/thredds-server/
     * ├── year-2020
     * │   ├── file_01.nc
     * │   ├── file_02.nc
     * │   ├── file_03.nc
     * │   ├── ...
     * │   └── file_12.nc
     * └── year-2021
     *     ├── file_01.nc
     *     ├── file_02.nc
     *     ├── file_03.nc
     *     ├── ...
     *     └── file_12.nc
     * </pre>
     *
     * @param subDirectory The sub directory in which the files are downloaded.
     */
    public void setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory;
    }

    /**
     * Set the sub directory.
     *
     * <p>See {@link #setSubDirectory(String)}</p>
     *
     * @return The sub directory.
     */
    public String getSubDirectory() {
        return this.subDirectory;
    }


    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("catalogueUrl", this.catalogueUrl);
        json.put("subDirectory", this.subDirectory);

        return json;
    }
}
