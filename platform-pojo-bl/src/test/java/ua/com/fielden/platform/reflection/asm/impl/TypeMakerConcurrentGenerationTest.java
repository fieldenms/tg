package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.performConcurrentTypeGenerationTest;
import static ua.com.fielden.platform.reflection.asm.api.NewProperty.create;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getCachedClass;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.AbstractWorkerForTypeGenerationTests;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;

/**
 * A test case to ensure correct concurrent generation of entity type using {@link TypeMaker#endModification()}.
 *
 * @author TG Team
 *
 */
public class TypeMakerConcurrentGenerationTest {
    private static final EntityTestIocModuleWithPropertyFactory module = new CommonEntityTestIocModuleWithPropertyFactory();
    private static final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private static EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

    @Test
    public void concurrent_type_generation_resulting_in_the_same_type_is_supported() throws InterruptedException {
        performConcurrentTypeGenerationTest((index, phaser) -> new Worker("Worker with phaser %s".formatted(index), phaser));
        
        final var genTypeOpt = getCachedClass(Entity.class.getName() + "WithPredefinedName");
        assertTrue(genTypeOpt.isPresent());

        final var genEntity = entityFactory.newEntity((Class<AbstractEntity>) genTypeOpt.get());

        // check collectional property initialisation (TypeMaker.ConstructorInterceptor):
        assertEquals(new ArrayList<>(), genEntity.get("newTestProp"));
        final var propVal = genEntity.get("newTestProp");
        assertNotSame(entityFactory.newEntity((Class<AbstractEntity>) genTypeOpt.get()).get("newTestProp"), propVal); // references for different entities must be different

        // check collectional property setting (TypeMaker.CollectionalSetterInterceptor):
        genEntity.set("newTestProp", listOf("item"));
        assertEquals(listOf("item"), genEntity.get("newTestProp"));
        assertSame(propVal, genEntity.get("newTestProp")); // reference must be the same -- the same list is used for collectional properties
    }

    private static class Worker extends AbstractWorkerForTypeGenerationTests {

        public Worker(final String name, final Phaser phaser) {
            super(name, phaser);
        }

        @Override
        public void doGenTypeWork() throws Exception {
            startModification(Entity.class)
                .addProperties(create("newTestProp", List.class, "Gen Prop", "Generated Propery", new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance())) // error 'There is no field delegate$qudc9a0 defined on class...' manifests only for Collection-typed NewProperty properties
                .modifyTypeName(Entity.class.getName() + "WithPredefinedName") // we generate same types with exactly the same name on different threads
                .endModification();
        }

    }

}