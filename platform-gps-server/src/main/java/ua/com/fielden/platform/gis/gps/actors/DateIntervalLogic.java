package ua.com.fielden.platform.gis.gps.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Represents a set of utilities to work with date interval continuum.
 * 
 * IMPORTANT: please note, that ALL interval logic is based on the fact that interval is OPENED '[' at start and CLOSED ')' at the end!
 * 
 * @author TG Team
 */
public class DateIntervalLogic {
    /** Convenient constant for INCLUSIVE left infinity in time continuum. */
    public static final DateTime INFINITY_LEFT = new DateTime(1900, 1, 1, 0, 0);
    /** Convenient constant for EXCLUSIVE right infinity in time continuum. */
    public static final DateTime INFINITY_RIGHT = new DateTime(2100, 1, 1, 0, 0);

    /**
     * Convenient factory method to create non-empty standard [closedInfinity; open) interval value.
     * 
     * @param closedRight
     * @param value
     * @return
     */
    public static <T> IntervalValue<T> closedInfinity_to_open(final DateTime openRight, final T value) {
        return closed_to_open(INFINITY_LEFT, openRight, value);
    }

    /**
     * Convenient factory method to create non-empty standard [closed; openInfinity) interval value.
     * 
     * @param openedLeft
     * @param value
     * @return
     */
    public static <T> IntervalValue<T> closed_to_openInfinity(final DateTime closedLeft, final T value) {
        return closed_to_open(closedLeft, INFINITY_RIGHT, value);
    }

    /**
     * Convenient factory method to create non-empty standard [closed; open) interval value.
     * 
     * @param openedLeft
     * @param closedRight
     * @param value
     * @return
     */
    public static <T> IntervalValue<T> closed_to_open(final DateTime closedLeft, final DateTime openRight, final T value) {
        if (closedLeft == null) {
            throw new NullPointerException("ClosedLeft is null during interval creation.");
        }
        if (openRight == null) {
            throw new NullPointerException("OpenRight is null during interval creation.");
        }
        if (closedLeft.isAfter(openRight)) {
            throw new IllegalArgumentException("ClosedLeft [" + closedLeft + "] should not be after OpenRight [" + openRight
                    + "] during interval creation to make non-empty interval.");
        }
        return new IntervalValue<T>(new Interval(closedLeft, openRight), value);
    }

    /**
     * Convenient factory method to create non-empty (open; open) interval value.
     * 
     * @param closedLeft
     * @param closedRight
     * @param value
     * @return
     */
    //    public static <T> IntervalValue<T> open_to_open(final DateTime openLeft, final DateTime openRight, final T value) {
    //	if (openLeft == null) {
    //	    throw new NullPointerException("OpenLeft is null during interval creation.");
    //	}
    //	return closed_to_open(openLeft, openRight, value);
    //    }

    //    /**
    //     * Convenient factory method to create non-empty [closed; closed] interval value.
    //     *
    //     * @param openedLeft
    //     * @param openedRight
    //     * @param value
    //     * @return
    //     */
    //    public static <T> IntervalValue<T> closed_to_closed(final DateTime closedLeft, final DateTime closedRight, final T value) {
    ////	if (closedRight == null) {
    ////	    throw new NullPointerException("ClosedRight is null during interval creation.");
    ////	}
    ////	return closed_to_open(closedLeft, closedRight, value);
    //
    //	if (closedLeft == null) {
    //	    throw new NullPointerException("ClosedLeft is null during interval creation.");
    //	}
    //	if (closedRight == null) {
    //	    throw new NullPointerException("OpenRight is null during interval creation.");
    //	}
    //	if (closedLeft.isAfter(closedRight)) {
    //	    throw new IllegalArgumentException("ClosedLeft [" + closedLeft + "] should be before or equal to ClosedRight [" + closedRight + "] during interval creation to make non-empty interval.");
    //	}
    //	return new IntervalValue<T>(new Interval(closedLeft, closedRight), value);
    //    }

    /**
     * A simple [) date interval with some 'tag' value assigned.
     * 
     * @author TG Team
     * 
     * @param <T>
     *            -- the type of 'tag' value
     */
    public static class IntervalValue<T> implements Comparable<IntervalValue<T>> {
        private final Interval interval;
        private final transient T value;

        public IntervalValue(final Interval interval, final T value) {
            if (interval == null) {
                throw new NullPointerException("Interval cannot be null inside IntervalValue.");
            }
            this.interval = interval;
            this.value = value;
        }

        public Interval getInterval() {
            return interval;
        }

        public DateTime getStart() {
            return interval.getStart();
        }

        public DateTime getEnd() {
            return interval.getEnd();
        }

        public T getValue() {
            return value;
        }

        public int compareTo(final IntervalValue<T> o) {
            return this.getInterval().getStart().compareTo(o.getInterval().getStart());
        };

        @Override
        public String toString() {
            return "IntervalValue [interval=" + interval + ", value=" + value + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((interval == null) ? 0 : interval.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final IntervalValue other = (IntervalValue) obj;
            if (interval == null) {
                if (other.interval != null)
                    return false;
            } else if (!interval.equals(other.interval))
                return false;
            return true;
        }

        public boolean isBefore(final IntervalValue<T> that) {
            return this.interval.isBefore(that.interval);
        }

        public boolean overlaps(final IntervalValue<T> that) {
            return this.interval.overlaps(that.interval);
        }

        public IntervalValue<T> overlap(final IntervalValue<T> that) {
            final Interval overlap = this.interval.overlap(that.interval);
            return overlap == null ? null : new IntervalValue<T>(overlap, this.value);
        }
    }

