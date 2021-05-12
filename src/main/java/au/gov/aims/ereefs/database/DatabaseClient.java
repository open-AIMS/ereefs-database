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
package au.gov.aims.ereefs.database;

import au.gov.aims.ereefs.database.table.DatabaseCompositeKeyTable;
import au.gov.aims.ereefs.database.table.DatabaseSingleKeyTable;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.internal.connection.ServerAddressHelper;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Object used to establish a connection to a {@code MongoDB} database.
 *
 * <p>Inspired from <a href="https://www.baeldung.com/java-mongodb" target="_blank">https://www.baeldung.com/java-mongodb</a></p>
 *
 * <p>API reference and tutorial: <a href="https://mongodb.github.io/mongo-java-driver/3.10/driver/tutorials/authentication/" target="_blank">https://mongodb.github.io/mongo-java-driver/3.10/driver/tutorials/authentication/</a></p>
 *
 * <p>Composite primary key reference: <a href="https://docs.mongodb.com/manual/core/index-compound/" target="_blank">https://docs.mongodb.com/manual/core/index-compound/</a></p>
 */
public class DatabaseClient {
    private static final Logger LOGGER = Logger.getLogger(DatabaseClient.class);
    private static final String AUTHENTICATION_SOURCE = "admin";
    private static final String VARIABLE_STORE_PREFIX_ENVIRONMENT_VARIABLE = "EXECUTION_ENVIRONMENT";

    // NOTE: The database is not 100% reliable. Sometime it disconnect (example: during server backups).
    //     Each method that access the DB is in a loop that try to re-establish a connection to the DB
    //     when it fail, up to X number of times, with a sufficient delay between retry to give a chance
    //     to the DB to recover / restart.
    // 10 attempts = 10 seconds + 2*10 seconds + ... + 15*10 seconds = 1200 seconds
    //     = 20 minutes (without considering timeout / running time of each attempts)
    private static final int DEFAULT_DB_RETRY_ATTEMPTS = 15;
    private static final int DEFAULT_DB_INITIAL_DELAY_BETWEEN_ATTEMPT = 10; // in seconds

    private int dbRetryAttempts = DEFAULT_DB_RETRY_ATTEMPTS;
    private int dbInitialDelayBetweenAttempt = DEFAULT_DB_INITIAL_DELAY_BETWEEN_ATTEMPT;

    private String appName;

    private ServerAddress serverAddr;
    private String databaseName;
    private MongoCredential credential;

    /**
     * Creates a {@code DatabaseClient} for a given application name.
     *
     * <p>It retrieves the connection information such as the
     * {@code userid} and the {@code password}, from the AWS
     * Systems Manager (SSM) parameter store.</p>
     *
     * <p>The parameter are expected to be found in the following path:</p>
     * <ul>
     *     <li>/<variableStorePrefix>/<appName>/mongodb/userid</li>
     *     <li>/<variableStorePrefix>/<appName>/mongodb/password</li>
     * </ul>
     *
     * <p>It also used the following global parameters:</p>
     * <ul>
     *     <li>/<variableStorePrefix>/global/mongodb/host</li>
     *     <li>/<variableStorePrefix>/global/mongodb/port</li>
     *     <li>/<variableStorePrefix>/global/mongodb/db</li>
     * </ul>
     *
     * <p>AWS API documentation: <a href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/simplesystemsmanagement/AWSSimpleSystemsManagement.html#getParameters-com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest-" target="_blank">https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/simplesystemsmanagement/AWSSimpleSystemsManagement.html#getParameters-com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest-</a></p>
     *
     * @param appName the application name. For example: {@code ncanimate}.
     */
    public DatabaseClient(String appName) {
        this.appName = appName;
        this.resolveServerAddr();
    }

    /**
     * Creates a {@code DatabaseClient} from basic database connection information.
     * This is used with unit tests.
     *
     * @param host the database host.
     * @param port the database connection port.
     * @param databaseName the database name.
     * @param userId the username used to connect to the database.
     * @param password the password used to connect to the database.
     */
    public DatabaseClient(String host, int port, String databaseName, String userId, String password) {
        this.init(host, port, databaseName, userId, password);
    }

    /**
     * Creates a {@code DatabaseClient} from a {@code ServerAddress}
     * object and a database name.
     * This is used with unit tests.
     *
     * @param serverAddr the server address used to connect to the database.
     * @param databaseName the database name.
     */
    public DatabaseClient(ServerAddress serverAddr, String databaseName) {
        this.init(serverAddr, databaseName, null, null);
    }

    private void init(String host, int port, String databaseName, String userId, String password) {
        this.init(ServerAddressHelper.createServerAddress(host, port), databaseName, userId, password);
    }

    private void init(ServerAddress serverAddr, String databaseName, String userId, String password) {
        this.databaseName = databaseName;
        this.serverAddr = serverAddr;
        this.credential = null;

        if (userId != null && password != null) {
            this.credential = MongoCredential.createCredential(userId, AUTHENTICATION_SOURCE, password.toCharArray());
        }
    }

