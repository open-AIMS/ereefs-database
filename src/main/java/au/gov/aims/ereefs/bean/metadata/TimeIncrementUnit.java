/*
 * Copyright (c) Australian Institute of Marine Science, 2021.
 * @author Gael Lafond <g.lafond@aims.gov.au>
 */
package au.gov.aims.ereefs.bean.metadata;

import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * Object representing a time increment unit.
 * It's part of the {@link TimeIncrement} object.
 */
public enum TimeIncrementUnit {
    ETERNITY (null),
    YEAR     (PeriodType.years()),
    MONTH    (PeriodType.months()),
    WEEK     (PeriodType.weeks()),
    DAY      (PeriodType.days()),
    HOUR     (PeriodType.hours()),
    MINUTE   (PeriodType.minutes()),
    SECOND   (PeriodType.seconds());

    private PeriodType periodType;

    private TimeIncrementUnit(PeriodType periodType) {
        this.periodType = periodType;
    }

    /**
     * Null safe and case safe wrapper around {@link #valueOf(String)} method.
     *
     * @param timeIncrementUnitStr String representation of the unit.
     * @return the corresponding {@code TimeIncrementUnit}, or null if the
     *   {@code timeIncrementUnitStr} parameter is null or empty.
     */
    public static TimeIncrementUnit parse(String timeIncrementUnitStr) {
        if (timeIncrementUnitStr == null || timeIncrementUnitStr.isEmpty()) {
            return null;
        }

        return TimeIncrementUnit.valueOf(timeIncrementUnitStr.toUpperCase());
    }

    /**
     * Returns the Joda time PeriodType.
     * @return the Joda time PeriodType.
     */
    public PeriodType getPeriodType() {
        return this.periodType;
    }

    /**
     * Returns a Joda time Period representing a {@link TimeIncrement}.
     *
     * @param count The number of time increment unit.
     * @return a Joda time Period representing a TimeIncrement.
     *   Returns null for time increment unit {@code ETERNITY}.
     */
    public Period getPeriod(int count) {
        switch(this) {
            case SECOND:
                return Period.seconds(count);
            case MINUTE:
                return Period.minutes(count);
            case HOUR:
                return Period.hours(count);
            case DAY:
                return Period.days(count);
            case WEEK:
                return Period.weeks(count);
            case MONTH:
                return Period.months(count);
            case YEAR:
                return Period.years(count);
        }

        // Eternity returns null
        return null;
    }
}