    public static <T> List<IntervalValue<T>> removeIntervals(final IntervalValue<T> interval, final List<IntervalValue<T>> intervalsToRemove) {
        return removeIntervals(Arrays.asList(interval), intervalsToRemove);
    }

    protected static <T> List<IntervalValue<T>> removeIntervals(final List<IntervalValue<T>> intervals, final List<IntervalValue<T>> intervalsToRemove) {
        if (intervalsToRemove.isEmpty()) {
            return intervals;
        } else {
            final IntervalValue<T> intervalToRemove = intervalsToRemove.get(0);
            final List<IntervalValue<T>> removedFirst = removeIntervals(intervals, intervalToRemove);

            final List<IntervalValue<T>> newIntervalsToRemove = new ArrayList<>(intervalsToRemove);
            newIntervalsToRemove.remove(0);

            return removeIntervals(removedFirst, newIntervalsToRemove);
        }
    }

    public static <T> List<IntervalValue<T>> removeIntervals(final List<IntervalValue<T>> intervals, final IntervalValue<T> intervalToRemove) {
        final List<IntervalValue<T>> result = new ArrayList<>();
        if (intervals.isEmpty()) {
            return result;
        } else {
            for (final IntervalValue<T> i : intervals) {
                result.addAll(remove(i, intervalToRemove));
            }
            return result;
        }
    }

    /**
     * Finds an interval which fully contains the specified 'intervalValue'. Returns 'null' if there is no such interval.
     * 
     * @param intervalValue
     * @param establishedIntervals
     * @return
     */
    protected static <T> IntervalValue<T> findContaining(final IntervalValue<T> intervalValue, final List<IntervalValue<T>> establishedIntervals) {
        final IntervalValue<T> found = findContaining(intervalValue.getStart(), establishedIntervals);
        if (found != null) {
            return found.getInterval().contains(intervalValue.getInterval()) ? found : null;
        } else {
            return null;
        }
    }

    /**
     * Finds an interval which fully contains the specified 'date'. Returns 'null' if there is no such interval.
     * 
     * @param date
     * @param establishedIntervals
     * @return
     */
    protected static <T> IntervalValue<T> findContaining(final DateTime date, final List<IntervalValue<T>> establishedIntervals) {
        final int index = Collections.binarySearch(establishedIntervals, new IntervalValue<T>(new Interval(date, date/*irrelevant*/), null /*irrelevant*/));

        final int foundIntervalIndex;
        if (index >= 0) {
            foundIntervalIndex = index;
        } else {
            final int i = (-index - 1 - 1);
            foundIntervalIndex = (i >= 0 && i <= establishedIntervals.size() - 1) ? i : -1;
        }

        if (foundIntervalIndex >= 0 && establishedIntervals.get(foundIntervalIndex).getInterval().contains(date)) {
            return establishedIntervals.get(foundIntervalIndex);
        } else {
            return null;
        }
    }

    public static <T> boolean containsFully(final IntervalValue<T> what, final List<IntervalValue<T>> intervals) {
        return findContaining(what, intervals) != null;

        //	final List<IntervalValue<T>> removed = removeIntervals(what, intervals);
        //	return removed.isEmpty();
    }

    public static <T> List<IntervalValue<T>> union(final List<IntervalValue<T>> intervals, final IntervalValue<T> intervalToUnion) {
        final Set<IntervalValue<T>> set = /* FIXME should be used TreeSet?? *//* new TreeSet<>(); */new /*Linked*/HashSet<>();

        if (intervals.isEmpty()) {
            set.add(intervalToUnion);
        } else {
            for (final IntervalValue<T> i : intervals) {
                set.addAll(union(i, intervalToUnion));
            }
        }
        final List<IntervalValue<T>> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    private static <T> List<IntervalValue<T>> union(final IntervalValue<T> interval1, final IntervalValue<T> interval2) {
        if (interval1.overlaps(interval2)) {
            final DateTime st1 = interval1.getStart();
            final DateTime st2 = interval2.getStart();
            final DateTime en1 = interval1.getEnd();
            final DateTime en2 = interval2.getEnd();

            final DateTime start = st1.isBefore(st2) ? st1 : st2;
            final DateTime end = en1.isBefore(en2) ? en2 : en1;

            return Arrays.asList(closed_to_open(start, end, interval1.getValue()));
        } else {
            return interval1.isBefore(interval2) ? Arrays.asList(interval1, interval2) : Arrays.asList(interval2, interval1);
        }
    }

    protected static <T> List<IntervalValue<T>> remove(final IntervalValue<T> interval, final IntervalValue<T> intervalToRemove) {
        final List<IntervalValue<T>> complementIntervals = complement(intervalToRemove);
        final List<IntervalValue<T>> intersected = new ArrayList<>();

        for (final IntervalValue<T> i : complementIntervals) {
            final IntervalValue<T> overlap = interval.overlap(i);
            if (overlap != null) {
                intersected.add(overlap);
            }
        }
        return intersected;
    }

    private static <T> List<IntervalValue<T>> complement(final IntervalValue<T> interval) {
        final List<IntervalValue<T>> complementIntervals = new ArrayList<>();
        if (!INFINITY_LEFT.equals(interval.getStart())) {
            complementIntervals.add(closedInfinity_to_open(interval.getStart(), interval.getValue()));
        }
        if (!INFINITY_RIGHT.equals(interval.getEnd())) {
            complementIntervals.add(closed_to_openInfinity(interval.getEnd(), interval.getValue()));
        }
        return complementIntervals;
    }
}