package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class CommonEntityDaoCompanionInstantiationTest extends AbstractDaoTestCase {
    // TODO companion objects for entities like CentreConfigUpdater needs to be skipped here. Such companions are dependent on Web UI infrastructure and can not be checked in dao tests.
    // TODO This also needs to be deeper investigated in light of ICriteriaEntityRestorer interface.

    @Ignore
    @Test
    public void companion_objects_for_any_registered_domain_entity_can_be_instantiated_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final IEntityDao<?> randomCo = co$(PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size())));
        
        for (final Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co = randomCo.co$(type);
            assertNotNull(format("Companion object for entity [%s] could not have been instantiated.", type.getName()), co);
        }
    }

    @Test
    public void companion_objects_for_any_registered_domain_entity_are_cached_if_created_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final IEntityDao<?> randomCo = co$(PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size())));
        
        for (final Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co1 = randomCo.co$(type);
            final IEntityDao<?> co2 = randomCo.co$(type);
            assertTrue(format("Companion object for entity [%s] was not cached.", type.getName()), co1 == co2);
        }
    }

    @Ignore
    @Test
    public void the_cache_of_companion_objects_is_not_shared_between_different_instances_of_the_same_producing_companion_object() {
        final Random rnd = new Random();
        final Class<? extends AbstractEntity<?>> rndType = PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size()));
        final ICompanionObjectFinder coFinder = getInstance(ICompanionObjectFinder.class);
        final IEntityDao<?> randomCo1 = coFinder.find(rndType);
        final IEntityDao<?> randomCo2 = coFinder.find(rndType);
        
        assertFalse(randomCo1 == randomCo2);
        
        for (final Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co1 = randomCo1.co$(type);
            final IEntityDao<?> co2 = randomCo2.co$(type);
            assertFalse(format("Companion objects for entity [%s] produced by different companions should not be the same.", type.getName()), co1 == co2);
        }
    }
    
}