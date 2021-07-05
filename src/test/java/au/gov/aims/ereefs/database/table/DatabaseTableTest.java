/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.database.table;

import au.gov.aims.ereefs.database.CacheStrategy;
import au.gov.aims.ereefs.database.DatabaseClient;
import au.gov.aims.ereefs.database.DatabaseTestBase;
import au.gov.aims.ereefs.database.table.key.CompositePrimaryKey;
import au.gov.aims.ereefs.database.table.key.PrimaryKey;
import au.gov.aims.ereefs.database.table.key.SinglePrimaryKey;
import au.gov.aims.json.JSONUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DatabaseTableTest extends DatabaseTestBase {
    private static final Logger LOGGER = Logger.getLogger(DatabaseTableTest.class);
    private static final String TABLE_NAME = "STAFF";

    /**
     * Test basic operations on the Database
     */
    @Test
    public void testInsertSelectUpdate() throws Exception {
        DatabaseClient databaseClient = this.getDatabaseClient();

        databaseClient.createTable(TABLE_NAME);
        DatabaseTable table = databaseClient.getTable(TABLE_NAME, CacheStrategy.NONE, "_id");

        JSONObject jsonStaff1 = new JSONObject()
            .put("_id", 1)
            .put("givenname", "Gael")
            .put("surname", "Lafond")
            .put("basket", new JSONArray()
                .put("Banana")
                .put("Orange")
                .put("Chocolate"));

        table.insert(jsonStaff1);
        PrimaryKey primaryKeyStaff1 = table.getPrimaryKey(jsonStaff1);
        Assert.assertNotNull("Primary key is null", primaryKeyStaff1);
        SinglePrimaryKey singlePrimaryKeyStaff1 = (SinglePrimaryKey)primaryKeyStaff1;
        Assert.assertEquals("Wrong primary key name", "_id", singlePrimaryKeyStaff1.getKeyName());
        Assert.assertEquals("Wrong primary key value", 1, singlePrimaryKeyStaff1.getKeyValue());


        table.insert(new JSONObject()
            .put("_id", 2)
            .put("givenname", "Marc")
            .put("surname", "Hammerton")
            .put("basket", new JSONArray()
                .put("Apple")
                .put("Chicken")));

        table.insert(new JSONObject()
            .put("_id", 3)
            .put("givenname", "Aaron")
            .put("surname", "Smith")
            .put("basket", new JSONArray()
                .put("Milk")
                .put("RedBull")));

        table.insert(new JSONObject()
            .put("_id", 4)
            .put("givenname", "Eric")
            .put("surname", "Lawrey")
            .put("basket", new JSONArray()
                .put("Encyclopedia")
                .put("Reef finder")
                .put("Corals of the world")));


        // Test select
        JSONObject selected = table.select(primaryKeyStaff1);
        Assert.assertNotNull("Selected record is null", selected);

        Assert.assertTrue(String.format("Invalid selected record. Expected:%n%s%nFound:%n%s%n", jsonStaff1.toString(4), selected.toString(4)),
            JSONUtils.equals(jsonStaff1, selected));

        selected
            .put("givenname", "Gael2")
            .put("basket", new JSONArray()
                .put("Chips")
                .put("Candies"));

        table.update(selected, primaryKeyStaff1);


        // Test re-select after an update
        JSONObject reSelected = table.select(primaryKeyStaff1);
        Assert.assertNotNull("Re-selected record is null", reSelected);

        Assert.assertTrue(String.format("Invalid re-selected record. Expected:%n%s%nFound:%n%s%n", selected.toString(4), reSelected.toString(4)),
            JSONUtils.equals(selected, reSelected));


        int counter = 0;
        JSONObjectIterable items = table.selectAll();
        Assert.assertNotNull("Selected items is null", items);
        for (JSONObject item : items) {
            counter++;
            LOGGER.debug(item.toString(4));
        }

        Assert.assertEquals("Wrong number of selected items", 4, counter);
    }


    /**
     * Test basic operations on the Database using a composite key
     */
    @Test
    public void testCompositeKeyObject() throws Exception {
        DatabaseClient databaseClient = this.getDatabaseClient();

        databaseClient.createTable(TABLE_NAME);
        DatabaseTable table = databaseClient.getTable(TABLE_NAME, CacheStrategy.NONE, "_id", "id", "type");

        JSONObject jsonStaff1 = new JSONObject()
            .put("_id", new JSONObject()
                .put("id", 1)
                .put("type", "programmer")
            )
            .put("givenname", "Gael")
            .put("surname", "Lafond")
            .put("basket", new JSONArray()
                .put("Banana")
                .put("Orange")
                .put("Chocolate"));

        table.insert(jsonStaff1);
        PrimaryKey primaryKeyStaff1 = table.getPrimaryKey(jsonStaff1);
        Assert.assertNotNull("Primary key is null", primaryKeyStaff1);
        CompositePrimaryKey compositePrimaryKeyStaff1 = (CompositePrimaryKey)primaryKeyStaff1;
        Map<String, Object> keyValues = compositePrimaryKeyStaff1.getKeyValues();
        Assert.assertEquals("Wrong primary key id value", 1, keyValues.get("id"));
        Assert.assertEquals("Wrong primary key type value", "programmer", keyValues.get("type"));


        table.insert(new JSONObject()
            .put("_id", new JSONObject()
                .put("id", 2)
                .put("type", "programmer")
            )
            .put("givenname", "Marc")
            .put("surname", "Hammerton")
            .put("basket", new JSONArray()
                .put("Apple")
                .put("Chicken")));

        table.insert(new JSONObject()
            .put("_id", new JSONObject()
                .put("id", 3)
                .put("type", "programmer")
            )
            .put("givenname", "Aaron")
            .put("surname", "Smith")
            .put("basket", new JSONArray()
                .put("Milk")
                .put("RedBull")));

        table.insert(new JSONObject()
            .put("_id", new JSONObject()
                .put("id", 1)
                .put("type", "supervisor")
            )
            .put("givenname", "Eric")
            .put("surname", "Lawrey")
            .put("basket", new JSONArray()
                .put("Encyclopedia")
                .put("Reef finder")
                .put("Corals of the world")));


        // Test select
        JSONObject selected = table.select(primaryKeyStaff1);
        Assert.assertNotNull("Selected record is null", selected);

        Assert.assertTrue(String.format("Invalid selected record. Expected:%n%s%nFound:%n%s%n", jsonStaff1.toString(4), selected.toString(4)),
            JSONUtils.equals(jsonStaff1, selected));

        selected
            .put("givenname", "Gael2")
            .put("basket", new JSONArray()
                .put("Chips")
                .put("Candies"));

        table.update(selected, primaryKeyStaff1);


        // Test re-select after an update
        JSONObject reSelected = table.select(primaryKeyStaff1);
        Assert.assertNotNull("Re-selected record is null", reSelected);

        Assert.assertTrue(String.format("Invalid re-selected record. Expected:%n%s%nFound:%n%s%n", selected.toString(4), reSelected.toString(4)),
            JSONUtils.equals(selected, reSelected));


        int counter = 0;
        JSONObjectIterable items = table.selectAll();
        Assert.assertNotNull("Selected items is null", items);
        for (JSONObject item : items) {
            counter++;
            LOGGER.debug(item.toString(4));
        }

        Assert.assertEquals("Wrong number of selected items", 4, counter);
    }
}
