/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.aws.s3.entity.S3Client;
import au.gov.aims.aws.s3.manager.DownloadManager;
import au.gov.aims.ereefs.bean.metadata.ncanimate.NcAnimateOutputFileMetadataBean;
import au.gov.aims.ereefs.bean.metadata.netcdf.NetCDFMetadataBean;
import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import com.amazonaws.services.s3.AmazonS3URI;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Helper class used to simplify interaction with the database.
 *
 * <p>This class relates to the {@link NetCDFMetadataBean}
 * and {@link NcAnimateOutputFileMetadataBean},
 * used with the {@code ereefs-ncanimate2} project,
 * the {@code ereefs-download-manager} project, etc.</p>
 */
public class MetadataHelper {
    private static final Logger LOGGER = Logger.getLogger(MetadataHelper.class);

    private DatabaseClient dbClient;
    private MetadataManager metadataManager;

    /**
     * @deprecated Use {@link MetadataHelper#(DatabaseClient, CacheStrategy)}
     * @param dbClient the {@link DatabaseClient} used to query the database.
     */
    @Deprecated
    public MetadataHelper(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Creates a {@code MetadataHelper} using a database client and a cache strategy.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public MetadataHelper(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        this.dbClient = dbClient;
        this.metadataManager = new MetadataManager(this.dbClient, cacheStrategy);
    }

    /**
     * Set the {@link MetadataManager} cache strategy.
     * @param cacheStrategy the new cache strategy.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.metadataManager.setCacheStrategy(cacheStrategy);
    }

    /**
     * Clear the {@link MetadataManager} cache.
     * @throws IOException if something goes wrong while clearing the disk cache.
     */
    public void clearCache() throws IOException {
        this.metadataManager.clearCache();
    }

    /**
     * Returns the {@link DatabaseClient} that was set in the constructor.
     * @return the {@link DatabaseClient}.
     */
    public DatabaseClient getDbClient() {
        return this.dbClient;
    }

    // NcAnimate product (output file) metadata

    /**
     * Returns an {@code Iterable} list of all the {@link NcAnimateOutputFileMetadataBean}
     * found in the database, for a given {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean} ID,
     * identified as {@code definitionId} in the metadata document.
     *
     * @param definitionId the {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean} ID.
     * @return an {@code Iterable} list of all the {@link NcAnimateOutputFileMetadataBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<NcAnimateOutputFileMetadataBean> getNcAnimateProductMetadatas(String definitionId) throws Exception {
        JSONObjectIterable jsonMetadatas = this.metadataManager.selectValidByDefinitionId(
                MetadataManager.MetadataType.NCANIMATE_PRODUCT, definitionId);

        Iterator<JSONObject> jsonMetadataIterator = jsonMetadatas.iterator();
        return new Iterable<NcAnimateOutputFileMetadataBean>() {
            @Override
            public Iterator<NcAnimateOutputFileMetadataBean> iterator() {
                return new Iterator<NcAnimateOutputFileMetadataBean>() {
                    @Override
                    public boolean hasNext() {
                        return jsonMetadataIterator.hasNext();
                    }

                    @Override
                    public NcAnimateOutputFileMetadataBean next() {
                        JSONObject jsonMetadata = jsonMetadataIterator.next();
                        if (jsonMetadata == null) {
                            return null;
                        }

                        try {
                            return new NcAnimateOutputFileMetadataBean(jsonMetadata);
                        } catch(OutOfMemoryError ex) {
                            throw ex;
                        } catch(Exception ex) {
                            LOGGER.error("Error occurred while converting a JSON output file metadata to a NcAnimateOutputFileMetadataBean", ex);
                        }
                        return null;
                    }
                };
            }
        };
    }

    /**
     * Retrieve a {@link NcAnimateOutputFileMetadataBean} from the database
     * for a given {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean}
     * ID ({@code definitionId}) and a given dataset ID.
     *
     * @param definitionId the {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean} ID.
     * @param datasetId the dataset ID.
     * @return the {@link NcAnimateOutputFileMetadataBean} or null if not found.
     * @throws Exception if the database is unreachable.
     */
    public NcAnimateOutputFileMetadataBean getNcAnimateProductMetadata(String definitionId, String datasetId) throws Exception {
        String id = NetCDFMetadataBean.getUniqueDatasetId(definitionId, datasetId);
        JSONObject jsonMetadata = this.metadataManager.select(id);
        if (jsonMetadata != null) {
            return new NcAnimateOutputFileMetadataBean(jsonMetadata);
        }

        return null;
    }

    /**
     * Delete a {@link NcAnimateOutputFileMetadataBean} from the database
     * for a given {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean}
     * ID ({@code definitionId}) and a given dataset ID.
     *
     * @param definitionId the {@link au.gov.aims.ereefs.bean.ncanimate.NcAnimateConfigBean} ID.
     * @param datasetId the dataset ID.
     * @return the deleted {@link NcAnimateOutputFileMetadataBean} or null if not found.
     * @throws Exception if the database is unreachable.
     */
    public NcAnimateOutputFileMetadataBean deleteNcAnimateProductMetadata(String definitionId, String datasetId) throws Exception {
        String id = NetCDFMetadataBean.getUniqueDatasetId(definitionId, datasetId);
        JSONObject jsonMetadata = this.metadataManager.delete(id);
        if (jsonMetadata != null) {
            return new NcAnimateOutputFileMetadataBean(jsonMetadata);
        }

        return null;
    }

    // NetCDF metadata

    /**
     * Returns an {@code Iterable} list of all the valid {@link NetCDFMetadataBean}
     * found in the database, for a given {@code definitionId}.
     *
     * @param definitionId the definition ID.
     * @return an {@code Iterable} list of all the valid {@link NetCDFMetadataBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<NetCDFMetadataBean> getValidNetCDFMetadatas(String definitionId) throws Exception {
        return this.getNetCDFMetadatas(definitionId, true);
    }

    /**
     * Returns an {@code Iterable} list of all the {@link NetCDFMetadataBean}
     * found in the database, for a given {@code definitionId}.
     *
     * @param definitionId the definition ID.
     * @return an {@code Iterable} list of all the {@link NetCDFMetadataBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<NetCDFMetadataBean> getAllNetCDFMetadatas(String definitionId) throws Exception {
        return this.getNetCDFMetadatas(definitionId, false);
    }

    private Iterable<NetCDFMetadataBean> getNetCDFMetadatas(String definitionId, boolean onlyValid) throws Exception {
        JSONObjectIterable jsonMetadatas = onlyValid ?
                this.metadataManager.selectValidByDefinitionId(MetadataManager.MetadataType.NETCDF, definitionId) :
                this.metadataManager.selectByDefinitionId(MetadataManager.MetadataType.NETCDF, definitionId);

        Iterator<JSONObject> jsonMetadataIterator = jsonMetadatas.iterator();
        return new Iterable<NetCDFMetadataBean>() {
            @Override
            public Iterator<NetCDFMetadataBean> iterator() {
                return new Iterator<NetCDFMetadataBean>() {
                    @Override
                    public boolean hasNext() {
                        return jsonMetadataIterator.hasNext();
                    }

                    @Override
                    public NetCDFMetadataBean next() {
                        JSONObject jsonMetadata = jsonMetadataIterator.next();
                        if (jsonMetadata == null) {
                            return null;
                        }

                        try {
                            return new NetCDFMetadataBean(jsonMetadata);
                        } catch(OutOfMemoryError ex) {
                            throw ex;
                        } catch(Exception ex) {
                            LOGGER.error("Error occurred while converting a JSON NetCDF metadata to a NetCDFMetadataBean", ex);
                        }
                        return null;
                    }
                };
            }
        };
    }

    /**
     * Retrieve a {@link NetCDFMetadataBean} from the database
     * for a given {@code definitionId} and a given {@code datasetId}.
     *
     * @param definitionId the definition ID.
     * @param datasetId the dataset ID.
     * @return the {@link NetCDFMetadataBean} or null if not found.
     * @throws Exception if the database is unreachable.
     */
    public NetCDFMetadataBean getNetCDFMetadata(String definitionId, String datasetId) throws Exception {
        String id = NetCDFMetadataBean.getUniqueDatasetId(definitionId, datasetId);
        JSONObject jsonMetadata = this.metadataManager.select(id);
        if (jsonMetadata != null) {
            return new NetCDFMetadataBean(jsonMetadata);
        }

        return null;
    }

    /**
     * Download the NetCDF file associated with a {@link NetCDFMetadataBean}, to a given
     * location on disk.
     *
     * @param definitionId the definition ID.
     * @param datasetId the dataset ID.
     * @param outputFile location where the NetCDF file should be downloaded.
     * @throws Exception if the database is unreachable or disk error.
     */
    public void downloadNetCDFFile(String definitionId, String datasetId, File outputFile) throws Exception {
        NetCDFMetadataBean metadata = this.getNetCDFMetadata(definitionId, datasetId);
        this.downloadNetCDFFile(metadata, outputFile);
    }

    /**
     * Download the NetCDF file associated with a {@link NetCDFMetadataBean}, to a given
     * location on disk.
     *
     * @param metadata the {@link NetCDFMetadataBean} of the NetCDF file to download.
     * @param outputFile location where the NetCDF file should be downloaded.
     * @throws IOException on disk error.
     */
    public void downloadNetCDFFile(NetCDFMetadataBean metadata, File outputFile) throws IOException {
        try (S3Client s3Client = new S3Client()) {
            this.downloadNetCDFFile(metadata, outputFile, s3Client);
        }
    }

    /**
     * Download the NetCDF file associated with a {@link NetCDFMetadataBean}, to a given
     * location on disk.
     *
     * @param metadata the {@link NetCDFMetadataBean} of the NetCDF file to download.
     * @param outputFile location where the NetCDF file should be downloaded.
     * @param s3Client the S3 client to use to download the NetCDF file.
     * @throws IOException on disk error.
     */
    public void downloadNetCDFFile(NetCDFMetadataBean metadata, File outputFile, S3Client s3Client) throws IOException {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata is null");
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("Output file is null");
        }

        // Prepare output file (check if everything is ok, create parent dir if needed)
        if (outputFile.exists()) {
            if (outputFile.isFile()) {
                if (!outputFile.canWrite()) {
                    throw new IOException("Output file exist but it's not writable: " + outputFile);
                }
            } else {
                throw new IOException("Output file exist but it's not a normal file: " + outputFile);
            }
        } else {
            File parentDir = outputFile.getParentFile();
            if (parentDir.exists()) {
                if (!parentDir.isDirectory()) {
                    throw new IOException("Output file directory exists but it's not a directory: " + outputFile);
                }
            } else {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Output file directory doesn't exist and can not be created: " + outputFile);
                }
            }
        }

        URI fileUri = metadata.getFileURI();
        if (fileUri == null) {
            throw new IllegalArgumentException("Metadata file URI is null");
        }

        if ("s3".equalsIgnoreCase(fileUri.getScheme())) {
            AmazonS3URI s3Uri = new AmazonS3URI(fileUri);
            DownloadManager.download(s3Client, s3Uri, outputFile);
        } else {
            File sourceFile = new File(fileUri);
            if (!sourceFile.exists()) {
                throw new IOException("Metadata file doesn't exists: " + fileUri);
            }
            if (!sourceFile.canRead()) {
                throw new IOException("Metadata file is not readable: " + fileUri);
            }

            Files.createSymbolicLink(outputFile.toPath(), new File(fileUri).toPath());
        }
    }
}
