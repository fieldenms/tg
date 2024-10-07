package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import static org.junit.Assert.assertTrue;

/**
 * A wrapper for {@link DomainMetadataGenerator} for testing purposes.
 */
class TestDomainMetadataGenerator {

    private final DomainMetadataGenerator generator;

    private TestDomainMetadataGenerator(final DomainMetadataGenerator generator) {
        this.generator = generator;
    }

    public static TestDomainMetadataGenerator wrap(final DomainMetadataGenerator generator) {
        return new TestDomainMetadataGenerator(generator);
    }

    /**
     * Forwards to {@link DomainMetadataGenerator#forEntity(Class)} and asserts the result's presence.
     * <p>
     * A raw {@link AbstractEntity} is used as a parameter for {@code entityType} to make the compiler happy in accepting generic types.
     */
    public EntityMetadata forEntity(final Class<? extends AbstractEntity> entityType) {
        // cast is required to type-check...
        return generator.forEntity((Class<? extends AbstractEntity<?>>) entityType)
                .orElseThrow(() -> new AssertionError("Expected metadata to be generated for entity [%s].".formatted(entityType.getTypeName())));
    }

    /**
     * Forwards to {@link DomainMetadataGenerator#forEntity(Class)} and asserts the result's absence.
     * <p>
     * A raw {@link AbstractEntity} is used as a parameter for {@code entityType} to make the compiler happy in accepting generic types.
     */
    public TestDomainMetadataGenerator assertNotGenerated(final Class<? extends AbstractEntity> entityType) {
        assertTrue("Did not expected metadata to be generated for entity [%s].".formatted(entityType.getTypeName()),
                   // cast is required to type-check...
                   generator.forEntity((Class<? extends AbstractEntity<?>>) entityType).isEmpty());
        return this;
    }

}
