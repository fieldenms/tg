package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.meta.TypeRegistry.COMPONENT_TYPES;

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
        // Pre-populate generator's cache.
        // Do not use parallelStream() for processing as this leads to a deadlock due to loading of mutually dependent classes in parallel.
        // The deadlock happens upon initialisation of PropertyNature, which has several static fields.
        // See https://stackoverflow.com/questions/53682182/class-initialization-deadlock-mechanism-explanation for a discussion on this topic.
        entityTypes.forEach(generator::forEntity);
        COMPONENT_TYPES.forEach(generator::forComponent);

        return new DomainMetadataImpl(generator);
    }

}
