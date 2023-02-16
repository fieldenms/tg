package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockFoundMoreThanOneEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;

import java.util.function.Supplier;

import org.junit.Test;

import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.sample.domain.EntityWithUnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.UnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Test case for union entity existence validation.
 * 
 * @see EntityExistsValidator
 * @author TG Team
 * 
 */
public class UnionEntityExistsValidationTest extends AbstractDaoTestCase {

    private void valid_union_entity_can_be_assigned(final Supplier<TgBogieLocation> creator) {
        co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));

        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co(TgWorkshop.class).findByKey("W1");
        bogie.setLocation(creator.get().setWorkshop(workshop));

        assertNotNull(bogie.getLocation());
        assertTrue(bogie.getProperty("location").isValid());
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

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                "%s is invalid: %s",
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                format(
                    "Required property [%s] is not specified for entity [%s].",
                    getTitleAndDesc(KEY, TgBogieLocation.class).getKey(),
                    getEntityTitleAndDesc(TgBogieLocation.class).getKey()
                )
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
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
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                "%s is invalid: %s",
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                format(
                    "%s was not found.",
                    getEntityTitleAndDesc(TgWorkshop.class).getKey()
                )
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
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
        final var bogieLocation = co$(TgBogieLocation.class).new_().setWorkshop(workshop);

        co$(TgWorkshop.class).delete(workshop);

        assertTrue(bogieLocation.isValid().isSuccessful());

        bogie.setLocation(bogieLocation);

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                "%s [%s] was not found.",
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                "W1"
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void uninstrumented_union_entity_with_existing_active_property__that_has_been_deleted__cannot_be_assigned() {
        co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));

        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co(TgWorkshop.class).findByKey("W1");
        final var bogieLocation = new TgBogieLocation().setWorkshop(workshop);

        co$(TgWorkshop.class).delete(workshop);

        bogie.setLocation(bogieLocation);

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                "%s is invalid: %s",
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                format(
                    "%s [%s] was not found.",
                    getEntityTitleAndDesc(TgWorkshop.class).getKey(),
                    "W1"
                )
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockNotFoundEntity(TgBogieLocation.class, "UNKNOWN"));

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                "%s [%s] was not found.",
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                "UNKNOWN"
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void more_than_one_mock_union_entity_cannot_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockFoundMoreThanOneEntity(TgBogieLocation.class, "MANY"));

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals("Please choose a specific value explicitly from a drop-down.", bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    private void skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned(final Supplier<UnionEntityWithSkipExistsValidation> creator) {
        final var entityWithUnion = co$(EntityWithUnionEntityWithSkipExistsValidation.class).new_();
        final var workshop = (TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1");
        entityWithUnion.setUnion(creator.get().setWorkshop(workshop));

        assertNotNull(entityWithUnion.getUnion());
        assertTrue(entityWithUnion.getProperty("union").isValid());
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