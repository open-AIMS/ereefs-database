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
package au.gov.aims.ereefs.bean.metadata.netcdf;

import au.gov.aims.ereefs.Utils;
import au.gov.aims.ereefs.bean.AbstractBean;
import au.gov.aims.ereefs.bean.NetCDFUtils;
import au.gov.aims.ereefs.bean.ncanimate.NcAnimateIdBean;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.EnumTypedef;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import uk.ac.rdg.resc.edal.dataset.GriddedDataset;
import uk.ac.rdg.resc.edal.dataset.cdm.NetcdfDatasetAggregator;
import uk.ac.rdg.resc.edal.metadata.VariableMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This is a bean, used with the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
 * and other eReefs projects.
 * It represent the documents found in the MongoDB collection {@code metadata}
 * with {@code type=NETCDF}.
 * It's contains the metadata of a downloaded NetCDF or GRIB files as returned
 * by the UCAR library.
 */
public class NetCDFMetadataBean extends AbstractBean {
    private static final Logger LOGGER = Logger.getLogger(NetCDFMetadataBean.class);

    /**
     * Version of the JSON Metadata file.
     *   If the file format is altered, increase the version number.
     *   This will force the re-generation of the cached metadata files.
     */
    private static final String VERSION = "2.0";
    private static final String CHECKSUM_ALGORITHM = "MD5";

    private String id;
    private String definitionId;
    private String datasetId;
    private MetadataManager.MetadataType type = MetadataManager.MetadataType.NETCDF;
    private URI fileURI; // Where is the master copy of the file (S3 or file system)
    private String checksum;
    // Most accurate last modified date of the actual NetCDF data file.
    // If the file is downloaded from THREDDS, it's the last modified date
    // as found in the THREDDS catalog.xml file:
    //     thredds.client.catalog.Dataset.getLastModifiedDate()
    private long lastModified;
    // Date the NetCDF file was downloaded from the source and uploaded into the system
    private long lastDownloaded;

    // Key: variable ID (as defined in the NetCDF file)
    private Map<String, VariableMetadataBean> variableMetadataBeanMap;

    // Global attributes tree
    private JSONObject attributes;

    // Present if the NetCDF file is corrupted
    private String errorMessage;
    private List<String> stacktrace;
    private Status status;

    private NetCDFMetadataBean(String definitionId, String datasetId) {
        this.definitionId = definitionId;
        this.datasetId = datasetId;

        this.lastModified = 0;
        this.lastDownloaded = 0;
    }

    /**
     * Construct a {@code NetCDFMetadataBean} from a {@code JSONObject} object.
     * Used when parsing the metadata JSON document retrieved from the database.
     * To construct a {@code NetCDFMetadataBean} from a NetCDF file or a GRIB file,
     * use {@link #create(String, String, URI, File, long)}.
     *
     * @param json JSON serialised NetCDFMetadataBean.
     */
    public NetCDFMetadataBean(JSONObject json) {
        String version = json.optString("version", null);
        if (!VERSION.equals(version)) {
            throw new IllegalArgumentException("Unsupported metadata version. Expected " + VERSION + ", found " + version);
        }

        this.id = json.optString("_id", null);
        this.definitionId = json.optString("definitionId", null);
        this.datasetId = json.optString("datasetId", null);
        this.setFileURIStr(json.optString("fileURI", null));

        this.checksum = json.optString("checksum", null);

        this.lastModified = 0;
        if (json.has("lastModified")) {
            String lastModifiedStr = json.optString("lastModified", null);

            try {
                DateTime lastModifiedDate = DateTime.parse(lastModifiedStr);
                if (lastModifiedDate != null) {
                    this.lastModified = lastModifiedDate.getMillis();
                }
            } catch(Exception ex) {
                LOGGER.warn(String.format("Invalid %s for %s: %s", "lastModified", this.id, lastModifiedStr));
            }
        }

        this.lastDownloaded = 0;
        if (json.has("lastDownloaded")) {
            String lastDownloadedStr = json.optString("lastDownloaded", null);

            try {
                DateTime lastDownloadedDate = DateTime.parse(lastDownloadedStr);
                if (lastDownloadedDate != null) {
                    this.lastDownloaded = lastDownloadedDate.getMillis();
                }
            } catch(Exception ex) {
                LOGGER.warn(String.format("Invalid %s for %s: %s", "lastDownloaded", this.id, lastDownloadedStr));
            }
        }

        this.errorMessage = json.optString("errorMessage", null);
        this.setStacktrace(json.optJSONArray("stacktrace"));

        this.setStatus(json.optString("status", null));

        this.attributes = json.optJSONObject("attributes");

        this.variableMetadataBeanMap = new HashMap<String, VariableMetadataBean>();
        JSONObject jsonVariableMetadataMap = json.optJSONObject("variables");
        if (jsonVariableMetadataMap != null) {
            for (String variableId : jsonVariableMetadataMap.keySet()) {
                JSONObject jsonVariableMetadata = jsonVariableMetadataMap.optJSONObject(variableId);
                if (jsonVariableMetadata != null) {
                    this.variableMetadataBeanMap.put(variableId, new VariableMetadataBean(jsonVariableMetadata));
                }
            }
        }

        this.initVariableMetadataParent(this.variableMetadataBeanMap);
    }

