package ua.com.fielden.platform.validators;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.utils.Validators;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OverlappingSequentialClosedPeriodsWithoutGapsTest extends AbstractDomainDrivenTestCase {

    private final ITgTimesheet dao = getInstance(ITgTimesheet.class);

    @Test
    public void test_overlapping_for_new_closed_before_but_touches_start_of_the_next() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-10-31 12:00:00")).setFinishDate(date("2011-11-01 12:00:00")).setIncident("001");
	assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_before() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-10-31 12:00:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_within() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 12:30:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_over_two() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 13:30:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_closed_after_but_touches_end_of_the_last() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setFinishDate(date("2011-11-01 16:00:00")).setIncident("001");
	assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_after_but_touches_end_of_the_last() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 15:00:00")).setIncident("001");
	assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_new_open_overleping_the_last() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:00:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_entity_with_missing_values_for_matching_properties() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:00:00"));

	try {
	    Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident");
	    fail("Should have thrown an exception warning about missing value in one of the matching properties.");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_overlapping_for_modified_period_without_overleping() {
	final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
	ts.setFinishDate(date("2011-11-01 12:30:00"));
	assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_for_modified_period_with_overleping() {
	final TgTimesheet ts = dao.findByKey("USER1", date("2011-11-01 12:00:00"));
	ts.setFinishDate(date("2011-11-01 14:30:00"));
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person"));
    }

    @Test
    public void test_overlapping_without_matching_property_condition() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate"));
    }

    @Test
    public void test_overlapping_with_several_matching_propertys() {
	TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("005");
	assertFalse(Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident"));

	ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 14:00:00")).setIncident("001");
	assertTrue(Validators.overlaps(ts, dao, "startDate", "finishDate", "person", "incident"));
    }

    @Test
    public void should_have_found_overlapping_timesheet_when_overlapping_with_several_matching_properties() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:30:00")).setFinishDate(date("2011-11-01 14:00:00"));
	final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
	assertNotNull(offendedTs);
	assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
	assertEquals("Incorrect offended timesheet.", date("2011-11-01 12:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void should_have_found_overlapping_timesheet_when_overlapping_last_existing() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 14:30:00"));
	final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
	assertNotNull(offendedTs);
	assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
	assertEquals("Incorrect offended timesheet.", date("2011-11-01 13:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void should_found_overlapping_timesheet_when_overlapping_with_the_first_existing_as_result_of_open_end() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:00:00"));
	final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
	assertNotNull(offendedTs);
	assertEquals("Incorrect offended timesheet.", "USER1", offendedTs.getPerson());
	assertEquals("Incorrect offended timesheet.", date("2011-11-01 12:00:00"), offendedTs.getStartDate());
    }

    @Test
    public void should_not_found_overlapping_timesheets() {
	final TgTimesheet ts = new_composite(TgTimesheet.class, "USER1", date("2011-11-01 11:00:00")).setFinishDate(date("2011-11-01 11:55:00"));
	final TgTimesheet offendedTs = Validators.findFirstOverlapping(ts, dao, "startDate", "finishDate", "person");
	assertNull(offendedTs);
    }

    @Override
    protected void populateDomain() {
	save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 12:00:00")).setFinishDate(date("2011-11-01 13:00:00")).setIncident("001"));
	save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }

}
