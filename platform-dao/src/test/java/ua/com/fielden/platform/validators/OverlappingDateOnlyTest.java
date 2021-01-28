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
    public void validator_detects_intersection_at_the_beginning_of_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_intersection_at_the_end_of_the_iterval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_contains_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-02-15 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_is_inside_of_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-20 00:00:00")).setToDateProp(date("2021-01-25 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_didnt_find_the_interval_that_would_overlap_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-10 00:00:00"));
        assertFalse(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_touches_the_end_of_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-01-01 00:00:00")).setToDateProp(date("2021-01-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_touches_the_end_of_the_interval_with_time_portion() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-20 15:00:00")).setToDateProp(date("2021-03-02 10:13:17"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_touches_the_begining_of_the_interval_of_the_new_entity() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 00:00:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Test
    public void validator_detects_interval_that_touches_the_begining_of_the_interval_with_time_portion() {
        final TgDateTestEntity dte = new_(TgDateTestEntity.class, "next_date_test_entity").setFromDateProp(date("2021-02-01 08:15:00")).setToDateProp(date("2021-02-12 00:00:00"));
        assertTrue(Validators.overlaps(dte, dao, "fromDateProp", "toDateProp"));
    }

    @Override
    protected void populateDomain() {
        save(new_(TgDateTestEntity.class, "date_test_entity").setFromDateProp(date("2021-01-12 00:00:00")).setToDateProp(date("2021-02-01 00:00:00")));
        save(new_(TgDateTestEntity.class, "date_test_entity_with_time_portion").setFromDateProp(date("2021-03-02 13:01:12")).setToDateProp(date("2021-03-10 10:43:29")));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}
