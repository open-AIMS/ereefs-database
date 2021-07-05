/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class TimeIncrementTest {

    @Test
    public void testCompareTo() {
        TimeIncrement hourly = new TimeIncrement(1, TimeIncrementUnit.HOUR);
        TimeIncrement daily = new TimeIncrement(1, TimeIncrementUnit.DAY);
        TimeIncrement monthly = new TimeIncrement(1, TimeIncrementUnit.MONTH);
        TimeIncrement yearly = new TimeIncrement(1, TimeIncrementUnit.YEAR);
        TimeIncrement all = new TimeIncrement(1, TimeIncrementUnit.ETERNITY);

        Assert.assertTrue("TimeIncrement comparator to is not working as expected", hourly.compareTo(yearly) < 0);
        Assert.assertTrue("TimeIncrement comparator to is not working as expected", all.compareTo(daily) > 0);
        Assert.assertTrue("TimeIncrement comparator to is not working as expected", monthly.compareTo(monthly) == 0);
    }

    @Test
    public void testOrder() {
        TimeIncrement hourly = new TimeIncrement(1, TimeIncrementUnit.HOUR);
        TimeIncrement daily = new TimeIncrement(1, TimeIncrementUnit.DAY);
        TimeIncrement monthly = new TimeIncrement(1, TimeIncrementUnit.MONTH);
        TimeIncrement yearly = new TimeIncrement(1, TimeIncrementUnit.YEAR);
        TimeIncrement all = new TimeIncrement(1, TimeIncrementUnit.ETERNITY);

        List<TimeIncrement> expectedOrder = new ArrayList<TimeIncrement>();
        expectedOrder.add(hourly);
        expectedOrder.add(daily);
        expectedOrder.add(monthly);
        expectedOrder.add(yearly);
        expectedOrder.add(all);

        SortedSet<TimeIncrement> orderedSet = new TreeSet<TimeIncrement>();

        // Add all time increments, in random order
        orderedSet.add(monthly);
        orderedSet.add(hourly);
        orderedSet.add(all);
        orderedSet.add(daily);
        orderedSet.add(yearly);
        orderedSet.add(hourly); // Duplicate, expected to be ignored

        List<TimeIncrement> orderedList = new ArrayList<TimeIncrement>(orderedSet);

        Assert.assertEquals("Wrong TimeIncrement order", expectedOrder, orderedList);
    }
}
