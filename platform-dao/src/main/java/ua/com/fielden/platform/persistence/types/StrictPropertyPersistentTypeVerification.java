package ua.com.fielden.platform.persistence.types;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;

/// Implements strict verification.
/// * For each component-typed property with a custom persistent type, it must match the globally configured persistent type.
///   This rule enforces the use of the same component type representation throughout the application.
///
public class StrictPropertyPersistentTypeVerification implements IPropertyPersistentTypeVerification {

    private final HibernateTypeMappings typeMappings;
    private final IDomainMetadata domainMetadata;

    @Inject
    protected StrictPropertyPersistentTypeVerification(
            final HibernateTypeMappings typeMappings,
            final IDomainMetadata domainMetadata)
    {
        this.typeMappings = typeMappings;
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Result verify(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
        final var mdProp = domainMetadata.forProperty(entityType, property);
        if (mdProp.type().isComponent()
            && mdProp.hibType() != null
            && typeMappings.getHibernateType(mdProp.type().javaType()).isPresent()
            && !mdProp.hibType().equals(typeMappings.getHibernateType(mdProp.type().javaType()).get()))
        {
            return failuref("Specified type [%s] does not match the globally configured type [%s].",
                            mdProp.hibType(),
                            typeMappings.getHibernateType(mdProp.type().javaType()).get());
        }
        else {
            return successful();
        }
    }

}
