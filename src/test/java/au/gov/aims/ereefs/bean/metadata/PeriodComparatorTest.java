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

import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

public class PeriodComparatorTest {

    @Test
    public void testCompareTo() {
        PeriodComparator periodComparator = new PeriodComparator();

        Period _1Hour = Period.hours(1);
        Period _24Hours = Period.hours(24);
        Period _100Hours = Period.hours(100);

        Period _1Day = Period.days(1);

        Period _1Month = Period.months(1);
        Period _11Months = Period.months(11);
        Period _12Months = Period.months(12);
        Period _13Months = Period.months(13);

        Period _1Year = Period.years(1);

        Period eternity = null;

        int cmp;

        cmp = periodComparator.compare(_1Hour, _1Day);
        Assert.assertTrue(String.format("1 hour must be smaller than 1 day: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Day, _1Month);
        Assert.assertTrue(String.format("1 day must be smaller than 1 month: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Month, _1Year);
        Assert.assertTrue(String.format("1 month must be smaller than 1 year: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Month, _1Year);
        Assert.assertTrue(String.format("1 month must be smaller than 1 year: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Year, eternity);
        Assert.assertTrue(String.format("1 year must be smaller than 1 eternity: %d", cmp), cmp < 0);


        // Test transitivity: if a > b and b > c then a > c

        cmp = periodComparator.compare(_1Hour, _1Year);
        Assert.assertTrue(String.format("1 hour must be smaller than 1 year: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Hour, eternity);
        Assert.assertTrue(String.format("1 hour must be smaller than 1 eternity: %d", cmp), cmp < 0);


        // Test symmetry: if a > b then b < a

        cmp = periodComparator.compare(_1Year, _1Hour);
        Assert.assertTrue(String.format("1 year must be bigger than 1 hour: %d", cmp), cmp > 0);

        cmp = periodComparator.compare(eternity, _1Hour);
        Assert.assertTrue(String.format("1 eternity must be bigger than 1 hour: %d", cmp), cmp > 0);


        // Test months relative to a year

        cmp = periodComparator.compare(_11Months, _1Year);
        Assert.assertTrue(String.format("11 months must be smaller than 1 year: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_12Months, _1Year);
        Assert.assertEquals(String.format("12 months must be equals to 1 year: %s", cmp), cmp, 0);

        cmp = periodComparator.compare(_13Months, _1Year);
        Assert.assertTrue(String.format("13 months must be bigger than 1 year: %d", cmp), cmp > 0);


        // Other tests

        cmp = periodComparator.compare(_1Day, _100Hours);
        Assert.assertTrue(String.format("1 day must be smaller than 100 hours: %d", cmp), cmp < 0);

        cmp = periodComparator.compare(_1Day, _24Hours);
        Assert.assertEquals(String.format("1 day must be equals to 24 hours: %s", cmp), cmp, 0);

        // Check symmetry again
        cmp = periodComparator.compare(_24Hours, _1Day);
        Assert.assertEquals(String.format("24 hours must be equals to 1 day: %s", cmp), cmp, 0);
    }
}
