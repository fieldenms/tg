package ua.com.fielden.platform.entity.validation.exists;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.activatable.test_entities.ActivatableUnionOwner;
import ua.com.fielden.platform.entity.activatable.test_entities.Member1;
import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.entity.activatable.test_entities.UnionOwner;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestIocModule;
import ua.com.fielden.platform.test.entities.TgEntityWithManyPropTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashSet;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockFoundMoreThanOneEntity;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.createMockNotFoundEntity;

/// This test case covers rules of the entity-exists validation.
///
/// @see EntityExistsValidator
/// @see SkipEntityExistsValidation
///
// TODO Some parts of this test class can be made generic in the fashion of AbstractEntityActivatabilityTestCase.
public class EntityExistsValidationTest extends AbstractDaoTestCase {
    private final CriteriaGeneratorTestIocModule module = new CriteriaGeneratorTestIocModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    private final CentreDomainTreeManagerAndEnhancer cdtm = new CentreDomainTreeManagerAndEnhancer(entityFactory, new HashSet<>(asList(TgSystem.class)));
    
    @Test
    public void existing_active_entity_can_be_assigned_to_property_with_default_validation() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
    }

    @Test
    public void existing_but_inactive_entity_cannot_be_assigned_to_property_with_default_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] exists, but is not active.", result.getMessage());
    }

    @Test
    public void existing_but_inactive_entity_inside_union_cannot_be_assigned_to_property_with_default_validation() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var o1 = new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion(new_(Union.class).setMember1(m1));
        assertThat(o1.getProperty("union").getFirstFailure())
                .hasMessage(format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, getEntityTitleAndDesc(m1).getKey(), m1));
   }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_with_default_validation_if_enclosing_entity_is_inactive() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat);
        assertTrue(sys.isValid().isSuccessful());
        assertTrue(sys.getProperty("category").isValid());
    }

    @Test
    public void existing_but_inactive_entity_inside_union_can_be_assigned_to_property_with_default_validation_if_enclosing_entity_is_inactive() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var o1 = new_(ActivatableUnionOwner.class, "O1").setActive(false).setUnion(new_(Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union").isValid());
    }

    @Test
    public void existing_but_inactive_entity_inside_union_can_be_assigned_to_property_with_default_validation_if_enclosing_entity_is_not_activatable() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var o1 = new_(UnionOwner.class, "O1").setUnion(new_(Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union").isValid());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_of_active_entity_with_skipActiveOnly() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat);
        assertTrue(sys.getProperty("thirdCategory").isValid());
    }

    @Test
    public void existing_but_inactive_entity_inside_union_can_be_assigned_to_property_of_active_entity_with_skipActiveOnly() {
        final var m1 = save(new_(Member1.class, "M1").setActive(false));
        final var o1 = new_(ActivatableUnionOwner.class, "O1").setActive(true).setUnion2(new_(Union.class).setMember1(m1));
        assertTrue(o1.getProperty("union2").isValid());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_with_default_validation_on_criteria_entity() {
        cdtm.getFirstTick().check(TgSystem.class, "critOnlySingleCategory", true);
        final var cg = injector.getInstance(ICriteriaGenerator.class);
        final var criteriaEntity = cg.generateCentreQueryCriteria(cdtm);
        
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        criteriaEntity.set("tgSystem_critOnlySingleCategory", cat2);
        
        final Result result = criteriaEntity.isValid();
        assertTrue(result.isSuccessful());
    }
    
    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_default_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co$(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] was not found.", result.getMessage());
    }

    @Test
    public void new_entity_cannot_be_assigned_to_property_with_default_validation() {
        final TgCategory newCat = co(TgCategory.class).new_().setKey("NEW CAT");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(newCat);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(format(EntityExistsValidator.ERR_WAS_NOT_FOUND, TgCategory.ENTITY_TITLE), result.getMessage());
    }

    
    @Test
    public void non_existing_entity_can_be_assigned_to_property_with_default_validation_on_criteria_entity() {
        cdtm.getFirstTick().check(TgSystem.class, "critOnlySingleCategory", true);
        final var cg = injector.getInstance(ICriteriaGenerator.class);
        final var criteriaEntity = cg.generateCentreQueryCriteria(cdtm);
        
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        
        // let's delete cat2 to make it non-existing
        co$(TgCategory.class).delete(cat2);
        
        criteriaEntity.set("tgSystem_critOnlySingleCategory", cat2);
        
        final Result result = criteriaEntity.isValid();
        assertTrue(result.isSuccessful());
    }
    
    @Test
    public void existing_entity_can_be_assigned_to_property_with_skipped_exists_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_can_be_assigned_to_property_with_skipped_exists_validation() {
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(new_(TgCategory.class, "Cat3"));

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co$(TgCategory.class).delete(cat2);

        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] was not found.", result.getMessage());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }

    @Test
    public void existing_and_active_entity_can_be_assigned_to_property_with_only_active_check_skipped_validation() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getThirdCategory());
    }

    @Test
    public void entity_exists_validation_does_not_permit_dirty_entities() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat1);

        final String entityTitle = getEntityTitleAndDesc(cat1.getType()).getKey();
        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(format(ERR_DIRTY, cat1, entityTitle), result.getMessage());
    }

    @Test
    public void dirty_values_inside_a_union_are_not_allowed() {
        final var one = save(new_(EntityOne.class, "ONE"))
                .setStringProperty("abc");
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp(union);

        assertFalse(entity.getProperty("unionProp").isValid());
        assertEquals(format(ERR_UNION_INVALID, getEntityTitleAndDesc(UnionEntity.class).getKey(), format(ERR_DIRTY, one, getEntityTitleAndDesc(one).getKey())),
                     entity.getProperty("unionProp").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_are_allowed_for_property_annotated_with_SkipEntityExistsValidation() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void dirty_values_inside_union_are_allowed_for_property_annotated_with_SkipEntityExistsValidation() {
        final var one = save(new_(EntityOne.class, "ONE"))
                .setStringProperty("abc");
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp2(union);

        assertTrue(entity.getProperty("unionProp2").isValid());
        assertNotNull(entity.getUnionProp2());
    }

    @Test
    public void dirty_values_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipActiveOnly_is_true() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertFalse(sys.getProperty("thirdCategory").isValid());
        assertEquals(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()),
                     sys.getProperty("thirdCategory").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_inside_union_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipActiveOnly_is_true() {
        final var one = save(new_(EntityOne.class, "ONE"))
                .setStringProperty("abc");
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp3(union);

        assertFalse(entity.getProperty("unionProp3").isValid());
        assertEquals(format(ERR_UNION_INVALID, getEntityTitleAndDesc(UnionEntity.class).getKey(), format(ERR_DIRTY, one, getEntityTitleAndDesc(one).getKey())),
                     entity.getProperty("unionProp3").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_is_true() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat1);

        assertFalse(sys.getProperty("permitNewCategory").isValid());
        assertEquals(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()),
                     sys.getProperty("permitNewCategory").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_inside_union_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_is_true() {
        final var one = save(new_(EntityOne.class, "ONE"))
                .setStringProperty("abc");
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp4(union);

        assertFalse(entity.getProperty("unionProp4").isValid());
        assertEquals(format(ERR_UNION_INVALID, getEntityTitleAndDesc(UnionEntity.class).getKey(), format(ERR_DIRTY, one, getEntityTitleAndDesc(one).getKey())),
                     entity.getProperty("unionProp4").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_and_skipActiveOnly_are_true() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewAndSkipActiveOnlyCategory(cat1);

        assertFalse(sys.getProperty("permitNewAndSkipActiveOnlyCategory").isValid());
        assertEquals(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()),
                     sys.getProperty("permitNewAndSkipActiveOnlyCategory").getFirstFailure().getMessage());
    }

    @Test
    public void dirty_values_inside_union_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_and_skipActiveOnly_are_true() {
        final var one = save(new_(EntityOne.class, "ONE"))
                .setStringProperty("abc");
        final var union = new_(UnionEntity.class).setPropertyOne(one);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp5(union);

        assertFalse(entity.getProperty("unionProp5").isValid());
        assertEquals(format(ERR_UNION_INVALID, getEntityTitleAndDesc(UnionEntity.class).getKey(), format(ERR_DIRTY, one, getEntityTitleAndDesc(one).getKey())),
                     entity.getProperty("unionProp5").getFirstFailure().getMessage());
    }

    @Test
    public void non_persisted_entity_value_is_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_is_true() {
        final var cat42 = co$(TgCategory.class).new_().setKey("Cat42");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat42);

        assertTrue(sys.getProperty("permitNewCategory").isValid());
        assertNotNull(sys.getPermitNewCategory());
    }

    @Test
    public void non_persisted_entity_value_inside_union_is_allowed_if_property_and_active_union_member_are_annotated_with_SkipEntityExistsValidation_and_skipNew_is_true() {
        final var two = new_(EntityTwo.class, "22");
        final var union = new_(UnionEntity.class).setPropertyTwo(two);
        final var entity = new_(TgEntityWithManyPropTypes.class, "A").setUnionProp4(union);

        assertTrue(entity.getProperty("unionProp4").isValid());
        assertNotNull(entity.getUnionProp4());
    }

    @Test
    public void values_of_type_PropertyDescriptor_are_recognised_as_existent() {
        final PropertyDescriptor<TgCategory> pd = new PropertyDescriptor<>(TgCategory.class, "parent");
        final TgSystem sys = new_(TgSystem.class, "Sys2");
        sys.setPropDescriptor(pd);
        final MetaProperty<?> mp = sys.getProperty("propDescriptor");
        assertTrue(mp.isValid());
        assertEquals(pd, mp.getValue());
    }

    @Test
    public void uninstrumented_union_entity_with_existing_active_property__that_has_been_deleted__cannot_be_assigned() {
        final var w1 = save(new_(TgWorkshop.class, "W1"));
        final var bogie = new_(TgBogie.class);
        final var bogieLocation = new TgBogieLocation().setWorkshop(w1);

        co$(TgWorkshop.class).delete(w1);

        bogie.setLocation(bogieLocation);

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(format(ERR_UNION_INVALID,
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                   format("%s [%s] was not found.", getEntityTitleAndDesc(w1).getKey(), w1)));
    }

    @Test
    public void instrumented_union_entity_with_existing_active_property__that_has_been_deleted__cannot_be_assigned() {
        final var w1 = save(new_(TgWorkshop.class, "W1"));
        final var bogie = new_(TgBogie.class);
        final var bogieLocation = new_(TgBogieLocation.class).setWorkshop(w1);

        co$(TgWorkshop.class).delete(w1);

        bogie.setLocation(bogieLocation);

        // The error message does not contain "Union is invalid" because when an uninstrumented union is assigned,
        // it is first instrumented and then validated, which immediately yields an invalid result.
        // But when a union is instrumented prior to assignment, its validation had already been performed, and its result
        // is successful since it occurred before deletion.
        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(format("%s [%s] was not found.", getEntityTitleAndDesc(w1).getKey(), w1));
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

    private void union_entity_without_active_property_cannot_be_assigned(final Supplier<TgBogieLocation> locationSupplier) {
        final var bogie = new_(TgBogie.class);
        bogie.setLocation(locationSupplier.get());

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(format(ERR_UNION_INVALID,
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                   format("Required property [%s] is not specified for entity [%s].",
                                          getTitleAndDesc(KEY, TgBogieLocation.class).getKey(),
                                          getEntityTitleAndDesc(TgBogieLocation.class).getKey())));
    }

    @Test
    public void instrumented_union_entity_without_active_property_cannot_be_assigned() {
        union_entity_without_active_property_cannot_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_union_entity_without_active_property_cannot_be_assigned() {
        union_entity_without_active_property_cannot_be_assigned(TgBogieLocation::new);
    }

    private void union_entity_with_non_existing_active_property_cannot_be_assigned(final Supplier<TgBogieLocation> locationSupplier) {
        final var bogie = new_(TgBogie.class);
        final var workshop = new_(TgWorkshop.class, "W1");
        bogie.setLocation(locationSupplier.get().setWorkshop(workshop));

        assertThat(bogie.getProperty("location").getFirstFailure())
                .hasMessage(format(ERR_UNION_INVALID,
                                   getEntityTitleAndDesc(TgBogieLocation.class).getKey(),
                                   format("%s was not found.", getEntityTitleAndDesc(TgWorkshop.class).getKey())));
    }

    @Test
    public void instrumented_union_entity_with_non_existing_active_property_cannot_be_assigned() {
        union_entity_with_non_existing_active_property_cannot_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_union_entity_with_non_existing_active_property_cannot_be_assigned() {
        union_entity_with_non_existing_active_property_cannot_be_assigned(TgBogieLocation::new);
    }

    private void _valid_union_entity_can_be_assigned(final Supplier<TgBogieLocation> locationSupplier) {
        final var workshop = save(new_(TgWorkshop.class, "W1"));
        final var bogie = new_(TgBogie.class);
        bogie.setLocation(locationSupplier.get().setWorkshop(workshop));
        assertNull(bogie.getProperty("location").getFirstFailure());
    }

    @Test
    public void instrumented_valid_union_entity_can_be_assigned() {
        _valid_union_entity_can_be_assigned(() -> co$(TgBogieLocation.class).new_());
    }

    @Test
    public void uninstrumented_valid_union_entity_can_be_assigned() {
        _valid_union_entity_can_be_assigned(TgBogieLocation::new);
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
        skipEntityExistsNew_union_entity_with_skipEntityExistsNew_active_property_can_be_assigned(UnionEntityWithSkipExistsValidation::new);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

}