    /**
     * Retrieve all the necessary connection information from AWS SSM.
     * It retrieve the information from AWS SSM using the application name
     * provided during the creation of the {@code DatabaseClient}.
     *
     * <p>See {@link #DatabaseClient(String)}</p>
     */
    public void resolveServerAddr() {
        // The "appName" variable is only set when running on AWS.
        // This method do nothing when running with unit tests
        if (this.appName != null) {
            // Either "testing" or "production".
            // This variable is set in an Environment variable (in the Docker container) by Jenkins, at compile time.
            String variableStorePrefix = System.getenv(VARIABLE_STORE_PREFIX_ENVIRONMENT_VARIABLE);
            if (variableStorePrefix == null || variableStorePrefix.isEmpty()) {
                throw new AssertionError(String.format("Environment variable %s is missing.", VARIABLE_STORE_PREFIX_ENVIRONMENT_VARIABLE));
            }
            LOGGER.info(String.format("Using Variable Store Prefix: %s", variableStorePrefix));

            String globalPath = String.format("/%s/global/mongodb/", variableStorePrefix);
            String appPath = String.format("/%s/%s/mongodb/", variableStorePrefix, this.appName);

            Map<String, String> globalParameterMap = new HashMap<String, String>();
            Map<String, String> appParameterMap = new HashMap<String, String>();
            AWSSimpleSystemsManagement ssmClient = null;
            try {
                ssmClient = AWSSimpleSystemsManagementClientBuilder.defaultClient();

                GetParametersByPathResult globalParameters = ssmClient.getParametersByPath(new GetParametersByPathRequest()
                        .withPath(globalPath));
                globalParameters.getParameters().forEach(parameter -> {
                    globalParameterMap.put(parameter.getName(), parameter.getValue());
                });

                GetParametersByPathResult appParameters = ssmClient.getParametersByPath(new GetParametersByPathRequest()
                        .withPath(appPath));
                appParameters.getParameters().forEach(parameter -> {
                    appParameterMap.put(parameter.getName(), parameter.getValue());
                });
            } finally {
                if (ssmClient != null) {
                    ssmClient.shutdown();
                }
            }

            boolean valid = true;

            String host = globalParameterMap.get(globalPath + "host");
            if (host == null) {
                LOGGER.error(String.format("Global parameter %s is null", globalPath + "host"));
                valid = false;
            }

            String portStr = globalParameterMap.get(globalPath + "port");
            if (portStr == null) {
                LOGGER.error(String.format("Global parameter %s is null", globalPath + "port"));
                valid = false;
            }

            String databaseName = globalParameterMap.get(globalPath + "db");
            if (databaseName == null) {
                LOGGER.error(String.format("Global parameter %s is null", globalPath + "db"));
                valid = false;
            }


            String userId = appParameterMap.get(appPath + "userid");
            if (userId == null) {
                LOGGER.error(String.format("Application parameter %s is null", appPath + "userid"));
                valid = false;
            }

            String password = appParameterMap.get(appPath + "password");
            if (password == null) {
                LOGGER.error(String.format("Application parameter %s is null", appPath + "password"));
                valid = false;
            }

            int port = -1;
            if (portStr != null) {
                try {
                    port = Integer.parseInt(portStr);
                } catch(Exception ex) {
                    LOGGER.error(String.format("Invalid port number %s", portStr), ex);
                    valid = false;
                }
            }

            if (!valid) {
                throw new IllegalStateException("Invalid store parameters");
            }

            this.init(host, port, databaseName, userId, password);
        }
    }

    /**
     * Returns the number of times the {@code DatabaseClient}
     * attempt to reconnect to the database server before giving up.
     * This is used to define the incremental wait
     * used before attempting to reconnect. By default, it is
     * large enough to wait long enough for the server to shutdown,
     * get recreated by the AWS CloudFormation system and restore
     * it's backup.
     *
     * <p>Default: {@code 15}</p>
     *
     * @return the number of times the {@code DatabaseClient}
     *     attempt to reconnect to the database.
     */
    public int getDbRetryAttempts() {
        return this.dbRetryAttempts;
    }

    /**
     * Set the number of times the {@code DatabaseClient}
     * attempt to reconnect to the database server before giving up.
     *
     * <p>The value was carefully chosen to wait just long
     * enough for the server to recover from a destruction
     * and a rebuilt.</p>
     *
     * <p>See {@link #getDbRetryAttempts()}</p>
     *
     * @param dbRetryAttempts
     */
    public void setDbRetryAttempts(int dbRetryAttempts) {
        this.dbRetryAttempts = Math.max(1, dbRetryAttempts);
    }

    /**
     * @deprecated Use getDbInitialDelayBetweenAttempt()
     */
    @Deprecated
    public int getDbDelayBetweenAttempt() {
        return this.getDbInitialDelayBetweenAttempt();
    }

    /**
     * @deprecated Use setDbInitialDelayBetweenAttempt(int dbDelayBetweenAttempt)
     */
    @Deprecated
    public void setDbDelayBetweenAttempt(int dbInitialDelayBetweenAttempt) {
        this.setDbInitialDelayBetweenAttempt(dbInitialDelayBetweenAttempt);
    }

