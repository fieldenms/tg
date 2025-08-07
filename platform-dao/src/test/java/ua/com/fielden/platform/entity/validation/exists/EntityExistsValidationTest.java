package ua.com.fielden.platform.entity.validation.exists;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestIocModule;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.HashSet;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.pd;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// This test case covers rules of the entity-exists validation.
///
/// @see EntityExistsValidator
/// @see SkipEntityExistsValidation
///
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
    public void existing_inactive_entity_cannot_be_assigned_to_property_of_activatable_entity_with_default_validation() {
        final var cat2 = co$(TgCategory.class).findByKey("Cat2");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        assertThat(sys.getProperty("firstCategory").getFirstFailure())
                .hasMessage(format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, getEntityTitleAndDesc(cat2).getKey(), cat2));
    }

    @Test
    public void existing_inactive_entity_cannot_be_assigned_to_property_of_non_activatable_entity_with_default_validation() {
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        final var subSys = new_(TgSubSystem.class, "SubSys2").setFirstCategory(cat2);

        assertThat(subSys.getProperty("firstCategory").getFirstFailure())
                .hasMessage(format(ERR_ENTITY_EXISTS_BUT_NOT_ACTIVE, getEntityTitleAndDesc(cat2).getKey(), cat2));
    }

    @Test
    public void existing_inactive_entity_can_be_assigned_to_property_of_inactive_entity_with_default_validation() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(false).setCategory(cat);
        assertTrue(sys.getProperty("category").isValid());
    }

    @Test
    public void existing_inactive_entity_can_be_assigned_to_property_of_active_entity_with_skipActiveOnly() {
        final var cat = save(new_(TgCategory.class, "CAT10").setActive(false));
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat);
        assertTrue(sys.getProperty("thirdCategory").isValid());
    }

    @Test
    public void existing_inactive_entity_can_be_assigned_to_property_of_criteria_entity_with_default_validation() {
        cdtm.getFirstTick().check(TgSystem.class, "critOnlySingleCategory", true);
        final var cg = injector.getInstance(ICriteriaGenerator.class);
        final var criteriaEntity = cg.generateCentreQueryCriteria(cdtm);
        
        final TgCategory cat2 = co$(TgCategory.class).findByKey("Cat2");
        criteriaEntity.set("tgSystem_critOnlySingleCategory", cat2);

        assertTrue(criteriaEntity.getProperty("tgSystem_critOnlySingleCategory").isValid());
    }
    
    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_default_validation() {
        final var cat2 = co$(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co$(TgCategory.class).delete(cat2);

        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat2);

        assertThat(sys.getProperty("firstCategory").getFirstFailure())
                .hasMessage(format(ERR_ENTITY_WAS_NOT_FOUND, getEntityTitleAndDesc(cat2).getKey(), cat2));
    }

    @Test
    public void new_entity_cannot_be_assigned_to_property_with_default_validation() {
        final var newCat = co(TgCategory.class).new_().setKey("NEW CAT");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(newCat);

        assertThat(sys.getProperty("firstCategory").getFirstFailure())
                .hasMessage(format(ERR_WAS_NOT_FOUND, getEntityTitleAndDesc(newCat).getKey()));
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

        assertTrue(criteriaEntity.getProperty("tgSystem_critOnlySingleCategory").isValid());
    }
    
    @Test
    public void existing_entity_can_be_assigned_to_property_with_default_SkipEntityExistsValidation() {
        final var cat2 = co$(TgCategory.class).findByKey("Cat2");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat2);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_can_be_assigned_to_property_with_default_SkipEntityExistsValidation() {
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(new_(TgCategory.class, "Cat3"));

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void non_existing_entity_cannot_be_assigned_to_property_with_skipActiveOnly() {
        final var cat2 = co$(TgCategory.class).findByKey("Cat2");

        // let's delete cat2 to make it non-existing
        co$(TgCategory.class).delete(cat2);

        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        assertThat(sys.getProperty("thirdCategory").getFirstFailure())
                .hasMessage(format(ERR_ENTITY_WAS_NOT_FOUND, getEntityTitleAndDesc(cat2).getKey(), cat2));
    }

    @Test
    public void existing_inactive_entity_can_be_assigned_to_property_with_skipActiveOnly() {
        final var cat2 = co$(TgCategory.class).findByKey("Cat2");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat2);

        assertTrue(sys.getProperty("thirdCategory").isValid());
    }

    @Test
    public void existing_active_entity_can_be_assigned_to_property_with_skipActiveOnly() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertTrue(sys.getProperty("thirdCategory").isValid());
    }

    @Test
    public void dirty_entity_cannot_be_assigned_to_property_with_default_validation() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setFirstCategory(cat1);

        assertThat(sys.getProperty("firstCategory").getFirstFailure())
                .hasMessage(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()));
    }

    @Test
    public void dirty_entity_can_be_assigned_to_property_with_default_SkipEntityExistsValidation() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat1);

        assertTrue(sys.getProperty("firstCategory").isValid());
    }

    @Test
    public void dirty_entity_cannot_be_assigned_to_property_with_skipActiveOnly() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        assertThat(sys.getProperty("thirdCategory").getFirstFailure())
                .hasMessage(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()));
    }

    @Test
    public void dirty_entity_cannot_be_assigned_to_property_with_skipNew() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat1);

        assertThat(sys.getProperty("permitNewCategory").getFirstFailure())
                .hasMessage(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()));
    }

    @Test
    public void dirty_entity_cannot_be_assigned_to_property_with_skipNew_and_skipActiveOnly() {
        final var cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewAndSkipActiveOnlyCategory(cat1);

        assertThat(sys.getProperty("permitNewAndSkipActiveOnlyCategory").getFirstFailure())
                .hasMessage(format(ERR_DIRTY, cat1, getEntityTitleAndDesc(cat1).getKey()));
    }

    @Test
    public void new_entity_can_be_assigned_to_property_with_skipNew() {
        final var cat42 = co$(TgCategory.class).new_().setKey("Cat42");
        final var sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat42);

        assertTrue(sys.getProperty("permitNewCategory").isValid());
    }

    @Test
    public void values_of_type_PropertyDescriptor_are_recognised_as_existent() {
        final var pd = pd(TgCategory.class, "parent");
        final var sys = new_(TgSystem.class, "Sys2").setPropDescriptor(pd);
        assertTrue(sys.getProperty("propDescriptor").isValid());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

}
