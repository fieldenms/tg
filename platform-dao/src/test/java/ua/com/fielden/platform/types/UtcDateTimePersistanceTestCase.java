package ua.com.fielden.platform.types;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeConstants;
import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class UtcDateTimePersistanceTestCase extends AbstractDaoTestCase {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        utcFormat.setTimeZone(UTC);
    }
    
    private static final TimeZone australia = TimeZone.getTimeZone("Australia/Melbourne");
    private static final DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        localFormat.setTimeZone(australia);
    }
    
    @Test
    public void utc_date_is_stored_and_retrieved_correctly_with_respect_to_daylightsaving() throws Exception {
        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        
        final String dateTime1 = "2013-04-07 2:30:00";
        final Date dob = date(dateTime1);
        final Date utcDob = new Date(dob.getTime() - DateTimeConstants.MILLIS_PER_HOUR);
        
        System.out.println("             Local DOB: " + dob);
        System.out.println("formated     Local DOB: " + utcFormat.format(dob));
        System.out.println("             UTC DOB: " + utcDob);
        System.out.println("formated     UTC DOB: " + utcFormat.format(utcDob));
        
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null)
                .setDob(dob).setUtcDob(utcDob));
        
        assertEquals(dob, date.getDob());
        assertEquals(utcDob, date.getUtcDob());
        
        System.out.println("----------------- loaded from db --------------");
        System.out.println("             Local DOB: " + date.getDob());
        System.out.println("formated     Local DOB: " + utcFormat.format(date.getDob()));
        System.out.println("             UTC DOB: " + date.getUtcDob());
        System.out.println("formated     UTC DOB: " + utcFormat.format(date.getUtcDob()));
    }
    
    @Test
    public void date_conversions_between_local_and_UTC_timezone_using_string_representation_works_as_expected() throws Exception {
        final DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        final DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        localFormat.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
        
        
        final Date localDate = localFormat.parse("2016-01-01 11:00:00");
        final Date utcDate = utcFormat.parse("2016-01-01 00:00:00");
        
        assertEquals(localDate.getTime(), utcDate.getTime());
        
        final Date invalidLocalFromUtcDate = localFormat.parse(utcFormat.format(utcDate));
        assertNotEquals(invalidLocalFromUtcDate.getTime(), utcDate.getTime());
        
        final Date utcFromUtcDate = utcFormat.parse(utcFormat.format(utcDate));
        final Date localFromUtcDate = localFormat.parse(localFormat.format(utcFromUtcDate));
        assertEquals(localFromUtcDate.getTime(), utcFromUtcDate.getTime());
    }

}
