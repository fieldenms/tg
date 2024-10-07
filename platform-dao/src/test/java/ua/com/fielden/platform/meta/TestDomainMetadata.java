package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.Assertions.PropertyA;

import static java.lang.String.format;

/**
 * A wrapper for {@link IDomainMetadata} for testing purposes.
 */
public class TestDomainMetadata {

    private final IDomainMetadata domainMetadata;

    private TestDomainMetadata(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    public static TestDomainMetadata wrap(final IDomainMetadata domainMetadata) {
        return new TestDomainMetadata(domainMetadata);
    }

    /**
     * Forwards to {@link IDomainMetadata#forPropertyOpt(Class, CharSequence)}} and asserts the result's presence.
     */
    public PropertyA<PropertyMetadata> forProperty(final Class<? extends AbstractEntity<?>> entityType, final CharSequence propPath) {
        final var propertyMetadata = domainMetadata.forPropertyOpt(entityType, propPath)
                                     .orElseThrow(() -> new AssertionError("Expected metadata to be generated for entity [%s].".formatted(entityType.getTypeName())));
        return PropertyA.of(propertyMetadata);
    }

}
