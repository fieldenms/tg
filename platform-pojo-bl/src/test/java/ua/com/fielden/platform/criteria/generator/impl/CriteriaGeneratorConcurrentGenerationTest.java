package ua.com.fielden.platform.criteria.generator.impl;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.performConcurrentTypeGenerationTest;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator.generateCriteriaType;

import java.util.List;
import java.util.concurrent.Phaser;

import org.junit.Test;

import ua.com.fielden.platform.criteria.generator.impl.ConcurrentGenerationTestUtils.AbstractWorkerForTypeGenerationTests;
import ua.com.fielden.platform.sample.domain.crit_gen.TopLevelEntity;

/**
 * A test case to ensure correct concurrent generation of entity type using {@link CriteriaGenerator#generateCriteriaType(Class, List, Class)}.
 *
 * @author TG Team
 *
 */
public class CriteriaGeneratorConcurrentGenerationTest {

    @Test
    public void concurrent_type_generation_resulting_in_the_same_type_is_supported() throws InterruptedException {
        performConcurrentTypeGenerationTest((index, phaser) -> new Worker("Worker with phaser %s".formatted(index), phaser));
    }

    private static class Worker extends AbstractWorkerForTypeGenerationTests {

        public Worker(final String name, final Phaser phaser) {
            super(name, phaser);
        }

        @Override
        public void doGenTypeWork() {
            // we generate same types with exactly the same name on different threads;
            // error 'There is no field delegate$qudc9a0 defined on class...' manifests only for Collection-typed properties (criterion for "self" property has List type)
            generateCriteriaType(TopLevelEntity.class, asList("" /*aka "self" */), TopLevelEntity.class);
        }

    }

}