    /**
     * Create a {@code NetCDFMetadataBean} from a NetCDF file of a GRIB file.
     *
     * @param definitionId The definition ID. See {@link #getDefinitionId()}.
     * @param datasetId Unique identifier representing the file. See {@link #getDatasetId()}.
     * @param fileURI The URI of the NetCDF file or GRIB file. See {@link #getFileURI()}.
     * @param netCDFFile Local copy of the downloaded NetCDF file or GRIB file.
     * @param lastModified Last modified timestamp as defined in the {@code catalog.xml}.
     * @return A {@code NetCDFMetadataBean} metadata object, or null if the file is corrupted, missing or not readable.
     */
    public static NetCDFMetadataBean create(String definitionId, String datasetId, URI fileURI, File netCDFFile, long lastModified) {
        return NetCDFMetadataBean.create(definitionId, datasetId, fileURI, netCDFFile, lastModified, true);
    }

    /**
     * Internal method used to create a {@code NetCDFMetadataBean} from a NetCDF file of a GRIB file.
     * It is used to skip the checksum process, to run unit tests faster where checksum is not necessary.
     * Use {@link #create(String, String, URI, File, long)} instead if you want to use the metadata
     * in a real application.
     *
     * @param definitionId The definition ID. See {@link #getDefinitionId()}.
     * @param datasetId Unique identifier representing the file. See {@link #getDatasetId()}.
     * @param fileURI The URI of the NetCDF file or GRIB file. See {@link #getFileURI()}.
     * @param netCDFFile Local copy of the downloaded NetCDF file or GRIB file.
     * @param lastModified Last modified timestamp as defined in the {@code catalog.xml}.
     * @param withChecksum {@code true} to calculate the netCDFFile checksum, {@code false} otherwise. See {@link #getChecksum()}.
     * @return A {@code NetCDFMetadataBean} metadata object, or null if the file is corrupted, missing or not readable.
     */
    public static NetCDFMetadataBean create(String definitionId, String datasetId, URI fileURI, File netCDFFile, long lastModified, boolean withChecksum) {
        NetCDFMetadataBean metadata;
        try {
            metadata = new NetCDFMetadataBean(definitionId, datasetId);
            metadata.loadFromNetCDFFile(netCDFFile, lastModified, withChecksum);
            metadata.fileURI = fileURI;
            metadata.status = Status.VALID;
        } catch(Exception ex) {
            OutOfMemoryError outOfMemory = NetCDFUtils.getOutOfMemoryErrorCause(ex);
            if (outOfMemory != null) {
                throw outOfMemory;
            }
            metadata = new NetCDFMetadataBean(definitionId, datasetId);
            metadata.lastModified = lastModified;
            metadata.errorMessage = Utils.getExceptionMessage(ex);
            metadata.status = Status.CORRUPTED;
            metadata.setStacktrace(ex);
        }
        return metadata;
    }

