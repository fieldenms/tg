package ua.com.fielden.platform.entity.query.model;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class FillModelTest extends AbstractDaoTestCase {

    @Test
    public void applying_a_fill_model_to_plain_properties_results_in_specified_values_assigned_to_them() {
        final var trivialEntity = save(new_(TrivialPersistentEntity.class, "a"));
        final var qem = from(select(EntityToFill.class).model())
                .with(getInstance(FillModelBuilder.class).set("plainStr", "hello").set("plainEntity", trivialEntity).build(EntityToFill.class))
                .model();
        final var entity = co$(EntityToFill.class).getFirstEntities(qem, 1).getFirst();

        assertEquals("hello", entity.getPlainStr());
        assertFalse(entity.getProperty("plainStr").isDirty());
        assertEquals(trivialEntity, entity.getPlainEntity());
        assertFalse(entity.getProperty("plainEntity").isDirty());
    }

    @Test
    public void applying_a_fill_model_to_non_plain_properties_results_in_exception_upon_build() {
        try {
            getInstance(FillModelBuilder.class).set("plainStr", "hello").set("key", "some key value").build(EntityToFill.class);
            fail("Expected a failure due to the use of non-plain property.");
        } catch (final FillModelException ex) {
            assertEquals(FillModelBuilder.ERR_NON_PLAIN_PROPS.formatted(EntityToFill.class.getSimpleName(), "[key]"), ex.getMessage());
        }
    }

    @Test
    public void applying_a_fill_model_to_non_existing_properties_results_in_exception_upon_build() {
        try {
            getInstance(FillModelBuilder.class).set("plainStr", "hello").set("nonExistingProperty", "some value").build(EntityToFill.class);
            fail("Expected a failure due to the use of non-existing property.");
        } catch (final NoSuchPropertyException ex) {
            assertEquals(NoSuchPropertyException.ERR_NO_SUCH_PROP.formatted("nonExistingProperty", EntityToFill.class.getSimpleName()), ex.getMessage());
        }
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        save(new_(EntityToFill.class, "A"));
    }

}
