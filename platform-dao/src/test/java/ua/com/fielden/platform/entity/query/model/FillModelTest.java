package ua.com.fielden.platform.entity.query.model;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TrivialPersistentEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.model.FillModels.fill;

public class FillModelTest extends AbstractDaoTestCase {

    @Test
    public void applying_a_fill_model_to_plain_properties_results_in_specified_values_assigned_to_them() {
        final var trivialEntity = save(new_(TrivialPersistentEntity.class, "a"));
        final var qem = from(select(EntityToFill.class).model())
                .with(fill($ -> $.set("plainStr", "hello").set("plainEntity", trivialEntity)))
                .model();
        final var entity = co$(EntityToFill.class).getFirstEntities(qem, 1).getFirst();

        assertEquals("hello", entity.getPlainStr());
        assertFalse(entity.getProperty("plainStr").isDirty());
        assertEquals(trivialEntity, entity.getPlainEntity());
        assertFalse(entity.getProperty("plainEntity").isDirty());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        save(new_(EntityToFill.class, "A"));
    }

}
