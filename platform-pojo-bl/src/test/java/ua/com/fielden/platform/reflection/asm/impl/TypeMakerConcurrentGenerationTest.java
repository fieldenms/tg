package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.performConcurrentTypeGenerationTest;
import static ua.com.fielden.platform.reflection.asm.api.NewProperty.create;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;

import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.AbstractWorkerForTypeGenerationTests;
import ua.com.fielden.platform.entity.Entity;

/**
 * A test case to ensure correct concurrent generation of entity type using {@link TypeMaker#endModification()}.
 *
 * @author TG Team
 *
 */
public class TypeMakerConcurrentGenerationTest {

    @Test
    public void concurrent_type_generation_resulting_in_the_same_type_is_supported() throws InterruptedException {
        performConcurrentTypeGenerationTest(index -> phaser -> numberOfErrors -> numberOfOtherErrors -> new Worker("Worker with phaser %s".formatted(index), phaser, numberOfErrors, numberOfOtherErrors));
    }

    private static class Worker extends AbstractWorkerForTypeGenerationTests {

        public Worker(final String name, final Phaser phaser, final AtomicInteger numberOfErrors, final AtomicInteger numberOfOtherErrors) {
            super(name, phaser, numberOfErrors, numberOfOtherErrors);
        }

        @Override
        public void doGenTypeWork() throws Exception {
            startModification(Entity.class)
                .addProperties(create("newTestProp", List.class, "Gen Prop", "Generated Propery")) // error 'There is no field delegate$qudc9a0 defined on class...' manifests only for Collection-typed NewProperty properties
                .modifyTypeName(Entity.class.getName() + "WithPredefinedName") // we generate same types with exactly the same name on different threads
                .endModification();
        }

    }

}