package ua.com.fielden.platform.eql.dbschema;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;

// TODO reduce visibility after configuring tests to use Injector
@Singleton
public final class PropertyInlinerImpl implements PropertyInliner {

    private final IDomainMetadata domainMetadata;

    @Inject
    public PropertyInlinerImpl(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    @Override
    public Optional<List<PropertyMetadata.Persistent>> inline(final PropertyMetadata.Persistent property) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();

        final var subProps = pmUtils.subProperties(property).stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .collect(toImmutableList());
        if (property.type().isComponent() || pmUtils.isPropEntityType(property, EntityMetadata::isUnion)) {
            if (subProps.isEmpty()) {
                throw new DbSchemaException(format("Invalid property: sub-properties must not be empty. Property: %s", property));
            }
            return Optional.of(subProps);
        }
        else {
            return Optional.empty();
        }
    }

}
