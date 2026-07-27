package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.audit.exceptions.AuditingRuntimeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.meta.EntityMetadata;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.meta.PropertyMetadataUtils.SubPropertyNaming;
import ua.com.fielden.platform.meta.PropertyTypeMetadata;
import ua.com.fielden.platform.meta.PropertyTypeMetadata.Component;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.BASE_PROPERTIES;
import static ua.com.fielden.platform.audit.AuditUtils.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.UNION_MEMBER;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;

@Singleton
final class SynAuditModelGenerator implements ISynAuditModelGenerator {

    public static final String ERR_NOT_SYNTHETIC_AUDIT_TYPE = "[%s] is not a synthetic audit type.";
    private final AuditingMode auditingMode;
    private final IAuditTypeFinder auditTypeFinder;
    private final IDomainMetadata domainMetadata;

    @Inject
    SynAuditModelGenerator(
            final AuditingMode auditingMode,
            final IAuditTypeFinder auditTypeFinder,
            final IDomainMetadata domainMetadata)
    {
        this.auditingMode = auditingMode;
        this.auditTypeFinder = auditTypeFinder;
        this.domainMetadata = domainMetadata;
    }

    @Override
    public <E extends AbstractEntity<?>> List<EntityResultQueryModel<E>> generate(final Class<E> synAuditEntityType) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(ISynAuditModelGenerator.class, auditingMode);
        }

        if (isSynAuditEntityType(synAuditEntityType)) {
            return generateSynAuditEntityModel((Class) synAuditEntityType);
        }
        else if (isSynAuditPropEntityType(synAuditEntityType)) {
            return generateSynAuditPropModel((Class) synAuditEntityType);
        }
        else {
            throw new AuditingRuntimeException(ERR_NOT_SYNTHETIC_AUDIT_TYPE.formatted(synAuditEntityType.getTypeName()));
        }
    }

    private <E extends AbstractEntity<?>> List<EntityResultQueryModel<AbstractSynAuditEntity<E>>> generateSynAuditEntityModel(
            final Class<AbstractSynAuditEntity<E>> synAuditEntityType)
    {
        final var navigator = auditTypeFinder.navigateSynAudit(synAuditEntityType);
        final Class<AbstractSynAuditProp<E>> synAuditPropType = navigator.synAuditPropType();
        // Ordered by version descending
        final var allAuditEntityTypes = navigator.allAuditEntityTypes()
                .stream()
                .sorted(comparing(AuditUtils::getAuditTypeVersion).reversed())
                .toList();

        final var models = allAuditEntityTypes.stream()
                .map(ty -> makeAuditEntitySourceQuery(synAuditEntityType, ty))
                .toArray(EntityResultQueryModel[]::new);

        // Explicit typing helps the compiler
        final IFromAlias<AbstractSynAuditEntity<E>> select = select(models);
        final var query = select.where()
                .critCondition(select(synAuditPropType)
                                       .where()
                                       .prop(AbstractSynAuditProp.AUDIT_ENTITY).eq().extProp(ID),
                               AbstractSynAuditProp.PROPERTY, AbstractSynAuditEntity.CHANGED_PROPS_CRIT)
                .model();
        return ImmutableList.of(query);
    }

    /// Builds a query with persistent audit-entity type `auditEntityType` as its source and synthetic audit-entity type `synAuditEntityType` as its result.
    /// The query will yield into all properties of `synAuditEntityType`.
    ///
    private <E extends AbstractEntity<?>> EntityResultQueryModel<AbstractSynAuditEntity<E>> makeAuditEntitySourceQuery(
            final Class<AbstractSynAuditEntity<E>> synAuditEntityType,
            final Class<AbstractAuditEntity<E>> auditEntityType)
    {
        // Yield active properties as is, yield null into inactive properties, yield ID + base properties as is
        final var synAuditEntityMetadata = domainMetadata.forEntity(synAuditEntityType);
        final var auditEntityMetadata = domainMetadata.forEntity(auditEntityType);

        // Partitioned on predicate "Is audit-property present and active in `auditEntityType`?"
        final var groups = synAuditEntityMetadata.properties()
                .stream()
                .filter(p -> p.has(AUDIT_PROPERTY))
                .collect(partitioningBy(p -> auditEntityMetadata.propertyOpt(p.name())
                                                .filter(currProp -> currProp.get(AUDIT_PROPERTY).orElseThrow().active())
                                                .isPresent(),
                                        flatMapping(this::expand, toList())));

        final var presentAndActiveProps = groups.get(true).stream().map(PropertyMetadata::name).toList();
        final var nullYields = makeNullYields(groups.get(false));

        return makeModel(auditEntityType,
                         synAuditEntityType,
                         concatList(List.of(ID), BASE_PROPERTIES, presentAndActiveProps),
                         nullYields);
    }

    /// Expands a property into its sub-properties, forming a full path from the parent property (e.g. `money.amount`).
    ///
    /// Expansion occurs only if the property’s type is a component type or a union entity type.
    /// Otherwise, the property is expanded to itself.
    ///
    /// This expansion is required to address an EQL limitation:
    /// when yielding a union-typed or component-typed property, all of its sub-properties must also be yielded explicitly.
    ///
    private Stream<PropertyMetadata> expand(final PropertyMetadata property) {
        return switch (property.type()) {
            case Component $ -> domainMetadata.propertyMetadataUtils().subProperties(property, SubPropertyNaming.PATH).stream();
            case PropertyTypeMetadata.Entity type
                    when domainMetadata.propertyMetadataUtils().isPropEntityType(type, EntityMetadata::isUnion)
                    -> domainMetadata.propertyMetadataUtils().subProperties(property, SubPropertyNaming.PATH)
                        .stream()
                        .filter(p -> p.has(UNION_MEMBER));
            default -> Stream.of(property);
        };
    }

    private <E extends AbstractEntity<?>> List<EntityResultQueryModel<AbstractSynAuditProp<E>>> generateSynAuditPropModel(
            final Class<AbstractSynAuditProp<E>> synAuditPropType)
    {
        final var navigator = auditTypeFinder.navigateSynAuditProp(synAuditPropType);
        final var synAuditEntityType = navigator.synAuditEntityType();

        return navigator.allAuditEntityTypes()
                .stream()
                .sorted(comparing(AuditUtils::getAuditTypeVersion).reversed())
                .map(auditEntityType -> {
                    final var auditPropType = navigator.auditPropType(getAuditTypeVersion(auditEntityType));
                    return select(auditPropType)
                            .yield().prop(ID).as(ID)
                            .yield()
                                .model(select(auditEntityType).where().prop(ID).eq().extProp(AbstractAuditProp.AUDIT_ENTITY + "." + ID).modelAsEntity(synAuditEntityType))
                                .as(AbstractSynAuditProp.AUDIT_ENTITY)
                            .yield().prop(AbstractAuditProp.PROPERTY).as(AbstractSynAuditProp.PROPERTY)
                            .modelAsEntity(synAuditPropType);
                })
                .collect(toImmutableList());
    }

    /// Builds a query using the specified yields.
    ///
    /// @param propYields the names of properties to be yielded directly, such as `yield().prop("key").as("key")`.
    /// @param valueYields A map of `{ alias : value }` pairs describing yields in the form `yield().val(value).as(alias)`.
    ///
    private static <E extends AbstractEntity<?>> EntityResultQueryModel<E> makeModel(
            final Class<? extends AbstractEntity<?>> sourceType,
            final Class<E> resultType,
            final Collection<String> propYields,
            final Map<String, Object> valueYields)
    {
        final var part = select(sourceType);

        // This part is a bit tricky because we need to yield at least one property to get to the right fluent interface
        if (!propYields.isEmpty()) {
            final var propYieldsIter = propYields.iterator();
            final var yield0 = propYieldsIter.next();
            return makeModel_(part.yield().prop(yield0).as(yield0),
                              propYieldsIter,
                              valueYields.entrySet().iterator())
                    .modelAsEntity(resultType);
        }
        else if (!valueYields.isEmpty()) {
            final var valueYieldsIter = valueYields.entrySet().iterator();
            final var yield0 = valueYieldsIter.next();
            return makeModel_(part.yield().val(yield0.getValue()).as(yield0.getKey()),
                              propYields.iterator(),
                              valueYieldsIter)
                    .modelAsEntity(resultType);
        }
        else {
            return part.modelAsEntity(resultType);
        }
    }

    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> makeModel_(
            ISubsequentCompletedAndYielded<E> part,
            final Iterator<String> propYieldsIter,
            final Iterator<Map.Entry<String, Object>> valueYieldsIter)
    {
        while (propYieldsIter.hasNext()) {
            final var next = propYieldsIter.next();
            part = part.yield().prop(next).as(next);
        }

        while (valueYieldsIter.hasNext()) {
            final var next = valueYieldsIter.next();
            part = part.yield().val(next.getValue()).as(next.getKey());
        }

        return part;
    }

    /// Creates a map of yields in the form `{ alias : value }`, where each `value` is initially `null`.
    /// If `null` is not suitable for a particular property, a default value is used instead.
    ///
    /// The exact definition of the _default value_ may evolve in future implementations.
    ///
    /// @param properties the properties for which null-based yields should be created
    ///
    private static Map<String, Object> makeNullYields(final Collection<? extends PropertyMetadata> properties) {
        if (properties.isEmpty()) {
            return ImmutableMap.of();
        }

        final var map = new HashMap<String, Object>(properties.size());

        properties.forEach(prop -> {
            final Object value;
            if (prop.type().javaType() == boolean.class) {
                value = false;
            }
            else {
                value = null;
            }
            map.put(prop.name(), value);
        });

        return unmodifiableMap(map);
    }

}
