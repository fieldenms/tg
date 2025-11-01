package ua.com.fielden.platform.entity.validation.exists;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_ActivatableUnionOwner;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Member1;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Union;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_WAS_NOT_FOUND;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

public class EntityExistsValidationForUnionTypedPropertyWithInstrumentedUnionTest extends AbstractEntityExistsValidationForUnionTypedPropertyTestCase {

    @Override
    protected <U extends AbstractUnionEntity> U newUnion(final Class<U> type) {
        return new_(type);
    }

    @Test
    public void non_existing_entity_inside_instrumented_union_cannot_be_assigned_even_if_union_is_created_prior_to_deletion() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"));
        // Because the union is created prior to deletion of `m1`, the union member property is successfully validated.
        // The effect of deleting `m1` will be seen only during the validation of the union-typed property, hence the validation message will be different.
        final var union = newUnion(TestExists_Union.class).setMember1(m1);
        co(TestExists_Member1.class).delete(m1);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(union);
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m1).getKey(), m1));
    }

}