    /**
     * Returns the number of seconds to wait after the first failed
     * connection attempt. The delay is increase for each failed attempt.
     *
     * <p>The wait delay is calculated using the following formula:
     * {@code delay = dbInitialDelayBetweenAttempt * number of failed attempt}</p>
     *
     * <p>The value was carefully chosen to wait just long
     * enough for the server to recover from a destruction
     * and a rebuilt.</p>
     *
     * <p>Default: {@code 10}</p>
     *
     * @return the number of seconds to wait after the first failed connection attempt.
     */
    public int getDbInitialDelayBetweenAttempt() {
        return this.dbInitialDelayBetweenAttempt;
    }

    /**
     * Set the number of seconds to wait after the first failed
     * connection attempt.
     *
     * <p>See: {@code #getDbInitialDelayBetweenAttempt}</p>
     *
     * @param dbInitialDelayBetweenAttempt the number of seconds to wait
     *     after the first failed connection attempt.
     */
    public void setDbInitialDelayBetweenAttempt(int dbInitialDelayBetweenAttempt) {
        this.dbInitialDelayBetweenAttempt = Math.max(1, dbInitialDelayBetweenAttempt);
    }

    /**
     * Returns the {@code MongoClient}.
     *
     * <p>Used internally. Use a {@link au.gov.aims.ereefs.database.table.DatabaseTable}
     * object where possible, to take advantage of the caching and automatic reconnection.</p>
     *
     * @return the {@code MongoClient}.
     */
    public MongoClient getMongoClient() {
        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(this.serverAddr)));

        if (this.credential != null) {
            mongoClientSettingsBuilder = mongoClientSettingsBuilder.credential(this.credential);
        }

        return MongoClients.create(mongoClientSettingsBuilder.build());
    }

    /**
     * Returns the {@code MongoDatabase}.
     *
     * <p>Used internally. Use a {@link au.gov.aims.ereefs.database.table.DatabaseTable}
     * object where possible, to take advantage of the caching and automatic reconnection.</p>
     *
     * @return the {@code MongoDatabase}.
     */
    public MongoDatabase getMongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(this.databaseName);
    }

    // Collection = table

    /**
     * Create a table, aka {@code collection}, in the {@code MongoDB} database.
     *
     * <p>Used with unit tests.</p>
     *
     * <p>NOTE: Database tables are created using a cloud formation template.
     * There should be no need to use this method for any other reason than creating
     * a test environment.</p>
     *
     * @param tableName name of the database table.
     * @param indexes fields to index.
     * @throws Exception if something goes wrong.
     */
    public void createTable(String tableName, String ... indexes) throws Exception {
        boolean success = false;
        for (int attempt=1; attempt<=this.getDbRetryAttempts() && !success; attempt++) {
            try (MongoClient mongoClient = this.getMongoClient()) {
                MongoDatabase database = this.getMongoDatabase(mongoClient);
                database.createCollection(tableName);
                if (indexes != null && indexes.length > 0) {
                    MongoCollection<Document> table = database.getCollection(tableName, Document.class);
                    table.createIndex(Indexes.ascending(indexes));
                }
                success = true;
            } catch(Exception ex) {
                int delay = this.getDbInitialDelayBetweenAttempt() * attempt;
                LOGGER.warn(String.format("Database error [attempt #%d] on table %s. Try again in %d seconds.",
                        attempt, tableName, delay), ex);

                if (attempt == this.getDbRetryAttempts()) {
                    LOGGER.error("Maximum number of attempt reached.");
                    throw new Exception("Maximum number of attempt reached.", ex);
                }
                Thread.sleep(delay * 1000);
                this.resolveServerAddr();
            }
        }
    }

    /**
     * Creates an abject representing a database table, aka {@code collection}.
     *
     * <p>The database table object can be used to select, insert, update
     * and delete documents from the database.</p>
     *
     * @param tableName the name of the database table.
     * @param cacheStrategy the caching strategy.
     * @param primaryKeyName the attribute name of the primary key. Usually {@code _id}.
     * @return a database table object.
     */
    public DatabaseSingleKeyTable getTable(String tableName, CacheStrategy cacheStrategy, String primaryKeyName) {
        return new DatabaseSingleKeyTable(this, tableName, cacheStrategy, primaryKeyName);
    }

    /**
     * Creates an abject representing a database table with a composite primary key.
     *
     * @param tableName the name of the database table.
     * @param cacheStrategy the caching strategy.
     * @param primaryKeyName the attribute name of the primary key. Usually {@code _id}.
     * @param compositeKeyNames the attribute names of the composite primary key.
     * @return a database table object.
     */
    public DatabaseCompositeKeyTable getTable(String tableName, CacheStrategy cacheStrategy, String primaryKeyName, String ... compositeKeyNames) {
        return new DatabaseCompositeKeyTable(this, tableName, cacheStrategy, primaryKeyName, compositeKeyNames);
    }
}
