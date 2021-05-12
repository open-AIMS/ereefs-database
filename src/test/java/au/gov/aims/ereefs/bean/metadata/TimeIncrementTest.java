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
