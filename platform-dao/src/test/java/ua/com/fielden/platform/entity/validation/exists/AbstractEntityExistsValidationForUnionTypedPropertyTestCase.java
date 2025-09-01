package ua.com.fielden.platform.entity.validation.exists;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.validation.exists.test_entities.*;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockFoundMoreThanOneEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;

public abstract class AbstractEntityExistsValidationForUnionTypedPropertyTestCase extends AbstractDaoTestCase {

    protected abstract <U extends AbstractUnionEntity> U newUnion(Class<U> type);

    @Test
    public void dirty_entity_inside_union_cannot_be_assigned() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"))
                .setStr1("abc");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setUnion1(newUnion(TestExists_Union.class).setMember1(m1));

        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_DIRTY.formatted(m1, getEntityTitleAndDesc(m1).getKey())));
    }

    @Test
    public void dirty_entity_inside_union_can_be_assigned_to_property_with_default_SkipEntityExistsValidation() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(true))
                .setStr1("abc");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion3(newUnion(TestExists_Union.class).setMember1(m1));

        assertTrue(o1.getProperty("union3").isValid());
    }

    @Test
    public void dirty_entity_inside_union_cannot_be_assigned_to_property_with_skipActiveOnly() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(true))
                .setStr1("abc");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion2(newUnion(TestExists_Union.class).setMember1(m1));

        assertThat(o1.getProperty("union2").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_DIRTY.formatted(m1, getEntityTitleAndDesc(m1).getKey())));
    }

    @Test
    public void dirty_entity_inside_union_cannot_be_assigned_to_property_with_skipNew_and_skipActiveOnly() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(true))
                .setStr1("abc");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion5(newUnion(TestExists_Union.class).setMember1(m1));

        assertThat(o1.getProperty("union5").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_DIRTY.formatted(m1, getEntityTitleAndDesc(m1).getKey())));
    }

    @Test
    public void dirty_entity_inside_union_cannot_be_assigned_to_property_with_skipNew() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(true))
                .setStr1("abc");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion4(newUnion(TestExists_Union.class).setMember1(m1));

        assertThat(o1.getProperty("union4").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_DIRTY.formatted(m1, getEntityTitleAndDesc(m1).getKey())));
    }

    @Test
    public void existing_inactive_entity_inside_union_can_be_assigned_to_property_of_inactive_entity() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(false)
                .setUnion1(newUnion(TestExists_Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union1").isValid());
    }

    @Test
    public void existing_inactive_entity_inside_union_cannot_be_assigned_to_property_of_active_entity() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(newUnion(TestExists_Union.class).setMember1(m1));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(m1).getKey(), m1));
    }

    @Test
    public void existing_inactive_entity_inside_union_cannot_be_assigned_to_property_of_non_activatable_entity() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var o1 = new_(TestExists_UnionOwner.class, "O1")
                .setUnion1(newUnion(TestExists_Union.class).setMember1(m1));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(m1).getKey(), m1));
    }

    @Test
    public void existing_inactive_entity_inside_union_can_be_assigned_to_property_of_active_entity_if_both_union_typed_property_and_union_member_have_skipActiveOnly() {
        final var m4 = save(new_(TestExists_Member4.class, "M4").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion2(newUnion(TestExists_Union.class).setMember4(m4));
        assertTrue(o1.getProperty("union2").isValid());
    }

    @Test
    public void existing_inactive_entity_inside_union_cannot_be_assigned_to_property_of_active_entity_if_skipActiveOnly_is_present_only_on_union_typed_property() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion2(newUnion(TestExists_Union.class).setMember1(m1));
        assertThat(o1.getProperty("union2").getFirstFailure())
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(m1).getKey(), m1));
    }

    @Test
    public void existing_inactive_entity_inside_union_cannot_be_assigned_to_property_of_active_entity_if_skipActiveOnly_is_present_only_on_the_union_member() {
        final var m4 = save(new_(TestExists_Member4.class, "M4").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(newUnion(TestExists_Union.class).setMember4(m4));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE.formatted(getEntityTitleAndDesc(m4).getKey(), m4));
    }

    @Test
    public void existing_inactive_entity_inside_union_can_be_assigned_if_only_union_typed_property_has_default_SkipEntityExistsValidation() {
        final var m1 = save(new_(TestExists_Member1.class, "M1").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion3(newUnion(TestExists_Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union3").isValid());
    }

    @Test
    public void existing_inactive_entity_inside_union_can_be_assigned_to_property_with_default_SkipEntityExistsValidation_when_union_member_property_has_skipActiveOnly() {
        final var m4 = save(new_(TestExists_Member4.class, "M4").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion3(newUnion(TestExists_Union.class).setMember4(m4));
        assertTrue(o1.getProperty("union3").isValid());
    }

    @Test
    public void existing_inactive_entity_inside_union_can_be_assigned_to_property_with_default_SkipEntityExistsValidation_when_union_member_property_has_default_SkipEntityExistsValidation() {
        final var m3 = save(new_(TestExists_Member3.class, "M3").setActive(false));
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion3(newUnion(TestExists_Union.class).setMember3(m3));
        assertTrue(o1.getProperty("union3").isValid());
    }

    @Test
    public void new_entity_inside_union_can_be_assigned_to_property_with_skipNew_when_union_member_property_has_skipNew() {
        final var m2 = new_(TestExists_Member2.class, "M2");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion4(newUnion(TestExists_Union.class).setMember2(m2));
        assertTrue(o1.getProperty("union4").isValid());
    }

    @Test
    public void new_entity_inside_union_cannot_be_assigned_if_skipNew_is_present_only_on_union_typed_property() {
        final var m1 = new_(TestExists_Member1.class, "M1").setActive(true);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion4(newUnion(TestExists_Union.class).setMember1(m1));
        assertThat(o1.getProperty("union4").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m1).getKey())));
    }

    @Test
    public void new_entity_inside_union_cannot_be_assigned_if_skipNew_is_present_only_on_the_union_member() {
        final var m2 = new_(TestExists_Member2.class, "M2");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(newUnion(TestExists_Union.class).setMember2(m2));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m2).getKey()));
    }

    @Test
    public void new_entity_inside_union_can_be_assigned_to_property_with_skipNew_when_union_member_property_has_default_SkipEntityExistsValidation() {
        final var m3 = new_(TestExists_Member3.class, "M3");
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion4(newUnion(TestExists_Union.class).setMember3(m3));
        assertTrue(o1.getProperty("union4").isValid());
    }

    @Test
    public void non_existing_entity_inside_union_cannot_be_assigned() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"));
        // `m1` is deleted prior to creating a union instance.
        // Therefore, the effect of its deletion will be seen during the validation of the corresponding union member property.
        co(TestExists_Member1.class).delete(m1);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(newUnion(TestExists_Union.class).setMember1(m1));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TestExists_Union.class).getKey(),
                                   ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m1).getKey(), m1)));
    }

    @Test
    public void non_existing_entity_inside_union_can_be_assigned_if_only_union_typed_property_has_default_SkipEntityExistsValidation() {
        final var m1 = save(new_(TestExists_Member1.class, "M1"));
        co(TestExists_Member1.class).delete(m1);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion3(newUnion(TestExists_Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union3").isValid());
    }

    @Test
    public void non_existing_entity_inside_union_cannot_be_assigned_if_default_SkipEntityExistsValidation_is_present_only_on_the_union_member_property() {
        final var m3 = save(new_(TestExists_Member3.class, "M3"));
        co(TestExists_Member3.class).delete(m3);
        final var o1 = new_(TestExists_ActivatableUnionOwner.class, "O1")
                .setActive(true)
                .setUnion1(newUnion(TestExists_Union.class).setMember3(m3));
        assertThat(o1.getProperty("union1").getFirstFailure())
                .hasMessage(ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(m3).getKey(), m3));
    }

    @Test
    public void mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockNotFoundEntity(TgBogieLocation.class, "UNKNOWN"));

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(TgBogieLocation.class).getKey(), "UNKNOWN"));
    }

    @Test
    public void more_than_one_mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockFoundMoreThanOneEntity(TgBogieLocation.class, "MANY"));

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage("Please choose a specific value explicitly from a drop-down.");
    }

    @Test
    public void union_entity_without_active_property_cannot_be_assigned() {
        final var bogie = new_(TgBogie.class).setLocation(newUnion(TgBogieLocation.class));

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(ERR_UNION_INVALID.formatted(
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                   "Required property [%s] is not specified for entity [%s].".formatted(
                                          getTitleAndDesc(KEY, TgBogieLocation.class).getKey(),
                                          getEntityTitleAndDesc(TgBogieLocation.class).getKey())));
    }

    @Test
    public void valid_union_entity_can_be_assigned() {
        final var workshop = save(new_(TgWorkshop.class, "W1"));
        final var bogie = new_(TgBogie.class)
                .setLocation(newUnion(TgBogieLocation.class).setWorkshop(workshop));
        assertTrue(bogie.getProperty("location").isValid());
    }

}
