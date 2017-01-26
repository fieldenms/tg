package ua.com.fielden.platform.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class UtcDateTimePersistanceTestCase extends AbstractDaoTestCase {

    private final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private final DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    {
        utcFormat.setTimeZone(UTC);
    }
    
    private final TimeZone australia = TimeZone.getTimeZone("Australia/Melbourne");
    private final DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    {
        localFormat.setTimeZone(australia);
    }
    
    @Test
    public void utc_date_is_stored_and_retrieved_correctly_with_respect_to_daylightsaving() throws Exception {
        TimeZone.setDefault(australia);
        
        final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));
        
        final Date localDate = localFormat.parse("2016-01-01 11:00:00");
        final Date utcDate = utcFormat.parse("2016-01-01 00:00:00");

        
        final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null)
                .setDob(localDate).setUtcDob(utcDate));
        
        assertEquals(localDate, date.getDob());
        assertEquals(utcDate, date.getUtcDob());
        
        TimeZone.setDefault(null);
    }
    
    @Test
    public void date_conversions_between_local_and_UTC_timezone_using_string_representation_works_as_expected() throws Exception {
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
