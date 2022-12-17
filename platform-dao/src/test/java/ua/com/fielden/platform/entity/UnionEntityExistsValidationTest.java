package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.meta.MetaProperty.ERR_REQUIRED;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_UNION_INVALID;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_UNION_UNINSTRUMENTED;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_WAS_NOT_FOUND;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.MORE_THEN_ONE_FOUND_ERR;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.WAS_NOT_FOUND_CONCRETE_ERR;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockMoreThanOneEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.EntityWithUnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.sample.domain.UnionEntityWithSkipExistsValidation;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class UnionEntityExistsValidationTest extends AbstractDaoTestCase {

    @Test
    public void unistrumented_union_entity_can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation(new TgBogieLocation());

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(ERR_UNION_UNINSTRUMENTED, getEntityTitleAndDesc(TgBogieLocation.class).getKey()),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void valid_union_entity_can_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));
        bogie.setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop));

        assertNotNull(bogie.getLocation());
        assertTrue(bogie.getProperty("location").isValid());
    }

    @Test
    public void union_entity_without_active_property_can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation(co$(TgBogieLocation.class).new_());

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                ERR_UNION_INVALID,
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                format(
                    ERR_REQUIRED,
                    getTitleAndDesc(KEY, TgBogieLocation.class).getKey(),
                    getEntityTitleAndDesc(TgBogieLocation.class).getKey()
                )
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void union_entity_with_non_existing_active_property_can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        final var workshop = (TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1");
        bogie.setLocation(co$(TgBogieLocation.class).new_().setWorkshop(workshop));

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                ERR_UNION_INVALID,
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                format(
                    ERR_WAS_NOT_FOUND,
                    getEntityTitleAndDesc(TgWorkshop.class).getKey()
                )
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void union_entity_with_existing_active_property__that_has_been_deleted__can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        final var workshop = co$(TgWorkshop.class).save((TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1"));
        final var bogieLocation = co$(TgBogieLocation.class).new_().setWorkshop(workshop);

        co$(TgWorkshop.class).delete(workshop);

        assertTrue(bogieLocation.isValid().isSuccessful());

        bogie.setLocation(bogieLocation);

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                WAS_NOT_FOUND_CONCRETE_ERR,
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                "W1"
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void mock_union_entity_can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockNotFoundEntity(TgBogieLocation.class, "UNKNOWN"));

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                WAS_NOT_FOUND_CONCRETE_ERR,
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                "UNKNOWN"
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void more_than_one_mock_union_entity_can_not_be_assigned() {
        final var bogie = co$(TgBogie.class).new_();
        bogie.setLocation((TgBogieLocation) createMockMoreThanOneEntity(TgBogieLocation.class, "MANY"));

        assertNull(bogie.getLocation());
        assertFalse(bogie.getProperty("location").isValid());
        assertEquals(
            format(
                MORE_THEN_ONE_FOUND_ERR,
                getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                "MANY"
            ),
            bogie.getProperty("location").getFirstFailure().getMessage()
        );
    }

    @Test
    public void skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned() {
        final var entityWithUnion = co$(EntityWithUnionEntityWithSkipExistsValidation.class).new_();
        final var workshop = (TgWorkshop) co$(TgWorkshop.class).new_().setKey("W1");
        entityWithUnion.setUnion(co$(UnionEntityWithSkipExistsValidation.class).new_().setWorkshop(workshop));

        assertNotNull(entityWithUnion.getUnion());
        assertTrue(entityWithUnion.getProperty("union").isValid());
    }

}