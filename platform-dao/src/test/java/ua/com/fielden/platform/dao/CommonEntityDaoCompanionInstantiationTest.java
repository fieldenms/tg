package ua.com.fielden.platform.dao;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.domain.metadata.DomainPropertyTreeEntity;
import ua.com.fielden.platform.domain.metadata.DomainTreeEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyEntry;
import ua.com.fielden.platform.ref_hierarchy.ReferenceLevelHierarchyEntry;
import ua.com.fielden.platform.ref_hierarchy.ReferencedByLevelHierarchyEntry;
import ua.com.fielden.platform.ref_hierarchy.TypeLevelHierarchyEntry;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public class CommonEntityDaoCompanionInstantiationTest extends AbstractDaoTestCase {

    private static final Set<Class<? extends AbstractEntity<?>>> types = setOf(ReferenceHierarchy.class, ReferenceHierarchyEntry.class, TypeLevelHierarchyEntry.class, ReferencedByLevelHierarchyEntry.class, ReferenceLevelHierarchyEntry.class,
                                                                               DomainTreeEntity.class, DomainPropertyTreeEntity.class, Action.class);
    private static final List<Class<? extends AbstractEntity<?>>> entityTypes = PlatformTestDomainTypes.entityTypes.stream().filter(type -> !types.contains(type)).collect(toList());

    @Test
    public void companion_objects_for_any_registered_domain_entity_can_be_instantiated_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final IEntityDao<?> randomCo = co$(entityTypes.get(rnd.nextInt(entityTypes.size())));

        entityTypes.stream().forEach(type -> {
            final IEntityDao<?> co = randomCo.co$(type);
            assertNotNull(format("Companion object for entity [%s] could not have been instantiated.", type.getName()), co);
        });
    }

    @Test
    public void companion_objects_for_any_registered_domain_entity_are_cached_if_created_through_co_API_of_random_companion() {
        final Random rnd = new Random();
        final IEntityDao<?> randomCo = co$(entityTypes.get(rnd.nextInt(entityTypes.size())));

        entityTypes.stream().forEach(type -> {
            final IEntityDao<?> co1 = randomCo.co$(type);
            final IEntityDao<?> co2 = randomCo.co$(type);
            assertTrue(format("Companion object for entity [%s] was not cached.", type.getName()), co1 == co2);
        });
    }

    @Test
    public void the_cache_of_companion_objects_is_not_shared_between_different_instances_of_the_same_producing_companion_object() {
        final Random rnd = new Random();
        final Class<? extends AbstractEntity<?>> rndType = entityTypes.get(rnd.nextInt(entityTypes.size()));
        final ICompanionObjectFinder coFinder = getInstance(ICompanionObjectFinder.class);
        final IEntityDao<?> randomCo1 = coFinder.find(rndType);
        final IEntityDao<?> randomCo2 = coFinder.find(rndType);

        assertFalse(randomCo1 == randomCo2);

        entityTypes.stream().forEach(type -> {
            final IEntityDao<?> co1 = randomCo1.co$(type);
            final IEntityDao<?> co2 = randomCo2.co$(type);
            assertFalse(format("Companion objects for entity [%s] produced by different companions should not be the same.", type.getName()), co1 == co2);
        });
    }

}