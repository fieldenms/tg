package ua.com.fielden.platform.entity.validation.exists;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import ua.com.fielden.platform.entity.activatable.test_entities.Member1;
import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.EntityTwo;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_DIRTY;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// Tests that cover entity-exists validation for properties of union entities.
///
/// [EntityExistsValidationTest] contains tests that cover entity-exists validation for union-typed properties.
///
/// @see EntityExistsValidator
///
public class UnionEntityExistsValidationTest extends AbstractDaoTestCase {

    @Test
    public void dirty_entities_cannot_be_assigned_to_properties_of_union_entities() {
        final var one = save(new_(EntityOne.class, "A"));
        one.setStringProperty("hello");
        assertTrue(one.isDirty());
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        Assertions.assertThat(union.getProperty(UnionEntity.Property.propertyOne).getFirstFailure())
                .hasMessage(format(ERR_DIRTY, one, getEntityTitleAndDesc(one).getKey()));
    }

    @Test
    public void non_persisted_entities_cannot_be_assigned_to_properties_of_union_entities() {
        final var one = new_(EntityOne.class, "A");
        assertFalse(one.isPersisted());
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        Assertions.assertThat(union.getProperty(UnionEntity.Property.propertyOne).getFirstFailure())
                .hasMessage(format(EntityExistsValidator.ERR_WAS_NOT_FOUND, getEntityTitleAndDesc(one).getKey()));
    }

    @Test
    public void non_persisted_entities_can_be_assigned_to_properties_of_union_entities_if_skipNew_is_true() {
        final var two = new_(EntityTwo.class, "A");
        assertFalse(two.isPersisted());
        final var union = new_(UnionEntity.class).setPropertyTwo(two);
        assertNull(union.getProperty(UnionEntity.Property.propertyOne).getFirstFailure());
    }

    @Test
    public void inactive_entities_can_be_assigned_to_properties_of_union_entities() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var union = new_(Union.class).setMember1(m1);
        assertNull(union.getProperty("member1").getFirstFailure());
    }


}
