package ua.com.fielden.platform.swing.components.bind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jdesktop.swingx.calendar.DateUtils;
import org.junit.Test;

import ua.com.fielden.platform.swing.components.bind.development.BoundedJXDatePicker;
import ua.com.fielden.platform.swing.components.smart.datepicker.DatePickerLayer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BoundedJXDatePickerTest {

    @Test
    public void test_time_portion_modification() {
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mma");
        try {
            final Date one = format.parse("25/05/2010 13:45AM"), two = format.parse("01/02/2010 14:02AM");
            Date newDate = null, oldDate = null;

            ////
            final Long defaultTimePortionMillis = DatePickerLayer.defaultTimePortionMillisForTheEndOfDay();

            ///
            oldDate = null;

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(defaulted(newDate, defaultTimePortionMillis), BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));

            ///
            oldDate = one;

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(modifiedByOld(newDate, oldDate), BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));

            ///
            oldDate = DateUtils.startOfDay(one);

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(defaulted(newDate, defaultTimePortionMillis), BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));

        } catch (final ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void test_time_portion_modification_with_empty_default_time_portion() {
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mma");
        try {
            final Date one = format.parse("25/05/2010 13:45AM"), two = format.parse("01/02/2010 14:02AM");
            Date newDate = null, oldDate = null;

            ////
            final Long defaultTimePortionMillis = 0L;

            ///
            oldDate = null;

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));

            ///
            oldDate = one;

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(modifiedByOld(newDate, oldDate), BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));

            ///
            oldDate = DateUtils.startOfDay(one);

            //
            newDate = null;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = two;
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
            newDate = DateUtils.startOfDay(two);
            assertEquals(newDate, BoundedJXDatePicker.modifyDateByTheTimePortion(newDate, oldDate, defaultTimePortionMillis));
        } catch (final ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    private Date modifiedByOld(final Date newDate, final Date oldDate) {
        return new Date(newDate.getTime() + BoundedJXDatePicker.timePortionMillis(oldDate));
    }

    private Date defaulted(final Date newDate, final Long defaultTimePortion) {
        return new Date(newDate.getTime() + defaultTimePortion);
    }

}
