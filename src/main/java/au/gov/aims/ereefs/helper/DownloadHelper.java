/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.helper;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.manager.DownloadManager;
import au.gov.aims.ereefs.bean.download.DownloadBean;
import au.gov.aims.ereefs.database.table.JSONObjectIterable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

/**
 * Helper class used to simplify interaction with the database.
 *
 * <p>This class relates to the {@link DownloadBean},
 * used with the {@code ereefs-download-manager} project.</p>
 */
public class DownloadHelper {
    private DatabaseClient dbClient;
    private DownloadManager downloadManager;

    /**
     * @deprecated Use {@link DownloadHelper#(DatabaseClient, CacheStrategy)}
     * @param dbClient the {@link DatabaseClient} used to query the database.
     */
    @Deprecated
    public DownloadHelper(DatabaseClient dbClient) {
        this(dbClient, CacheStrategy.NONE);
    }

    /**
     * Creates a {@code DownloadHelper} using a database client and a cache strategy.
     *
     * @param dbClient the {@link DatabaseClient} used to query the database.
     * @param cacheStrategy the database cache strategy.
     */
    public DownloadHelper(DatabaseClient dbClient, CacheStrategy cacheStrategy) {
        this.dbClient = dbClient;
        this.downloadManager = new DownloadManager(this.dbClient, cacheStrategy);
    }

    /**
     * Set the {@link DownloadManager} cache strategy.
     * @param cacheStrategy the new cache strategy.
     */
    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.downloadManager.setCacheStrategy(cacheStrategy);
    }

    /**
     * Clear the {@link DownloadManager} cache.
     * @throws IOException if something goes wrong while clearing the disk cache.
     */
    public void clearCache() throws IOException {
        this.downloadManager.clearCache();
    }

    /**
     * Returns the {@link DatabaseClient} that was set in the constructor.
     * @return the {@link DatabaseClient}.
     */
    public DatabaseClient getDbClient() {
        return this.dbClient;
    }

    /**
     * Returns an {@code Iterable} list of all {@link DownloadBean}
     * found in the database.
     *
     * @return an {@code Iterable} list of all {@link DownloadBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<DownloadBean> getDownloads() throws Exception {
        JSONObjectIterable jsonDownloads = this.downloadManager.selectAll();
        return this.getIterable(jsonDownloads.iterator());
    }

    /**
     * Returns an {@code Iterable} list of all enabled {@link DownloadBean}
     * found in the database.
     *
     * @return an {@code Iterable} list of all enabled {@link DownloadBean}.
     * @throws Exception if the database is unreachable.
     */
    public Iterable<DownloadBean> getEnabledDownloads() throws Exception {
        JSONObjectIterable jsonDownloads = this.downloadManager.selectAllEnabled();
        return this.getIterable(jsonDownloads.iterator());
    }

    private Iterable<DownloadBean> getIterable(Iterator<JSONObject> jsonDownloadIterator) {
        return new Iterable<DownloadBean>() {
            @Override
            public Iterator<DownloadBean> iterator() {
                return new Iterator<DownloadBean>() {
                    @Override
                    public boolean hasNext() {
                        return jsonDownloadIterator.hasNext();
                    }

                    @Override
                    public DownloadBean next() {
                        JSONObject jsonConfig = jsonDownloadIterator.next();
                        if (jsonConfig == null) {
                            return null;
                        }
                        return new DownloadBean(jsonConfig);
                    }
                };
            }
        };
    }
}
