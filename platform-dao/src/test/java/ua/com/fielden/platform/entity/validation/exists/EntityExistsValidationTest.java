package ua.com.fielden.platform.entity.validation.exists;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestIocModule;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashSet;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_DIRTY;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_UNION_INVALID;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

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
    public void existing_but_inactive_entity_cannot_be_assigned_to_property_of_activatable_entity_with_default_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] exists, but is not active.", result.getMessage());
    }

    @Test
    public void existing_but_inactive_entity_cannot_be_assigned_to_property_of_non_activatable_entity_with_default_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final var subSys = new_(TgSubSystem.class, "SubSys2").setFirstCategory(cat2);

        final Result result = subSys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals("Tg Category [Cat2] exists, but is not active.", result.getMessage());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_of_inactive_entity_with_default_validation() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat);
        assertTrue(sys.isValid().isSuccessful());
        assertTrue(sys.getProperty("category").isValid());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_of_active_entity_with_skipActiveOnly() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat);
        assertTrue(sys.getProperty("thirdCategory").isValid());
    }

    @Test
    public void existing_but_inactive_entity_can_be_assigned_to_property_of_criteria_entity_with_default_validation() {
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
    public void non_existing_entity_can_be_assigned_to_property_of_criteria_entity_with_default_validation() {
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
    public void dirty_values_are_allowed_for_property_annotated_with_SkipEntityExistsValidation() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
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
    public void dirty_values_are_not_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_is_true() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat1);

        assertFalse(sys.getProperty("permitNewCategory").isValid());
        assertEquals(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()),
                     sys.getProperty("permitNewCategory").getFirstFailure().getMessage());
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
    public void non_persisted_entity_value_is_allowed_for_property_annotated_with_SkipEntityExistsValidation_if_skipNew_is_true() {
        final var cat42 = co$(TgCategory.class).new_().setKey("Cat42");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat42);

        assertTrue(sys.getProperty("permitNewCategory").isValid());
        assertNotNull(sys.getPermitNewCategory());
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

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

}
