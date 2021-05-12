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

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DatabaseClientTest extends DatabaseTestBase {

    /**
     * Test DB connection to the in-memory Database server.
     */
    @Test
    public void testConnection() {
        DatabaseClient databaseClient = this.getDatabaseClient();

        Assert.assertNotNull("The database client is null", databaseClient);
    }


    /**
     * Documentation about the "_id" field:
     * <ul>
     *     <li>https://docs.mongodb.com/manual/core/document/#document-id-field</li>
     * </ul>
     *
     * <p>Example of arbitrary JSONObject used for _id:</p>
     * <ul>
     *     <li>https://stackoverflow.com/questions/3298963/how-to-set-a-primary-key-in-mongodb#answer-34950608</li>
     *     <li>https://github.com/Automattic/mongoose/issues/2276</li>
     * </ul>
     */
    @Test
    public void testCompositeKeyObject() {
        DatabaseClient databaseClient = this.getDatabaseClient();
        try (MongoClient mongoClient = databaseClient.getMongoClient()) {
            MongoDatabase database = databaseClient.getMongoDatabase(mongoClient);

            String tableName = "objectkey";

            database.createCollection(tableName);

            JSONObject json = new JSONObject()
                .put("_id", new JSONObject()
                    .put("key1", "v1")
                    .put("key2", "v2")
                )
                .put("key", "value");

            MongoCollection<Document> table = database.getCollection(tableName, Document.class);
            table.insertOne(Document.parse(json.toString()));
        }
    }

    @Test
    public void testCompositeKeyObjectMultipleEntry() {
        DatabaseClient databaseClient = this.getDatabaseClient();
        try (MongoClient mongoClient = databaseClient.getMongoClient()) {
            MongoDatabase database = databaseClient.getMongoDatabase(mongoClient);

            String tableName = "objectkey";

            database.createCollection(tableName);

            MongoCollection<Document> table = database.getCollection(tableName, Document.class);
            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 1)
                    .put("type", "data")
                )
                .put("key", "value1").toString()));

            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 2)
                    .put("type", "data")
                )
                .put("key", "value2").toString()));

            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 1)
                    .put("type", "metadata")
                )
                .put("key", "value3").toString()));
        }
    }

    /**
     * Throws:
     *     com.mongodb.MongoWriteException: E11000 duplicate key error collection: testdb.objectkey index: _id_ dup key: { : { id: 1, type: "data" } }
     */
    @Test(expected = MongoWriteException.class)
    public void testCompositeKeyObjectDuplicateKey() {
        DatabaseClient databaseClient = this.getDatabaseClient();
        try (MongoClient mongoClient = databaseClient.getMongoClient()) {
            MongoDatabase database = databaseClient.getMongoDatabase(mongoClient);

            String tableName = "objectkey";

            database.createCollection(tableName);

            MongoCollection<Document> table = database.getCollection(tableName, Document.class);
            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 1)
                    .put("type", "data")
                )
                .put("key", "value1").toString()));

            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 2)
                    .put("type", "data")
                )
                .put("key", "value2").toString()));

            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 1)
                    .put("type", "metadata")
                )
                .put("key", "value3").toString()));

            table.insertOne(Document.parse(new JSONObject()
                .put("_id", new JSONObject()
                    .put("id", 1)
                    .put("type", "data")
                )
                .put("key", "value4").toString()));
        }
    }

    /**
     * Test to validate that Array ID are not valid with MongoDB.
     * Throws:
     *     com.mongodb.MongoWriteException: can't use an array for _id
     *
     * Documentation about the "_id" field
     *     "The _id field may contain values of any BSON data type, other than an array."
     *     https://docs.mongodb.com/manual/core/document/#the-_id-field
     *     List of BSON data types:
     *         https://docs.mongodb.com/manual/reference/bson-types/
     *
     *     https://docs.mongodb.com/manual/core/document/#field-names
     *     "The field name _id is reserved for use as a primary key; its value must be unique in the collection, is immutable, and may be of any type other than an array."
     */
    @Test(expected = MongoWriteException.class)
    public void testCompositeKeyArray() {
        DatabaseClient databaseClient = this.getDatabaseClient();
        try (MongoClient mongoClient = databaseClient.getMongoClient()) {
            MongoDatabase database = databaseClient.getMongoDatabase(mongoClient);

            String tableName = "arraykey";

            database.createCollection(tableName);

            JSONObject json = new JSONObject()
                .put("_id", new JSONArray()
                    .put("key1")
                    .put("key2")
                )
                .put("key", "value");

            MongoCollection<Document> table = database.getCollection(tableName, Document.class);
            table.insertOne(Document.parse(json.toString()));
        }
    }
}




























