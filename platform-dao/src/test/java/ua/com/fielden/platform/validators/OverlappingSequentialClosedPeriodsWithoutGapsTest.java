package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

public class OverlappingSequentialClosedPeriodsWithoutGapsTest extends AbstractDaoTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);

    @Test
    public void new_closed_period_ending_when_the_first_period_starts_does_not_overlap() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-10-31 12:00:00")).setFinishDate(date("2011-11-01 12:00:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_starting_before_all_existing_periods_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-10-31 12:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_inside_an_existing_period_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 12:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_spanning_two_adjacent_periods_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 13:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_starting_when_the_last_period_ends_does_not_overlap() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 16:00:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_starting_when_the_last_period_ends_does_not_overlap() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_starting_inside_an_existing_period_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void overlap_check_fails_if_a_matching_property_has_no_value() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:00:00"));

        try {
            Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident");
            fail("Should have thrown an exception warning about missing value in one of the matching properties.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void shrinking_a_period_does_not_overlap() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(date("2011-11-01 12:30:00"));
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void extending_a_period_into_the_next_one_overlaps() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(date("2011-11-01 14:30:00"));
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void overlap_check_can_be_performed_without_matching_properties() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate"));
    }

    @Test
    public void matching_properties_narrow_the_periods_considered_for_overlapping() {
        TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("005");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident"));

        ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident"));
    }

    @Test
    public void findFirstOverlapping_returns_the_earliest_starting_of_several_overlapped_periods() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:00:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 12:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_period_that_a_new_open_ended_period_starts_inside() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 13:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_first_period_reached_by_a_new_open_ended_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:00:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 12:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_null_when_nothing_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:00:00")).setFinishDate(date("2011-11-01 11:55:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNull(offendedTs);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 13:00:00")).setIncident("001"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }

}
