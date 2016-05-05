package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class CommonEntityDaoCompanionInstantiationTest extends AbstractDaoTestCase {

    @Test
    public void companion_objects_for_any_registered_domain_entity_can_be_instantiated_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final CommonEntityDao<?> randomCo = (CommonEntityDao<?>) ao(PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size())));
        
        for (Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co = randomCo.co(type);
            assertNotNull(format("Companion object for entity [%s] could not have been instantiated.", type.getName()), co);
        }
    }

    @Test
    public void companion_objects_for_any_registered_domain_entity_are_cached_if_created_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final CommonEntityDao<?> randomCo = (CommonEntityDao<?>) ao(PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size())));
        
        for (Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co1 = randomCo.co(type);
            final IEntityDao<?> co2 = randomCo.co(type);
            assertTrue(format("Companion object for entity [%s] was not cached.", type.getName()), co1 == co2);
        }
    }

    @Test
    public void the_cache_of_companion_objects_is_not_shared_between_different_instances_of_the_same_producing_companion_object() {
        final Random rnd = new Random();
        final Class<? extends AbstractEntity<?>> rndType = PlatformTestDomainTypes.entityTypes.get(rnd.nextInt(PlatformTestDomainTypes.entityTypes.size()));
        final CommonEntityDao<?> randomCo1 = (CommonEntityDao<?>) ao(rndType);
        final CommonEntityDao<?> randomCo2 = (CommonEntityDao<?>) ao(rndType);
        
        assertFalse(randomCo1 == randomCo2);
        
        for (Class<? extends AbstractEntity<?>> type: PlatformTestDomainTypes.entityTypes) {
            final IEntityDao<?> co1 = randomCo1.co(type);
            final IEntityDao<?> co2 = randomCo2.co(type);
            assertFalse(format("Companion objects for entity [%s] produced by different companions should not be the same.", type.getName()), co1 == co2);
        }
    }

    
}