package ua.com.fielden.platform.entity.validation.exists;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Member1;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Member2;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Member3;
import ua.com.fielden.platform.entity.validation.exists.test_entities.TestExists_Union;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_DIRTY;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_WAS_NOT_FOUND;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// Tests that cover entity-exists validation for union properties (i.e. the properties of union entities).
///
/// Tests that cover entity-exists validation for union-typed properties can be found in [AbstractEntityExistsValidationForUnionTypedPropertyTestCase] and its descendants.
///
/// @see EntityExistsValidator
///
public class UnionEntityExistsValidationTest extends AbstractDaoTestCase {

    @Test
    public void dirty_entities_cannot_be_assigned_to_properties_of_union_entities() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"));
        m1.setStr1("hello");
        assertTrue(m1.isDirty());
        final var union = new_(TestExists_Union.class).setMember1(m1);
        Assertions.assertThat(union.getProperty("member1").getFirstFailure())
                .hasMessage(ERR_DIRTY.formatted(m1, getEntityTitleAndDesc(m1).getKey()));
    }

    @Test
    public void non_persisted_entities_cannot_be_assigned_to_properties_of_union_entities() {
        final var m1 = new_(TestExists_Member1.class, "M1");
        assertFalse(m1.isPersisted());
        final var union = new_(TestExists_Union.class).setMember1(m1);
        Assertions.assertThat(union.getProperty("member1").getFirstFailure())
                .hasMessage(ERR_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m1).getKey()));
    }

    @Test
    public void non_persisted_entities_can_be_assigned_to_properties_of_union_entities_if_skipNew_is_true() {
        final var m2 = new_(TestExists_Member2.class, "M2");
        assertFalse(m2.isPersisted());
        final var union = new_(TestExists_Union.class).setMember2(m2);
        assertTrue(union.getProperty("member2").isValid());
    }

    @Test
    public void inactive_entities_can_be_assigned_to_properties_of_union_entities() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var union = new_(TestExists_Union.class).setMember1(m1);
        assertTrue(union.getProperty("member1").isValid());
    }

    @Test
    public void non_existing_entity_can_be_assigned_to_properties_of_union_entities_with_SkipEntityExistsValidation() {
        final var m3 = save(new_(TestExists_Member3.class, "M3").setActive(false));
        co(TestExists_Member3.class).delete(m3);
        final var union = new_(TestExists_Union.class).setMember3(m3);
        assertTrue(union.getProperty("member3").isValid());
    }

}
