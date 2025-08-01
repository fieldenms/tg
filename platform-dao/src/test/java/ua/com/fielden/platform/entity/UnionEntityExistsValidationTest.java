package ua.com.fielden.platform.entity;

import org.junit.Test;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.function.Supplier;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_UNION_INVALID;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockFoundMoreThanOneEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;

/**
 * Test case for union entity existence validation.
 * 
 * @see EntityExistsValidator
 * @author TG Team
 * 
 */
public class UnionEntityExistsValidationTest extends AbstractDaoTestCase {

    @Test
    public void inactive_entities_can_be_assigned_to_properties_of_union_entities() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var union = new_(Union.class).setMember1(m1);
        assertNull(union.getProperty("member1").getFirstFailure());
    }

    private void valid_union_entity_can_be_assigned(final Supplier<TgBogieLocation> creator) {
        co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));

        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co(TgWorkshop.class).findByKey("W1");
        bogie.setLocation(creator.get().setWorkshop(workshop));

        assertNull(bogie.getProperty("location").getFirstFailure());
    }

    @Test
    public void valid_union_entity_can_be_assigned() {
        valid_union_entity_can_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_valid_union_entity_can_be_assigned() {
        valid_union_entity_can_be_assigned(() -> new TgBogieLocation());
    }

    private void union_entity_without_active_property_cannot_be_assigned(final Supplier<TgBogieLocation> creator) {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation(creator.get());

        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(format("%s is invalid: %s",
                            getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                            format("Required property [%s] is not specified for entity [%s].",
                                   getTitleAndDesc(KEY, TgBogieLocation.class).getKey(),
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey())),
                     bogie.getProperty("location").getFirstFailure().getMessage());
    }

    @Test
    public void union_entity_without_active_property_cannot_be_assigned() {
        union_entity_without_active_property_cannot_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_union_entity_without_active_property_cannot_be_assigned() {
        union_entity_without_active_property_cannot_be_assigned(() -> new TgBogieLocation());
    }

    private void union_entity_with_non_existing_active_property_cannot_be_assigned(final Supplier<TgBogieLocation> creator) {
        final var bogie = co$(TgBogie.class).new_();
        final var workshop = (TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1");
        bogie.setLocation(creator.get().setWorkshop(workshop));

        assertNull(bogie.getLocation());
        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(format(ERR_UNION_INVALID,
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                   format("%s was not found.", getEntityTitleAndDesc(TgWorkshop.class).getKey())));
    }

    @Test
    public void union_entity_with_non_existing_active_property_cannot_be_assigned() {
        union_entity_with_non_existing_active_property_cannot_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_union_entity_with_non_existing_active_property_cannot_be_assigned() {
        union_entity_with_non_existing_active_property_cannot_be_assigned(() -> new TgBogieLocation());
    }

    @Test
    public void union_entity_with_existing_active_property__that_has_been_deleted__cannot_be_assigned() {
        co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));

        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co(TgWorkshop.class).findByKey("W1");
        assertNotNull(workshop);
        final var bogieLocation = co$(TgBogieLocation.class).new_().setWorkshop(workshop);

        co$(TgWorkshop.class).delete(workshop);

        assertTrue(bogieLocation.isValid().isSuccessful());

        bogie.setLocation(bogieLocation);

        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(format("%s [%s] was not found.", getEntityTitleAndDesc(workshop).getKey(), workshop),
                     bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void uninstrumented_union_entity_with_existing_active_property__that_has_been_deleted__cannot_be_assigned() {
        co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));

        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co(TgWorkshop.class).findByKey("W1");
        assertNotNull(workshop);
        final var bogieLocation = new TgBogieLocation().setWorkshop(workshop);

        co$(TgWorkshop.class).delete(workshop);

        bogie.setLocation(bogieLocation);

        assertThat(bogie.getProperty("location").getFirstFailure())
                        .hasMessage(format(ERR_UNION_INVALID,
                                           getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                           format("%s [%s] was not found.", getEntityTitleAndDesc(workshop).getKey(), workshop)));
    }

    @Test
    public void mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockNotFoundEntity(TgBogieLocation.class, "UNKNOWN"));

        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(format("%s [%s] was not found.", getEntityTitleAndDesc(TgBogieLocation.class).getKey(), "UNKNOWN"),
                     bogie.getProperty("location").getFirstFailure().getMessage());
    }

    @Test
    public void more_than_one_mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockFoundMoreThanOneEntity(TgBogieLocation.class, "MANY"));

        assertFalse(bogie.getProperty("location").isValid());
        assertEquals("Please choose a specific value explicitly from a drop-down.", bogie.getProperty("location").getFirstFailure().getMessage());
    }

    private void skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned(final Supplier<UnionEntityWithSkipExistsValidation> creator) {
        final var entityWithUnion = co$(EntityWithUnionEntityWithSkipExistsValidation.class).new_();
        final var workshop = (TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1");
        entityWithUnion.setUnion(creator.get().setWorkshop(workshop));

        assertNull(entityWithUnion.getProperty("union").getFirstFailure());
    }

    @Test
    public void skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned() {
        skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned(() -> co$(UnionEntityWithSkipExistsValidation.class).new_());
    }

    @Test
    public void skipEntityExistsNew_uninstrumented_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned() {
        skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned(() -> new UnionEntityWithSkipExistsValidation());
    }

}