    private void loadFromNetCDFFile(File netCDFFile, long lastModified, boolean withChecksum) throws Exception {
        this.lastModified = lastModified;
        try (NetcdfDataset netcdfDataset = NetcdfDatasetAggregator.getDataset(netCDFFile.getAbsolutePath(), false)) {
            LOGGER.debug(String.format("Extracting attributes for NetCDF file: %s", netCDFFile));
            this.attributes = this.getNetCDFAttributes(netcdfDataset);

            this.variableMetadataBeanMap = new HashMap<String, VariableMetadataBean>();

            LOGGER.debug(String.format("Extracting variables for NetCDF file: %s", netCDFFile));
            this.variableMetadataBeanMap = this.loadVariableMetadataBeanMap(netCDFFile, netcdfDataset);

            if (withChecksum) {
                LOGGER.debug(String.format("Calculating checksum for NetCDF file: %s", netCDFFile));
                this.checksum = String.format("%s:%s", CHECKSUM_ALGORITHM, Utils.checksum(netCDFFile, CHECKSUM_ALGORITHM));
            } else {
                LOGGER.warn(String.format("Skipping checksum for NetCDF file: %s", netCDFFile));
            }

            // Don't trust UCAR to deal with resources properly. Insist on freeing the resource.
            // NOTE: It's usually unnecessary to call releaseDataset twice, but it doesn't hurt to call it too often.
            //     If the library doesn't release all its links to the resource, the file system will agree to delete
            //     the NetCDF file but the disk space won't be freed until the Java VM dies.
            NetcdfDatasetAggregator.releaseDataset(netcdfDataset);
            NetcdfDatasetAggregator.releaseDataset(netcdfDataset);
        }
    }

    /**
     * Create a {@code Map} of {@code NetCDFMetadataBean} from a {@code Map} of {@code JSONObject}.
     * The key association from the input {@code Map} is respected in the output {@code Map}.
     * See {@link #NetCDFMetadataBean(JSONObject)}.
     *
     * @param jsonMetadatas a {@code Map} of {@code JSONObject}.
     * @return a {@code Map} of {@code NetCDFMetadataBean}.
     */
    public static Map<String, NetCDFMetadataBean> parseAll(Map<String, JSONObject> jsonMetadatas) {
        Map<String, NetCDFMetadataBean> metadatas = new HashMap<String, NetCDFMetadataBean>();

        if (jsonMetadatas != null) {
            for (Map.Entry<String, JSONObject> jsonMetadata : jsonMetadatas.entrySet()) {
                metadatas.put(
                    jsonMetadata.getKey(),
                    new NetCDFMetadataBean(jsonMetadata.getValue())
                );
            }
        }

        return metadatas;
    }

    /**
     * Return a map containing all the Global Attributes of the NetCDF file.
     *
     * <p>NOTE: It seems like the EDAL library is a wrapper around the UCAR library.
     *   The only way to access the file attributes (aka Global Attributes) is to
     *   use the UCAR library.</p>
     *
     * <p>See: {@code uk.ac.rdg.resc.edal.dataset.cdm.CdmGridDatasetFactory}
     *   <a href="https://github.com/Reading-eScience-Centre/edal-java/blob/master/cdm/src/main/java/uk/ac/rdg/resc/edal/dataset/cdm/CdmGridDatasetFactory.java" target="_blank">https://github.com/Reading-eScience-Centre/edal-java/blob/master/cdm/src/main/java/uk/ac/rdg/resc/edal/dataset/cdm/CdmGridDatasetFactory.java</a></p>
     *
     * @param netcdfDataset The NetCDF dataset to parse
     * @return A map of attributes
     */
    private JSONObject getNetCDFAttributes(NetcdfDataset netcdfDataset) {
        if (netcdfDataset == null) {
            return null;
        }

        return NetCDFMetadataBean.loadAttributes(netcdfDataset.getGlobalAttributes());
    }

