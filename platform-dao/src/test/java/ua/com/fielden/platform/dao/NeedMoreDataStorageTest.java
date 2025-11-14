package ua.com.fielden.platform.dao;

import org.junit.Test;
import ua.com.fielden.platform.continuation.NeedMoreDataStorage;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.test.runners.SkipNeedMoreDataStorageBinding;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Map;

import static org.junit.Assert.*;

/// This test case ensures correct behaviour of [NeedMoreDataStorage].
///
public class NeedMoreDataStorageTest extends AbstractDaoTestCase {

    @Test
    public void adding_continuation_data_to_companions_populates_scoped_NeedMoreDataStorage() {
        assertTrue("Each test starts in a new scope with empty NeedMoreDataStorage.", NeedMoreDataStorage.moreData().isEmpty());

        // Create companion, set more data for it and observer NeedMoreDataStorage change.
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        assertTrue(co.moreData().isEmpty());

        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        co.setMoreData("TEST_KEY", warnings);

        assertFalse(NeedMoreDataStorage.moreData().isEmpty());
        assertEquals(warnings, NeedMoreDataStorage.moreData("TEST_KEY").orElseThrow());
        assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
    }

    @Test
    public void scoped_NeedMoreDataStorage_is_available_to_companions_in_that_scope() {
        assertTrue("Each test starts in a new scope with empty NeedMoreDataStorage.", NeedMoreDataStorage.moreData().isEmpty());
        final EntityWithMoneyDao co = co$(EntityWithMoney.class);
        assertTrue(co.moreData().isEmpty());

        // Put more data into storage and observe is availability in companions.
        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        NeedMoreDataStorage.putMoreData("TEST_KEY", warnings);

        assertFalse(NeedMoreDataStorage.moreData().isEmpty());
        assertEquals(warnings, NeedMoreDataStorage.moreData("TEST_KEY").orElseThrow());
        assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
    }

    @Test
    @SkipNeedMoreDataStorageBinding
    public void running_op_with_NeedMoreDataStorage_makes_more_data_available_to_it() {
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());

        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        NeedMoreDataStorage.runWithMoreData(Map.of("TEST_KEY", warnings), () ->
            {
                final EntityWithMoneyDao co = co$(EntityWithMoney.class);
                assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
            }
        );

        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());
    }

    @Test
    @SkipNeedMoreDataStorageBinding
    public void running_op_with_NeedMoreDataStorage_in_a_nested_scope_makes_all_more_data_available_to_it() {
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());

        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        NeedMoreDataStorage.runWithMoreData(Map.of("TEST_KEY", warnings), () ->
            {
                final EntityWithMoneyDao co = co$(EntityWithMoney.class);
                assertEquals(1, NeedMoreDataStorage.moreData().size());
                assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
                NeedMoreDataStorage.runWithMoreData(Map.of("TEST_KEY1", warnings), () -> {
                    assertEquals(2, NeedMoreDataStorage.moreData().size());
                    assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
                    assertEquals(warnings, co.moreData("TEST_KEY1").orElseThrow());
                });
            }
        );

        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());
    }

    @Test
    @SkipNeedMoreDataStorageBinding
    public void calling_op_with_NeedMoreDataStorage_makes_more_data_available_to_it() {
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());

        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        final var result = NeedMoreDataStorage.callWithMoreData(Map.of("TEST_KEY", warnings), () ->
            {
                final EntityWithMoneyDao co = co$(EntityWithMoney.class);
                return co.moreData("TEST_KEY").orElseThrow();
            }
        );

        assertEquals(warnings, result);
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());
    }

    @Test
    @SkipNeedMoreDataStorageBinding
    public void calling_op_with_NeedMoreDataStorage_in_a_nested_scope_makes_all_more_data_available_to_it() {
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());

        final var coWarnings = co$(AcknowledgeWarnings.class);
        final var warnings = coWarnings.new_();
        final var result = NeedMoreDataStorage.callWithMoreData(Map.of("TEST_KEY", warnings), () ->
            {
                final EntityWithMoneyDao co = co$(EntityWithMoney.class);
                assertEquals(1, NeedMoreDataStorage.moreData().size());
                assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
                return NeedMoreDataStorage.callWithMoreData(Map.of("TEST_KEY1", warnings), () -> {
                    assertEquals(2, NeedMoreDataStorage.moreData().size());
                    assertEquals(warnings, co.moreData("TEST_KEY").orElseThrow());
                    return co.moreData("TEST_KEY1").orElseThrow();
                });
            }
        );

        assertEquals(warnings, result);
        assertFalse("NeedMoreDataStorage is not bound.", NeedMoreDataStorage.isBound());
    }

}
