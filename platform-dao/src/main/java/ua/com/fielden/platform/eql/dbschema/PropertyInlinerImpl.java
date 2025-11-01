package ua.com.fielden.platform.eql.dbschema;

import com.google.common.collect.ImmutableList;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

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
        final var inlined = inline_(property);
        return inlined.isEmpty() ? empty() : of(inlined);
    }

    private @Nonnull List<PropertyMetadata.Persistent> inline_(final PropertyMetadata.Persistent property) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();

        final var subProps = pmUtils.subProperties(property).stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .collect(toImmutableList());
        if (property.type().isComponent() || pmUtils.isPropEntityType(property, EntityMetadata::isUnion)) {
            if (subProps.isEmpty()) {
                throw new DbSchemaException("Invalid property: sub-properties must not be empty. Property: %s".formatted(property));
            }
            return subProps;
        }
        else {
            return ImmutableList.of();
        }
    }

}
