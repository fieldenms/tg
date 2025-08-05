package ua.com.fielden.platform.entity.validation.exists;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_ActivatableUnionOwner;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Member1;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Union;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_WAS_NOT_FOUND;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_UNION_INVALID;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

public class EntityExistsValidationForUnionTypedPropertyWithUninstrumentedUnionTest extends AbstractEntityExistsValidationForUnionTypedPropertyTestCase {

    @Override
    protected <U extends AbstractUnionEntity> U newUnion(final Class<U> type) {
        try {
            final var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            final U union = ctor.newInstance();
            ctor.setAccessible(false);
            return union;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    @Test
    public void non_existing_entity_inside_uninstrumented_union_cannot_be_assigned_when_union_is_created_prior_to_deletion() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"));
        // Despite the union being created prior to deletion of `m1`, the union member property will see the effects of this deletion.
        // This is because the uninstrumented union will be instrumented and validated during the validation of the union-typed property.
        final var union = newUnion(TestExists_Union.class).setMember1(m1);
        co(TestExists_Member1.class).delete(m1);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(union);
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(format(ERR_UNION_INVALID,
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   format(ERR_ENTITY_WAS_NOT_FOUND, getEntityTitleAndDesc(m1).getKey(), m1)));
    }


}
