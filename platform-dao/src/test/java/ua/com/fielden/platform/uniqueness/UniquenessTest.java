package ua.com.fielden.platform.uniqueness;

import org.junit.Test;
import ua.com.fielden.platform.entity.validation.UniqueValidator;
import ua.com.fielden.platform.entity.validation.test_entities.EntityWithDynamicRequiredness;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/// A test case for entity validation that enforces property uniqueness.
///
public class UniquenessTest extends AbstractDaoTestCase {

    public static final String UNIQUE_VALUE = "UNIQUE VALUE";

    @Test
    public void only_a_single_record_may_have_true_for_unique_boolean_property() {
        final var entity = new_(EntityWithDynamicRequiredness.class, "EWR-02").setProp1(10).setProp6(true)
                           .setUniqueBoolean(true);
        final var result = entity.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(UniqueValidator.ERR_VALIDATION_ERROR_TEMPLATE.formatted(true, "Unique boolean", "Entity With Dynamic Requiredness"), result.getMessage());
    }

    @Test
    public void multiple_records_may_have_false_for_unique_boolean_property() {
        save(new_(EntityWithDynamicRequiredness.class, "EWR-02").setProp1(10).setProp6(true)
             .setUniqueBoolean(false));
        save(new_(EntityWithDynamicRequiredness.class, "EWR-03").setProp1(10).setProp6(true)
             .setUniqueBoolean(false));
    }

    @Test
    public void only_a_single_record_may_have_same_value_for_unique_String_property() {
        final var entity = new_(EntityWithDynamicRequiredness.class, "EWR-02").setProp1(10).setProp6(true)
                .setUniqueString(UNIQUE_VALUE);
        final var result = entity.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(UniqueValidator.ERR_VALIDATION_ERROR_TEMPLATE.formatted(UNIQUE_VALUE, "Unique String", "Entity With Dynamic Requiredness"), result.getMessage());
    }

    @Test
    public void multiple_records_may_have_NULL_and_unique_values_for_unique_String_property() {
        save(new_(EntityWithDynamicRequiredness.class, "EWR-02").setProp1(10).setProp6(true)
                     .setUniqueString(UNIQUE_VALUE + "01"));
        save(new_(EntityWithDynamicRequiredness.class, "EWR-03").setProp1(10).setProp6(true)
                     .setUniqueString(UNIQUE_VALUE + "02"));
        save(new_(EntityWithDynamicRequiredness.class, "EWR-04").setProp1(10).setProp6(true)
                     .setUniqueString(null));
        save(new_(EntityWithDynamicRequiredness.class, "EWR-05").setProp1(10).setProp6(true)
                     .setUniqueString(null));
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        if (useSavedDataPopulationScript()) {
            return;
        }
        
        save(new_(EntityWithDynamicRequiredness.class, "EWR-01").setProp1(10).setProp6(true)
             .setUniqueBoolean(true).setUniqueString(UNIQUE_VALUE));
    }

}