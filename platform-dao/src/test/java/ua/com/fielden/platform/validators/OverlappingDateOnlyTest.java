package ua.com.fielden.platform.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.ITgDateTestEntity;
import ua.com.fielden.platform.sample.domain.TgDateTestEntity;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.Validators;

public class OverlappingDateOnlyTest extends AbstractDaoTestCase {

    private final ITgDateTestEntity dao = getInstance(ITgDateTestEntity.class);

    @Test
    public void test_overlapping_for_new_with_intersect_at_the_beginning() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_with_intersect_at_the_end() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_that_incudes_intersecting_period() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_that_is_within_intersecting_period() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-01-25 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_that_has_no_overlaps() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-10 00:00:00"));
        assertFalse(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_with_touched_end_period() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void test_overlapping_for_new_with_touched_start_period() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 00:00:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Override
    protected void populateDomain() {
        save(new_(TgDateTestEntity.class, "date_test_entity").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}
