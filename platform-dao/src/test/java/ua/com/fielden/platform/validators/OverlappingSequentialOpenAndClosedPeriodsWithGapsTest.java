package ua.com.fielden.platform.validators;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

import java.util.Optional;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyProxied;

/// A test case for overlapping validation where existing periods are a mix of closed and open-ended ones, separated by gaps.
///
/// All timesheets in the fixture belong to `USER1` and fall on 2011-11-01:
/// * 10:00 -- 11:00, closed;
/// * a gap;
/// * 12:00 -- 14:00, closed;
/// * a gap;
/// * 15:00 onwards, open-ended.
///
public class OverlappingSequentialOpenAndClosedPeriodsWithGapsTest extends AbstractDaoTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);

    @Test
    public void new_closed_period_strictly_inside_a_gap_does_not_overlap() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:15:00")).setFinishDate(date("2011-11-01 11:45:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_filling_a_gap_and_touching_both_neighbours_does_not_overlap() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:00:00")).setFinishDate(date("2011-11-01 12:00:00")).setIncident("001");
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_inside_an_open_ended_period_overlaps() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 17:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_starting_inside_a_closed_period_overlaps() {
        // The period ends at 14:30, that is, in the gap -- the open-ended timesheet starting at 15:00 is not reached.
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_closed_period_containing_an_existing_period_overlaps() {
        // The period spans both gaps and thus completely contains the timesheet occupying 12:00-14:00.
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:30:00")).setFinishDate(date("2011-11-01 14:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_overlaps_an_existing_open_ended_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void new_open_ended_period_starting_in_a_gap_overlaps_the_period_that_follows() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:30:00")).setIncident("001");
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void closing_the_last_open_ended_period_does_not_overlap() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        ts.setFinishDate(date("2011-11-01 17:30:00"));
        // The period held before the change does not overlap either, so the assignment has to be confirmed for the assertion below to mean anything.
        assertEquals("Precondition: the new finish date was assigned.", date("2011-11-01 17:30:00"), ts.getFinishDate());
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void expanding_a_period_into_the_surrounding_gaps_does_not_overlap() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setStartDate(date("2011-11-01 11:30:00"));
        ts.setFinishDate(date("2011-11-01 14:30:00"));
        // The period held before the change does not overlap either, so the assignments have to be confirmed for the assertion below to mean anything.
        assertEquals("Precondition: the new start date was assigned.", date("2011-11-01 11:30:00"), ts.getStartDate());
        assertEquals("Precondition: the new finish date was assigned.", date("2011-11-01 14:30:00"), ts.getFinishDate());
        assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void extending_a_closed_period_into_the_next_one_overlaps() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(date("2011-11-01 15:30:00"));
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void removing_the_finish_date_makes_a_period_overlap_a_later_one() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        ts.setFinishDate(null);
        assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_overlapped_by_a_new_closed_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00")).setFinishDate(date("2011-11-01 19:00:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_overlapped_by_a_new_open_ended_period() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 16:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void findFirstOverlapping_returns_the_open_ended_period_that_a_new_open_ended_period_starting_in_a_gap_runs_into() {
        final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:30:00"));
        final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
        assertNotNull(offendedTs);
        assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void firstOverlapping_uses_the_explicit_period_bounds_rather_than_those_held_by_the_entity() {
        // This timesheet occupies 12:00-14:00 and does not overlap anything, as the previous one ends at 11:00 and the next one starts at 15:00.
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        assertNull("Precondition: the period held by the entity does not overlap.",
                   Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person"));

        // The very same entity, tested against a period it does not hold, does overlap the timesheet that precedes it.
        // The period is entirely before the one held by the entity, so both explicit bounds are what select the answer.
        final Optional<TgTimesheet> offendedTs = Validators.firstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                                             date("2011-11-01 10:30:00"), date("2011-11-01 11:30:00"), "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 10:00:00"), offendedTs.get().getStartDate());
    }

    @Test
    public void firstOverlapping_finds_nothing_when_an_explicit_period_fits_inside_a_gap() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        // The period lies strictly inside the 11:00-12:00 gap -- its end is what keeps it clear of the timesheet starting at 12:00.
        assertTrue(Validators.firstOverlapping(ts, null, dao, "startDate", "finishDate",
                                               date("2011-11-01 11:15:00"), date("2011-11-01 11:45:00"), "person").isEmpty());
    }

    @Test
    public void firstOverlapping_returns_the_open_ended_period_that_an_explicit_open_ended_period_starting_in_a_gap_runs_into() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        // An open-ended period starting in the gap runs into the timesheet that starts at 15:00.
        final Optional<TgTimesheet> offendedTs = Validators.firstOverlapping(ts, null, dao, "startDate", "finishDate",
                                                                             date("2011-11-01 14:30:00"), null, "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.get().getStartDate());
    }

    @Test
    public void firstOverlapping_initialises_the_overlapping_entity_with_the_specified_fetch_model() {
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
        final Optional<TgTimesheet> offendedTs = Validators.firstOverlapping(ts, fetchOnly(TgTimesheet.class).with("startDate"), dao, "startDate", "finishDate",
                                                                            date("2011-11-01 16:30:00"), date("2011-11-01 19:00:00"), "person");
        assertTrue(offendedTs.isPresent());
        assertEquals("Incorrect offended timesheet.", date("2011-11-01 15:00:00"), offendedTs.get().getStartDate());
        // Property `incident` lies outside the fetch model, so it must be proxied -- the default fetch model would have initialised it.
        assertTrue("Property outside the fetch model should be proxied.", isPropertyProxied(offendedTs.get(), "incident"));
    }

    @Test
    public void firstOverlapping_excludes_the_entity_itself_when_matching_explicit_period_bounds() {
        // The open-ended timesheet, tested against its own period, must not report itself.
        final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 15:00:00"));
        assertTrue(Validators.firstOverlapping(ts, null, dao, "startDate", "finishDate",
                                               date("2011-11-01 15:00:00"), null, "person").isEmpty());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 10:00:00")).setFinishDate(date("2011-11-01 11:00:00")).setIncident("001"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001"));
        save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("002"));
    }

}
