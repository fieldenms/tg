package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.meta.TypeRegistry.COMPOSITE_TYPES;

public class DomainMetadataBuilder {

    private final DomainMetadataGenerator generator;
    private final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes;

    public DomainMetadataBuilder(final HibernateTypeMappings hibernateTypeMappings,
                                 final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes,
                                 final IDbVersionProvider dbVersionProvider)
    {
        this.generator = new DomainMetadataGenerator(hibernateTypeMappings, dbVersionProvider);
        this.entityTypes = entityTypes.stream().distinct().collect(toImmutableList());
    }

    public IDomainMetadata build() {
        // pre-populate generator's cache
        entityTypes.parallelStream().forEach(generator::forEntity);
        COMPOSITE_TYPES.parallelStream().forEach(generator::forComposite);

        return new DomainMetadataImpl(generator);
    }

}
