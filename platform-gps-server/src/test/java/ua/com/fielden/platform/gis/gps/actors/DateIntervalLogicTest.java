package ua.com.fielden.platform.gis.gps.actors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.closed_to_open;
import static ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.containsFully;
import static ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.remove;
import static ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.removeIntervals;
import static ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.union;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;

import ua.com.fielden.platform.gis.gps.actors.DateIntervalLogic.IntervalValue;

/** A test for {@link DateIntervalLogic}. */
public class DateIntervalLogicTest {
    private static IntervalValue<DateTime> interv(final int dayFrom, final int dayTo) {
        return closed_to_open(new DateTime(2000, 1, dayFrom, 23, 59), new DateTime(2000, 1, dayTo, 23, 59), null);
    }

    // TODO complete testing!
    @Test
    public void test_containsFully() {
        assertTrue(containsFully(interv(2, 4), Arrays.asList(interv(2, 4))));
        assertTrue(containsFully(interv(2, 4), Arrays.asList(interv(2, 5))));
        assertTrue(containsFully(interv(2, 4), Arrays.asList(interv(1, 4))));
        assertTrue(containsFully(interv(2, 4), Arrays.asList(interv(1, 2), interv(2, 4))));
        assertTrue(containsFully(interv(2, 4), Arrays.asList(interv(2, 4), interv(5, 7))));

        assertFalse(containsFully(interv(2, 4), Arrays.asList(interv(3, 4))));
        assertFalse(containsFully(interv(2, 4), Arrays.asList(interv(2, 3))));
    }

    @Test
    public void test_union() {
        assertEquals(Arrays.asList(interv(4, 8), interv(10, 14), interv(14, 19), interv(20, 23)), union(Arrays.asList(interv(4, 8), interv(10, 14), interv(14, 19)), interv(20, 23)));

        System.out.println(union(new ArrayList<IntervalValue<DateTime>>(), interv(2, 5)));
        System.out.println(union(Arrays.asList(interv(1, 3)), interv(2, 5)));
        System.out.println(union(Arrays.asList(interv(1, 2)), interv(3, 5)));
        System.out.println(union(Arrays.asList(interv(1, 2)), interv(2, 5)));
    }

    @Test
    public void test_remove() {
        System.out.println(remove(interv(1, 5), interv(2, 3)));
        System.out.println(remove(interv(1, 3), interv(2, 5)));

        System.out.println();
        System.out.println(removeIntervals(new ArrayList<IntervalValue<DateTime>>(), interv(2, 5)));
        System.out.println(removeIntervals(Arrays.asList(interv(1, 3)), Arrays.asList(interv(2, 5))));
        System.out.println(removeIntervals(Arrays.asList(interv(1, 3), interv(4, 5)), Arrays.asList(interv(2, 5))));
    }
}