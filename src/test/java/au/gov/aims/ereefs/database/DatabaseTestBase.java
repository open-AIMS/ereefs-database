/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database;

import au.gov.aims.ereefs.helper.NcAnimateConfigHelper;
import au.gov.aims.ereefs.helper.TestHelper;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.After;
import org.junit.Before;

import java.net.InetSocketAddress;

public class DatabaseTestBase {
    // DB Cache strategy used for tests
    public static final CacheStrategy CACHE_STRATEGY = CacheStrategy.MEMORY;
    private static final String DATABASE_NAME = "testdb";

    private MongoServer server;
    private DatabaseClient databaseClient;

    public DatabaseClient getDatabaseClient() {
        return this.databaseClient;
    }

    @Before
    public void init() throws Exception {
        this.server = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = this.server.bind();

        this.databaseClient = new DatabaseClient(new ServerAddress(serverAddress), DATABASE_NAME);
        this.createTables();
    }

    @After
    public void shutdown() {
        NcAnimateConfigHelper.clearMetadataCache();
        if (this.server != null) {
            this.server.shutdown();
        }
    }

    private void createTables() throws Exception {
        TestHelper.createTables(this.databaseClient);
    }
}