    /**
     * Load a list of UCAR {@code Attribute} into a {@code JSONObject}
     * to make it easier to parse.
     *
     * @param attributes List of UCAR {@code Attribute}.
     * @return {@code JSONObject} Tree of attribute serialised as {@code JSONObject}.
     */
    public static JSONObject loadAttributes(List<Attribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }

        JSONObject jsonAttributes = new JSONObject();
        for (Attribute attribute : attributes) {
            String attributeName = attribute.getShortName();
            if (attributeName != null) {
                Object attributeValue = null;

                int length = attribute.getLength();
                if (length > 1) {
                    JSONArray attributeValues = new JSONArray();
                    for (int i=0; i<length; i++) {
                        attributeValues.put(NetCDFMetadataBean.getAttributeValue(attribute, i));
                    }
                    attributeValue = attributeValues;
                } else if (length == 1) {
                    attributeValue = NetCDFMetadataBean.getAttributeValue(attribute, 0);
                }

                if (attributeValue != null) {
                    jsonAttributes.put(attributeName, attributeValue);
                }
            }
        }

        return jsonAttributes;
    }

    // Inspired of ucar.nc2.Attribute.writeCDL()
    private static Object getAttributeValue(Attribute attribute, int i) {
        if (attribute.isString()) {
            return attribute.getStringValue(i);

        } else if (attribute.getEnumType() != null) {
            EnumTypedef en = attribute.getEnumType();
            String econst = attribute.getStringValue(i);
            Integer ecint = en.lookupEnumInt(econst);
            if (ecint != null) {
                return Attribute.encodeString(econst);
            }

        } else {
            Number rawNumber = attribute.getNumericValue(i);
            if (rawNumber == null) {
                return null;
            }

            try {
                DataType dataType = attribute.getDataType();

                if (dataType == DataType.DOUBLE) {
                    Double doubleNumber = (Double)rawNumber;
                    if (!doubleNumber.isNaN() && !doubleNumber.isInfinite()) {
                        return rawNumber.doubleValue();
                    }

                } else if (dataType == DataType.FLOAT) {
                    Float floatNumber = (Float)rawNumber;
                    if (!floatNumber.isNaN() && !floatNumber.isInfinite()) {
                        return rawNumber.floatValue();
                    }

                } else if (dataType == DataType.SHORT) {
                    return rawNumber.shortValue();

                } else if (dataType == DataType.BYTE) {
                    return rawNumber.byteValue();

                } else if (dataType == DataType.LONG) {
                    return rawNumber.longValue();

                } else if (dataType == DataType.INT) {
                    return rawNumber.intValue();
                }

            } catch (Exception ex) {
                LOGGER.warn(String.format("Error occurred while extracting the numerical value of the number: %s", rawNumber), ex);
            }

            // If the number is NaN, infinite or an error occurred while extracting the numerical value, return the number as a String.
            return rawNumber.toString();

            // The UCAR library adds prefixes to the number.
            // I don't think they are very useful...
            /*
            StringBuilder valueSb = new StringBuilder(attribute.getNumericValue(i));

            DataType dataType = attribute.getDataType();
            boolean unsigned = attribute.isUnsigned();
            if (dataType == DataType.FLOAT) {
                valueSb.append("f");
            } else if (dataType == DataType.SHORT) {
                if (unsigned) {
                    valueSb.append("US");
                } else {
                    valueSb.append("S");
                }
            } else if (dataType == DataType.BYTE) {
                if (unsigned) {
                    valueSb.append("UB");
                } else {
                    valueSb.append("B");
                }
            } else if (dataType == DataType.LONG) {
                if (unsigned) {
                    valueSb.append("UL");
                } else {
                    valueSb.append("L");
                }
            } else if (dataType == DataType.INT && unsigned) {
                valueSb.append("U");
            }

            return valueSb.toString();
            */
        }

        return null;
    }



    private Map<String, VariableMetadataBean> loadVariableMetadataBeanMap(File netCDFFile, NetcdfDataset netcdfDataset) throws IOException {
        Map<String, VariableMetadataBean> variableMetadataMap = new HashMap<String, VariableMetadataBean>();

        GriddedDataset dataset = NetCDFUtils.getNetCDFDataset(netCDFFile);
        if (dataset != null) {
            Set<String> variableIds = dataset.getVariableIds();
            if (variableIds != null && !variableIds.isEmpty()) {
                for (String variableId : variableIds) {
                    VariableMetadata variableMetadata = dataset.getVariableMetadata(variableId);

                    Variable variable = netcdfDataset == null ? null : netcdfDataset.findVariable(variableId);

                    if (variableMetadata != null) {
                        variableMetadataMap.put(variableId, new VariableMetadataBean(variableMetadata, variable));
                    }
                }
            }
        }

        this.initVariableMetadataParent(variableMetadataMap);

        return variableMetadataMap;
    }

    private void initVariableMetadataParent(Map<String, VariableMetadataBean> variableMetadataMap) {
        if (variableMetadataMap != null && !variableMetadataMap.isEmpty()) {
            for (VariableMetadataBean variableMetadataBean : variableMetadataMap.values()) {
                String parentId = variableMetadataBean.getParentId();
                if (parentId != null) {
                    VariableMetadataBean parent = variableMetadataMap.get(parentId);
                    if (parent != null) {
                        variableMetadataBean.setParent(parent);
                    }
                }
            }
        }
    }

    /**
     * Returns the {@code NetCDFMetadataBean} ID.
     * If it's not set, construct an ID using the {@code definitionId} and
     * the {@code datasetId} and returns it.
     *
     * @return the {@code NetCDFMetadataBean} ID.
     */
    public String getId() {
        if (this.id == null) {
            this.id = NetCDFMetadataBean.getUniqueDatasetId(this.definitionId, this.datasetId);
        }

        return this.id;
    }

    /**
     * Construct a unique {@code NetCDFMetadataBean} ID using a {@code definitionId} and
     * a {@code datasetId}.
     * This method assume the resulting ID is unique. It doesn't check the database.
     *
     * @param definitionId the definitionId. See {@link #getDefinitionId()}.
     * @param datasetId the datasetId. See {@link #getDatasetId()}.
     * @return the {@code NetCDFMetadataBean} ID.
     */
    public static String getUniqueDatasetId(String definitionId, String datasetId) {
        return NetCDFMetadataBean.getUniqueDatasetId(definitionId, datasetId, true);
    }

    /**
     * Internal method used to construct a unique {@code NetCDFMetadataBean} ID using
     * a {@code definitionId} and a {@code datasetId}.
     * This method assume the resulting ID is unique. It doesn't check the database.
     *
     * @param definitionId the definitionId. See {@link #getDefinitionId()}.
     * @param datasetId the datasetId. See {@link #getDatasetId()}.
     * @param safe {@code true} to replace invalid characters in the definitionId and the datasetId
     *   when creating the ID. False to keep it the IDs as-is. This is used to create ID containing
     *   placeholders which haven't been replaced yet.
     * @return the {@code NetCDFMetadataBean} ID.
     */
    public static String getUniqueDatasetId(String definitionId, String datasetId, boolean safe) {
        StringBuilder idSb = new StringBuilder();
        if (definitionId == null || definitionId.isEmpty()) {
            idSb.append("UNKNOWN_DEFINITION");
        } else {
            idSb.append(safe ? AbstractBean.safeIdValue(definitionId) : definitionId);
        }

        idSb.append("/");

        if (datasetId == null || datasetId.isEmpty()) {
            idSb.append("UNKNOWN_DATASET");
        } else {
            idSb.append(safe ? AbstractBean.safeIdValue(datasetId) : datasetId);
        }

        return idSb.toString();
    }

    /**
     * Returns definition ID of the object that download or create the file.
     *   For the {@code ereefs-download-manager}, it's {@link au.gov.aims.ereefs.bean.download.DownloadBean#getId()}.
     *
     * @return the {@code definitionId}.
     */
    public String getDefinitionId() {
        return this.definitionId;
    }

    /**
     * Returns the dataset ID, the unique identifier representing the file.
     *   For the {@code ereefs-download-manager}, it's dataset ID defined in the {@code catalog.xml}.
     *
     * @return the {@code datasetId}.
     */
    public String getDatasetId() {
        return this.datasetId;
    }

    /**
     * Returns the file URI, where the NetCDF file or GRIB file is stored after been downloaded.
     *   Supports {@code s3://} and {@code file://} protocols.
     *
     * @return the {@code fileURI}.
     */
    public URI getFileURI() {
        return this.fileURI;
    }

    private void setFileURIStr(String fileURIStr) {
        this.fileURI = null;
        if (fileURIStr != null && !fileURIStr.isEmpty()) {
            try {
                this.fileURI = new URI(fileURIStr);
            } catch(Exception ex) {
                LOGGER.warn(String.format("Invalid file URI %s: %s",
                        fileURIStr, Utils.getExceptionMessage(ex)), ex);
            }
        }
    }

    /**
     * Returns the checksum of the NetCDF file or GRIB file, if it has been calculated.
     * @return the NetCDF file or GRIB file checksum.
     */
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * Returns the last modified timestamp of the NetCDF file or GRIB file.
     * If the file was downloaded from a THREDDS server, it's the last modified
     * found in the {@code catalog.xml}.
     *
     * @return last modified timestamp.
     */
    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * Set the last modified timestamp of the NetCDF file or GRIB file.
     * @param lastModified last modified timestamp.
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns the timestamp of when the file was downloaded.
     * @return downloaded timestamp.
     */
    public long getLastDownloaded() {
        return this.lastDownloaded > 0 ? this.lastDownloaded : this.lastModified;
    }

    /**
     * Set the timestamp of when the file was downloaded.
     * @param lastDownloaded downloaded timestamp.
     */
    public void setLastDownloaded(long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    /**
     * Returns an error message if an error has occurred while inspecting the data.
     * @return an error message if any.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Set the error message, if an error occurs while inspecting the data.
     * @param errorMessage an error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns an exception stack trace if an exception has occurred while inspecting the data.
     * @return an exception stack trace.
     */
    public List<String> getStacktrace() {
        return this.stacktrace;
    }

    /**
     * Set the exception stacktrace, if an exception occurs while inspecting the data.
     * @param stacktrace the stack trace of the exception.
     */
    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }

    /**
     * Set the exception stacktrace.
     * Used when parsing the metadata JSON document retrieved from the database.
     *
     * @param jsonArray the exception stacktrace.
     */
    public void setStacktrace(JSONArray jsonArray) {
        this.stacktrace = null;
        if (jsonArray != null && jsonArray.length() > 0) {
            this.stacktrace = new ArrayList<String>();
            for (int i=0; i<jsonArray.length(); i++) {
                this.stacktrace.add(jsonArray.optString(i, null));
            }
        }
    }

    /**
     * Set the exception stacktrace, if an exception occurs while inspecting the data.
     * @param exception the exception that has occurred.
     */
    public void setStacktrace(Throwable exception) {
        this.stacktrace = null;
        if (exception != null) {
            this.stacktrace = new ArrayList<String>();

            Throwable cause = exception;
            do {
                StackTraceElement[] stackTraceElements = cause.getStackTrace();
                if (stackTraceElements != null && stackTraceElements.length > 0) {
                    for (StackTraceElement stackTraceElement : stackTraceElements) {
                        if (stackTraceElement != null) {
                            this.stacktrace.add(stackTraceElement.toString());
                        }
                    }
                }

                cause = cause.getCause();
                if (cause != null) {
                    this.stacktrace.add("Caused by: " + Utils.getExceptionMessage(cause));
                }
            } while (cause != null);
        }
    }

    /**
     * Returns the status of the downloaded file.
     * @return downloaded file status.
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * Set the status of the downloaded file.
     * @param status downloaded file status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    private void setStatus(String statusStr) {
        this.status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                this.status = Status.valueOf(statusStr.toUpperCase());
            } catch (Exception ex) {
                LOGGER.warn("Invalid metadata status: " + statusStr);
            }
        }
    }

    /**
     * Returns the {@code Map} of {@link VariableMetadataBean} for the
     * NetCDF file or GRIB file.
     *
     * @return the {@code Map} of {@link VariableMetadataBean}.
     */
    public Map<String, VariableMetadataBean> getVariableMetadataBeanMap() {
        return this.variableMetadataBeanMap;
    }

    /**
     * Returns the global attributes tree found in the NetCDF file or GRIB file.
     * NOTE: NetCDF files and GRIB files attributes are very flexible.
     *   The value of a global attribute can be a native type (string, int, etc),
     *   a list of attributes or a map of attributes.
     *
     * @return global attributes tree.
     */
    public JSONObject getAttributes() {
        return this.attributes;
    }

    /**
     * Returns {@code true} if the NetCDF file or GRIB file contains variables,
     * {@code false} otherwise.
     *
     * @return {@code true} if the file contains data variables.
     */
    public boolean isEmpty() {
        if (this.variableMetadataBeanMap == null) {
            return true;
        }
        return this.variableMetadataBeanMap.isEmpty();
    }

    /**
     * Serialise the object into a {@code JSONObject}.
     * @return a {@code JSONObject} representing the object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        json.put("version", VERSION);
        json.put("_id", this.getId());
        json.put("definitionId", this.definitionId);
        json.put("datasetId", this.datasetId);
        json.put("type", this.type.name());
        json.put("fileURI", this.fileURI);

        json.put("checksum", this.checksum);
        json.put("lastModified", new DateTime(this.lastModified).toString());
        json.put("lastDownloaded", new DateTime(this.getLastDownloaded()).toString());
        json.put("errorMessage", this.errorMessage);

        if (this.stacktrace != null && !this.stacktrace.isEmpty()) {
            json.put("stacktrace", this.stacktrace);
        }

        json.put("status", this.status);

        if (this.attributes != null && !this.attributes.isEmpty()) {
            json.put("attributes", this.attributes);
        }

        if (this.variableMetadataBeanMap != null && !this.variableMetadataBeanMap.isEmpty()) {
            JSONObject jsonVariableMetadataMap = new JSONObject();

            for (Map.Entry<String, VariableMetadataBean> variableMetadataBeanEntry : this.variableMetadataBeanMap.entrySet()) {
                String variableId = variableMetadataBeanEntry.getKey();
                VariableMetadataBean variableMetadataBean = variableMetadataBeanEntry.getValue();
                if (variableId != null && variableMetadataBean != null) {
                    JSONObject jsonVariableMetadata = variableMetadataBean.toJSON();
                    if (jsonVariableMetadata != null) {
                        jsonVariableMetadataMap.put(variableId, jsonVariableMetadata);
                    }
                }
            }

            json.put("variables", jsonVariableMetadataMap);
        }

        return json;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetCDFMetadataBean that = (NetCDFMetadataBean) o;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.definitionId, that.definitionId) &&
                Objects.equals(this.datasetId, that.datasetId) &&
                Objects.equals(
                    this.fileURI == null ? null : this.fileURI.toString(),
                    that.fileURI == null ? null : that.fileURI.toString()
                );
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.definitionId, this.datasetId,
                this.fileURI == null ? null : this.fileURI.toString());
    }

    /**
     * Enum representing the status of a file associated with the metadata.
     * It's used with the {@link NetCDFMetadataBean} and
     * with {@link au.gov.aims.ereefs.bean.metadata.ncanimate.NcAnimateOutputFileMetadataBean}.
     * It's part of the {@link NetCDFMetadataBean} which is used with
     * the {@code ereefs-download-manager} project, {@code ereefs-ncanimate2} project
     * and other eReefs projects.
     */
    public enum Status {
        VALID, CORRUPTED, DELETED
    }
}
