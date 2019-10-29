package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestModule;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.SerialiserForDomainTreesTestingPurposes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class EntityExistsValidationTest extends AbstractDaoTestCase {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final ClassProviderForTestingPurposes provider = new ClassProviderForTestingPurposes(TgSystem.class, TgCategory.class);
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    private final ISerialiser serialiser = new SerialiserForDomainTreesTestingPurposes(entityFactory, provider, DomainTreeEnhancerCache.CACHE);
    private final CentreDomainTreeManagerAndEnhancer cdtm = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<>(asList(TgSystem.class)));
    
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
    public void existing_but_inactive_entity_can_be_assigned_to_property_with_default_validation_on_criteria_entity() {
        cdtm.getFirstTick().check(TgSystem.class, "critOnlySingleCategory", true);
        final ICriteriaGenerator cg = injector.getInstance(ICriteriaGenerator.class);
        final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TgSystem, IEntityDao<TgSystem>> criteriaEntity = cg.generateCentreQueryCriteria(TgSystem.class, cdtm);
        
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
    public void non_existing_entity_can_be_assigned_to_property_with_default_validation_on_criteria_entity() {
        cdtm.getFirstTick().check(TgSystem.class, "critOnlySingleCategory", true);
        final ICriteriaGenerator cg = injector.getInstance(ICriteriaGenerator.class);
        final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, TgSystem, IEntityDao<TgSystem>> criteriaEntity = cg.generateCentreQueryCriteria(TgSystem.class, cdtm);
        
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

        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey(); 
        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(format(EntityExistsValidator.DIRTY_ERR, cat1, entityTitle), result.getMessage());
    }

    @Test
    public void skipped_entity_validation_does_not_restrict_dirty_entities() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setSecondCategory(cat1);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getSecondCategory());
    }

    @Test
    public void entity_exists_validation_with_skipActiveOnly_does_not_permit_dirty_entities() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setThirdCategory(cat1);

        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey(); 
        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(format(EntityExistsValidator.DIRTY_ERR, cat1, entityTitle), result.getMessage());
    }
    
    @Test
    public void entity_exists_validation_with_skipNew_permints_new_entity_values() {
        final TgCategory cat42 = co$(TgCategory.class).new_().setKey("Cat42");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat42);

        assertTrue(sys.isValid().isSuccessful());
        assertNotNull(sys.getPermitNewCategory());
    }

    @Test
    public void entity_exists_validation_with_skipNew_does_not_permit_persited_dirty_entities() {
        final TgCategory cat1 = co$(TgCategory.class).findByKey("Cat1");
        cat1.setDesc(cat1.getDesc() + "some change");
        final TgSystem sys = new_(TgSystem.class, "Sys2").setActive(true).setPermitNewCategory(cat1);

        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(cat1.getType()).getKey(); 
        final Result result = sys.isValid();
        assertFalse(result.isSuccessful());
        assertEquals(format(EntityExistsValidator.DIRTY_ERR, cat1, entityTitle), result.getMessage());
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
