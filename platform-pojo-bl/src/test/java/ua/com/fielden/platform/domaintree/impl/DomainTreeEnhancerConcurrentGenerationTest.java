package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.performConcurrentTypeGenerationTest;
import static ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.createFrom;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_PRECISION;
import static ua.com.fielden.platform.entity.annotation.IsProperty.DEFAULT_SCALE;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.AbstractWorkerForTypeGenerationTests;
import ua.com.fielden.platform.domaintree.testing.EnhancingMasterEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

/**
 * A test case to ensure correct concurrent generation of entity type using {@link DomainTreeEnhancer#createFrom(EntityFactory, java.util.Set, java.util.Map, java.util.Map)}.
 *
 * @author TG Team
 *
 */
public class DomainTreeEnhancerConcurrentGenerationTest {
    private static final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private static final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private static EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

    @Test
    public void concurrent_type_generation_resulting_in_the_same_type_is_supported() throws InterruptedException {
        performConcurrentTypeGenerationTest(index -> phaser -> numberOfErrors -> numberOfOtherErrors -> new Worker("Worker with phaser %s".formatted(index), phaser, numberOfErrors, numberOfOtherErrors));
    }

    private static class Worker extends AbstractWorkerForTypeGenerationTests {

        public Worker(final String name, final Phaser phaser, final AtomicInteger numberOfErrors, final AtomicInteger numberOfOtherErrors) {
            super(name, phaser, numberOfErrors, numberOfOtherErrors);
        }

        @Override
        public void doGenTypeWork() {
            final var customProp = new CustomProperty(EnhancingMasterEntity.class, EnhancingMasterEntity.class, "", "listProp", "List Prop", "List property", List.class, DEFAULT_PRECISION, DEFAULT_SCALE); // error 'There is no field delegate$qudc9a0 defined on class...' manifests only for Collection-typed properties
            createFrom(entityFactory, setOf(EnhancingMasterEntity.class), mapOf(), mapOf(t2(EnhancingMasterEntity.class, listOf(customProp)))); // we generate same types with exactly the same name on different threads
        }

    }

}