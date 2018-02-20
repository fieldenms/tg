package ua.com.fielden.platform.entity;

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
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.crit_gen.CriteriaGeneratorTestModule;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.SerialiserForDomainTreesTestingPurposes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class EntityExistValidationTest extends AbstractDaoTestCase {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final ClassProviderForTestingPurposes provider = new ClassProviderForTestingPurposes(TgSystem.class, TgCategory.class);
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    private final ISerialiser serialiser = new SerialiserForDomainTreesTestingPurposes(entityFactory, provider);
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


    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final TgCategory cat1 = save(new_(TgCategory.class, "Cat1").setActive(true));
        save(new_(TgCategory.class, "Cat2").setActive(false).setParent(cat1));
    }

}
