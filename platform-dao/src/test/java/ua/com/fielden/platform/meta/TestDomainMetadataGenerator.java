package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import static java.lang.String.format;
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
     */
    // raw AbstractEntity is used to accept generic types
    public EntityMetadata forEntity(final Class<? extends AbstractEntity> entityType) {
        // cast is required to type-check...
        return generator.forEntity((Class<? extends AbstractEntity<?>>) entityType)
                .orElseThrow(() -> new AssertionError(format("Expected metadata to be generated for entity [%s]",
                                                             entityType.getTypeName())));
    }

    // raw AbstractEntity is used to accept generic types
    public TestDomainMetadataGenerator assertNotGenerated(final Class<? extends AbstractEntity> entityType) {
        assertTrue(format("Did not expected metadata to be generated for entity [%s]", entityType.getTypeName()),
                   // cast is required to type-check...
                   generator.forEntity((Class<? extends AbstractEntity<?>>) entityType).isEmpty());
        return this;
    }

}
