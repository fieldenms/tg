package ua.com.fielden.platform.types;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertPresent;

/**
 * A test case covering the usage of {@link UtcDateTimeType} in queries (persistence, retrieval).
 */
public class UtcDateTimeQueryingTestCase extends AbstractDaoTestCase {

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
        withTimeZone(australia, () -> {
            final TgPersonName chris = save(new_(TgPersonName.class, "Chris", "Chris"));

            final Date localDate = localFormat.parse("2016-01-01 11:00:00");
            final Date utcDate = utcFormat.parse("2016-01-01 00:00:00");

            final TgAuthor date = save(new_composite(TgAuthor.class, chris, "Date", null)
                    .setDob(localDate).setUtcDob(utcDate));

            assertEquals(localDate, date.getDob());
            assertEquals(utcDate, date.getUtcDob());
        });
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

    @Test
    public void fetched_value_of_utc_datetime_property_is_the_same_across_different_timezones() {
        final var co = co(TgEntityWithTimeZoneDates.class);
        final var fetch = fetch(TgEntityWithTimeZoneDates.class).with("datePropUtc");
        final var key = "something_unique_87321";
        final Supplier<TgEntityWithTimeZoneDates> fetcher = () -> assertPresent(co.findByKeyAndFetchOptional(fetch, key));

        withTimeZone(TimeZone.getTimeZone(ZoneId.of("+01")), () -> {
            final Date myDate = Date.from(Instant.now());
            save(new_(TgEntityWithTimeZoneDates.class, key).setDatePropUtc(myDate));
            assertEquals(myDate, fetcher.get().getDatePropUtc());

            withTimeZone(TimeZone.getTimeZone(ZoneId.of("+05")), () -> {
                assertEquals(myDate, fetcher.get().getDatePropUtc());
            });
        });
    }

    @Test
    public void fetched_value_of_datetime_property_is_different_across_different_timezones() {
        final var co = co(TgEntityWithTimeZoneDates.class);
        final var fetch = fetch(TgEntityWithTimeZoneDates.class).with("dateProp");
        final var key = "something_unique_74329";
        final Supplier<TgEntityWithTimeZoneDates> fetcher = () -> assertPresent(co.findByKeyAndFetchOptional(fetch, key));

        withTimeZone(TimeZone.getTimeZone(ZoneId.of("+01")), () -> {
            final Date myDate = date("2024-05-05 12:00:00");
            save(new_(TgEntityWithTimeZoneDates.class, key).setDateProp(myDate));
            assertEquals(myDate, fetcher.get().getDateProp());

            withTimeZone(TimeZone.getTimeZone(ZoneId.of("+05")), () -> {
                assertNotEquals(myDate, fetcher.get().getDateProp());
            });
        });
    }

    @Test
    public void Date_val_yielded_as_UTC_datetime_property_is_offset_from_fetched_val_by_default_timezone() {
        withTimeZone(TimeZone.getTimeZone(ZoneId.of("+06")), () -> {
            // 14:10 in +06 gets saved as 14:10 in UTC which is 20:10 in +06
            final Date myDate = date("2024-04-18 14:10:00");
            final var query = select().yield().val(myDate).as("datePropUtc").modelAsEntity(TgEntityWithTimeZoneDates.class);
            final TgEntityWithTimeZoneDates entity = co(TgEntityWithTimeZoneDates.class).getEntity(from(query).model());
            assertEquals(date("2024-04-18 20:10:00"), entity.getDatePropUtc());
        });
    }

    @Test
    public void Date_val_yielded_as_datetime_property_is_equal_to_the_fetched_val() {
        final Date myDate = date("2024-04-18 14:10:00");
        final var query = select().yield().val(myDate).as("dateProp").modelAsEntity(TgEntityWithTimeZoneDates.class);
        final TgEntityWithTimeZoneDates entity = co(TgEntityWithTimeZoneDates.class).getEntity(from(query).model());
        assertEquals(myDate, entity.getDateProp());
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    private static void withTimeZone(final TimeZone timeZone, final ThrowingRunnable runnable) {
        final TimeZone origTz = TimeZone.getDefault();
        TimeZone.setDefault(timeZone);
        try {
            runnable.run();
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        } finally {
            TimeZone.setDefault(origTz);
        }
    }

}
