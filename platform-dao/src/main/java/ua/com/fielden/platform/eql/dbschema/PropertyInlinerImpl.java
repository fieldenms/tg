package ua.com.fielden.platform.eql.dbschema;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;

// TODO reduce visibility after configuring tests to use Injector
@Singleton
public final class PropertyInlinerImpl implements PropertyInliner {

    private final IDomainMetadata domainMetadata;

    @Inject
    public PropertyInlinerImpl(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /**
     * If a property can be inlined, returns a non-empty list of properties that replace it.
     * Otherwise, returns an empty optional.
     */
    @Override
    public Optional<List<PropertyMetadata.Persistent>> inline(final PropertyMetadata.Persistent property) {
        final var pmUtils = domainMetadata.propertyMetadataUtils();

        final var subProps = pmUtils.subProperties(property).stream()
                .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                .collect(toImmutableList());
        if (pmUtils.isPropEntityType(property, EntityMetadata::isUnion)) {
            return Optional.of(subProps);
        }
        else if (subProps.isEmpty()) {
            return Optional.empty();
        }
        else if (subProps.size() == 1) {
            return Optional.of(ImmutableList.of(subProps.getFirst()));
        }
        else {
            return Optional.of(subProps);
        }
    }

}
