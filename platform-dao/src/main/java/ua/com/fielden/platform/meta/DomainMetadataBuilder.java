package ua.com.fielden.platform.meta;

import com.google.inject.Injector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toConcurrentMap;
import static ua.com.fielden.platform.meta.TypeRegistry.COMPOSITE_TYPES;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;
import static ua.com.fielden.platform.types.tuples.T2.t2;

public class DomainMetadataBuilder {

    private final DomainMetadataGenerator generator;
    private final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes;
    private final DbVersion dbVersion;
    private final Map<? extends Class, ? extends Class> hibTypesDefaults;
    private final Injector hibTypesInjector;

    public DomainMetadataBuilder(final @Nullable Map<? extends Class, ? extends Class> hibTypesDefaults,
                                 final Injector hibTypesInjector,
                                 final Collection<? extends Class<? extends AbstractEntity<?>>> entityTypes,
                                 final DbVersion dbVersion)
    {
        this.hibTypesDefaults = hibTypesDefaults;
        this.hibTypesInjector = hibTypesInjector;
        this.generator = new DomainMetadataGenerator(hibTypesInjector, hibTypesDefaults, dbVersion);
        this.entityTypes = entityTypes.stream().distinct().collect(toImmutableList());
        this.dbVersion = dbVersion;
    }

    public IDomainMetadata build() {
        final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap = entityTypes.parallelStream()
                .flatMap(type -> Try(() -> generator.forEntity(type))
                        .orElseThrow(e -> new DomainMetadataGenerationException(
                                format("Failed to generate metadata for entity [%s]", type.getTypeName()), e))
                        .map(em -> t2(type, em)).stream())
                .collect(toConcurrentMap(pair -> pair._1, pair -> pair._2));

        COMPOSITE_TYPES.parallelStream().forEach(type -> generator.forComposite(type).map(ctm -> t2(type, ctm)));

        return new DomainMetadataImpl(entityMetadataMap, entityTypes, generator, hibTypesInjector, hibTypesDefaults, dbVersion);
    }

}
