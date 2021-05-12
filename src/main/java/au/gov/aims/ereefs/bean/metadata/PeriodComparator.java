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

import java.util.Comparator;

/**
 * Comparator used to order Joda time Period objects.
 * It's approximate but precise enough in the context of eReefs.
 * It makes the following assumptions, for simplicity:
 * <ul>
 *   <li>A month is 30 days</li>
 *   <li>A year is 365 days</li>
 * </ul>
 *
 * For example, a period of 12 months (12 x 30 = 360 days) is
 * considered smaller than a period of 1 year (365 days).
 */
public class PeriodComparator implements Comparator<Period> {
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    private static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     *
     * <p>Note: this comparator
     * imposes orderings that are inconsistent with equals.</p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     * @throws NullPointerException if an argument is null and this
     *         comparator does not permit null arguments
     * @throws ClassCastException if the arguments' types prevent them from
     *         being compared by this comparator.
     */
    @Override
    public int compare(Period o1, Period o2) {
        if (o1 == o2) {
            return 0;
        }

        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }

        // NOTE: We can't return the result of the subtraction
        //   since the interface expect a int, not a long.
        long cmp = periodToApproximateMillis(o1) - periodToApproximateMillis(o2);
        return cmp == 0 ? 0 : (cmp > 0 ? 1 : -1);
    }

    private long periodToApproximateMillis(Period period) {
        int nbMonths = period.getMonths() % 12;
        int nbYears = period.getMonths() / 12 + period.getYears();

        return period.getMillis() +
            period.getSeconds() * MILLIS_PER_SECOND +
            period.getMinutes() * MILLIS_PER_MINUTE +
            period.getHours() * MILLIS_PER_HOUR +
            period.getDays() * MILLIS_PER_DAY +
            period.getWeeks() * 7 * MILLIS_PER_DAY +
            nbMonths * 30 * MILLIS_PER_DAY +  // Approximately
            nbYears * 365 * MILLIS_PER_DAY;   // Approximately
    }
}
