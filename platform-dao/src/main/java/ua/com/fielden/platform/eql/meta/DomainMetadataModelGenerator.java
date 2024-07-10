package ua.com.fielden.platform.eql.meta;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.eql.dbschema.PropertyInliner;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.LongFunction;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.eql.meta.DomainTypeData.domainTypeData;
import static ua.com.fielden.platform.eql.meta.PersistDomainMetadataModel.CRITERION;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.entityTypeHierarchy;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.utils.StreamUtils.*;

public final class DomainMetadataModelGenerator {

    private final IDomainMetadata domainMetadata;
    private final PropertyInliner propertyInliner;

    public DomainMetadataModelGenerator(final IDomainMetadata domainMetadata, final PropertyInliner propertyInliner) {
        this.domainMetadata = domainMetadata;
        this.propertyInliner = propertyInliner;
    }

    public Map<Class<?>, DomainTypeData> generateDomainTypesData(final Set<Class<? extends AbstractEntity<?>>> entityTypes) {
        // helper type for intermediate results
        // f is called with an ID
        record H (Class<?> type, LongFunction<DomainTypeData> f) {}

        final Stream<H> hs = entityTypes.stream().flatMap(entityType -> {
            final var em = domainMetadata.forEntity(entityType);
            final List<? extends PropertyMetadata> props = em.properties().stream()
                    .filter(pm -> !pm.name().equals(VERSION) && !pm.type().isCollectional() && !pm.type().isCompositeKey()
                                  && !(pm.isPlain() && em.isPersistent()))
                    .toList();

            final Stream<H> entityTypeS = Stream.of(
                    new H(entityType, id -> {
                        final var typeTitleAndDesc = getEntityTitleAndDesc(entityType);
                        final Optional<EntityMetadata.Persistent> persistentBase = persistentBaseForSynthetic(em);
                        final Optional<String> tableName = persistentBase.or(em::asPersistent).map(pem -> pem.data().tableName());
                        return domainTypeData(entityType, persistentBase.map(EntityMetadata::javaType).orElse(null),
                                              id, entityType.getName(), typeTitleAndDesc.getKey(),
                                              true, tableName.orElse(null), typeTitleAndDesc.getValue(), props.size(),
                                              domainMetadata.entityMetadataUtils().compositeKeyMembers(em),
                                              props);
                    }));

            final Stream<H> propsS = props.stream().map(prop -> {
                final Optional<Class<?>> optPropJavaType = switch (prop.type()) {
                    case PropertyTypeMetadata.Composite it -> Optional.of(it.javaType());
                    case PropertyTypeMetadata.Primitive it -> Optional.of(it.javaType());
                    case PropertyTypeMetadata.Entity it when domainMetadata.forType(it.javaType()).isEmpty()
                                                             || !entityTypes.contains(it.javaType())
                            -> Optional.of(it.javaType());
                    default -> Optional.empty();
                };
                return optPropJavaType.map(propJavaType -> new H(propJavaType, id -> {
                    final List<PropertyMetadata.Persistent> subItems = domainMetadata.propertyMetadataUtils().subProperties(prop).stream()
                            .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                            .toList();

                    final int propsCount = !subItems.isEmpty() && !(Money.class.equals(propJavaType) && subItems.size() == 1)
                            ? subItems.size() : 0;
                    final var subTypeTitleAndDesc = isUnionEntityType(propJavaType)
                            ? getEntityTitleAndDesc((Class<? extends AbstractUnionEntity>) propJavaType)
                            : null;
                    final String title = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getKey() : propJavaType.getSimpleName();
                    final String titleDesc = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getValue() : propJavaType.getSimpleName();

                    return domainTypeData(propJavaType, null, id, propJavaType.getName(), title, false,
                                          null, titleDesc, propsCount, ImmutableList.of(), ImmutableList.of());
                }));
            }).flatMap(Optional::stream);

            return Stream.concat(entityTypeS, propsS);
        });

        return collectToImmutableMap(distinct(hs, H::type),
                                     longs(1),
                                     (h, i) -> h.type(),
                                     (h, i) -> h.f().apply(i));
    }

    /**
     * If given a synthetic-based-on-persistent entity, returns the persistent type it's based on.
     */
    private Optional<EntityMetadata.Persistent> persistentBaseForSynthetic(final EntityMetadata em) {
        return em.asSynthetic()
                .map(EntityMetadata::javaType)
                .map(entityType -> entityTypeHierarchy(entityType, false).skip(1))
                .orElseGet(Stream::empty)
                .flatMap(entityType -> domainMetadata.forEntityOpt(entityType).flatMap(EntityMetadata::asPersistent).stream())
                .findFirst();
    }

    public List<DomainPropertyData> generateDomainPropsData(final Map<Class<?>, DomainTypeData> typesMap) {
        final List<DomainPropertyData> result = new ArrayList<>();

        long id = typesMap.size();
        for (final DomainTypeData entityType : typesMap.values()) {
            if (!entityType.isEntity()) {
                continue;
            }
            int position = 0;
            for (final PropertyMetadata pm : entityType.props().values()) {
                if (isExcluded(entityType.type(), pm.name())) {
                    continue;
                }

                id = id + 1;
                position = position + 1;
                final Pair<String, String> prelTitleAndDesc = getTitleAndDesc(pm.name(), entityType.type());
                final String prelTitle = prelTitleAndDesc.getKey();
                final String prelDesc = prelTitleAndDesc.getValue();

                final var propJavaType = (Class<?>) pm.type().javaType();
                final DomainTypeData superTypeDtd = typesMap.get(entityType.superType());
                final var domainPropertyData = new DomainPropertyData(
                        id,
                        pm.name(),
                        entityType,
                        null,
                        typesMap.get(propJavaType),
                        prelTitle,
                        prelDesc,
                        entityType.getKeyMemberIndex(pm.name()),
                        pm.is(REQUIRED),
                        determinePropColumn(ofNullable(superTypeDtd).flatMap(it -> it.getProperty(pm.name())).orElse(pm)),
                        position);
                result.add(domainPropertyData);

                // subproperties
                if (pm.isPersistent()) {
                    final var ppm = pm.asPersistent().orElseThrow();
                    final var optSubProps = propertyInliner.inline(ppm)
                            // ignore single-component composite types, they are treated as primitive types
                            .filter(props -> !ppm.type().isComposite() || props.size() > 1);
                    if (optSubProps.isPresent()) {
                        int subItemPosition = 0;
                        for (final var subProp : optSubProps.get().stream().flatMap(spm -> spm.asPersistent().stream()).toList()) {
                            id = id + 1;
                            subItemPosition = subItemPosition + 1;
                            final var titleAndDesc = getTitleAndDesc(subProp.name(), propJavaType);
                            result.add(new DomainPropertyData(id,
                                                              subProp.name(),
                                                              null,
                                                              domainPropertyData,
                                                              typesMap.get((Class<?>) subProp.type().javaType()),
                                                              titleAndDesc.getKey(),
                                                              titleAndDesc.getValue(),
                                                              null,
                                                              false,
                                                              subProp.data().column().name,
                                                              subItemPosition));
                        }
                    }
                }
            }
        }

        return result;
    }

    private @Nullable String determinePropColumn(final PropertyMetadata pm) {
        return switch (pm) {
            case PropertyMetadata.CritOnly $ -> CRITERION;
            case PropertyMetadata.Persistent it -> propertyInliner.inline(it)
                    .filter(ps -> ps.size() == 1)
                    .map(List::getFirst)
                    .map(prop -> prop.data().column().name)
                    .orElse(null);
            default -> null;
        };
    }

}
