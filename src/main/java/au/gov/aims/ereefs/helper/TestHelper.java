/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.AbstractManager;
import au.gov.aims.ereefs.database.manager.DownloadManager;
import au.gov.aims.ereefs.database.manager.MetadataManager;
import au.gov.aims.ereefs.database.manager.ProductManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigManager;
import au.gov.aims.ereefs.database.manager.ncanimate.ConfigPartManager;
import au.gov.aims.json.JSONUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * This classes is used by JUnit tests to create the project tables
 * (aka {@code collections}) in the database.
 */
public class TestHelper {
    private static final Logger LOGGER = Logger.getLogger(TestHelper.class);

    /**
     * Create all the tables needed to test the
     * {@code ereefs-download-manager} project and the
     * {@code ereefs-ncanimate2} project.
     *
     * @param databaseClient the {@link DatabaseClient} linked to the database
     *     in which the table will be created.
     * @throws Exception if the database is unreachable.
     */
    public static void createTables(DatabaseClient databaseClient) throws Exception {
        databaseClient.createTable(MetadataManager.TABLE_NAME);
        databaseClient.createTable(DownloadManager.TABLE_NAME);
        databaseClient.createTable(ProductManager.TABLE_NAME);
        databaseClient.createTable(ConfigManager.TABLE_NAME);
        databaseClient.createTable(ConfigPartManager.TABLE_NAME, "_id.id", "_id.datatype");
    }

    /**
     * Insert NcAnimate configuration document to the database,
     * to run the tests for {@code ereefs-ncanimate2} project.
     *
     * @param configPartManager the {@link ConfigManager} or {@link ConfigPartManager}
     *     used to insert configuration to the database.
     * @param path path to the resource directory containing the JSON configuration files or configuration part files.
     * @param partName the name of the configuration file or configuration part files,
     *     used to produce user friendly error message is something goes wrong with the configuration file.
     * @throws Exception if something goes wrong while inserting the configuration document to the database.
     */
    public static void insertTestConfigs(AbstractManager configPartManager, String path, String partName) throws Exception {
        TestHelper.insertTestConfigs(configPartManager, path, partName, null, false);
    }

    /**
     * Insert NcAnimate configuration document to the database,
     * to run the tests for {@code ereefs-ncanimate2} project.
     *
     * <p>This method allows a {@code Map} of substitutions that can be applied
     * to the configuration before inserting the document to the database.
     * Those substitutions usually contains paths which can only be known at
     * the moment the test run.</p>
     *
     * @param configPartManager the {@link ConfigManager} or {@link ConfigPartManager}
     *     used to insert configuration to the database.
     * @param path path to the resource directory containing the JSON configuration files or configuration part files.
     * @param partName the name of the configuration file or configuration part files,
     *     used to produce user friendly error message is something goes wrong with the configuration file.
     * @param substitutions {@code Map} of substitutions to apply to the configuration before inserting it to the database.
     * @throws Exception if something goes wrong while inserting the configuration document to the database.
     */
    public static void insertTestConfigs(AbstractManager configPartManager, String path, String partName, Map<String, String> substitutions) throws Exception {
        TestHelper.insertTestConfigs(configPartManager, path, partName, substitutions, false);
    }

    /**
     * Insert NcAnimate configuration document to the database,
     * to run the tests for {@code ereefs-ncanimate2} project.
     *
     * <p>This method allows a {@code Map} of substitutions that can be applied
     * to the configuration before inserting the document to the database.
     * Those substitutions usually contains paths which can only be known at
     * the moment the test run.</p>
     *
     * @param configPartManager the {@link ConfigManager} or {@link ConfigPartManager}
     *     used to insert configuration to the database.
     * @param path path to the resource directory containing the JSON configuration files or configuration part files.
     * @param partName the name of the configuration file or configuration part files,
     *     used to produce user friendly error message is something goes wrong with the configuration file.
     * @param substitutions {@code Map} of substitutions to apply to the configuration before inserting it to the database.
     * @param recursive look through the {@code path} directory recursively to find JSON configuration files.
     * @throws Exception if something goes wrong while inserting the configuration document to the database.
     */
    public static void insertTestConfigs(
            AbstractManager configPartManager,
            String path,
            String partName,
            Map<String, String> substitutions,
            boolean recursive
    ) throws Exception {
        LOGGER.info(String.format("Looking for JSON files in: %s", path));
        try (InputStream directoryInputStream = TestHelper.class.getClassLoader().getResourceAsStream(path)) {
            if (directoryInputStream == null) {
                LOGGER.warn(String.format("Resource directory not found: %s", path));
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(directoryInputStream))) {

                    JSONObject jsonSavedPart;
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        if (resource.endsWith(".json")) {
                            String absolutePath = path + "/" + resource;
                            try (InputStream configInputStream = TestHelper.class.getClassLoader().getResourceAsStream(absolutePath)) {
                                if (configInputStream == null) {
                                    throw new IllegalArgumentException(String.format("Resource file not found: %s", path));
                                } else {
                                    LOGGER.debug(String.format("Inserting %s, file: %s", partName, absolutePath));
                                    jsonSavedPart = configPartManager.save(
                                            TestHelper.parseJSONStream(configInputStream, substitutions));

                                    if (jsonSavedPart == null) {
                                        throw new IOException(String.format("Inserted %s is null. File: %s", partName, absolutePath));
                                    }
                                }
                            }
                        } else if (recursive) {
                            String subPath = String.format("%s/%s", path, resource);
                            TestHelper.insertTestConfigs(configPartManager, subPath, partName, substitutions, recursive);
                        }
                    }
                }
            }
        }
    }

    private static JSONObject parseJSONStream(InputStream configInputStream, Map<String, String> substitutions) throws IOException {
        String configString = JSONUtils.streamToString(configInputStream, true);

        if (substitutions != null) {
            for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
                configString = configString.replace(substitution.getKey(), substitution.getValue());
            }
        }

        return new JSONObject(configString);
    }
}
