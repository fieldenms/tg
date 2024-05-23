package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import static java.lang.String.format;

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
    public EntityMetadata forEntity(final Class<? extends AbstractEntity<?>> entityType) {
        return generator.forEntity(entityType)
                .orElseThrow(() -> new AssertionError(format("Expected metadata to be generated for entity [%s]", entityType)));
    }

}
