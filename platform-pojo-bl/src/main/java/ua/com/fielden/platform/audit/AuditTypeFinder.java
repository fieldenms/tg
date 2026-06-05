package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

/// Finds and provides navigation over audit types related to a given audited entity type.
///
/// This implementation scans the provided types and, depending on the [AuditingMode],
/// discovers and groups:
/// * synthetic audit-entity and audit-prop types,
/// * persistent audit-entity and audit-prop types,
/// * their versions and relationships to the audited entity type.
///
/// The resulting [IAuditTypeFinder.Navigator] instances expose a uniform API for:
/// * resolving the audited type;
/// * locating synthetic and persistent audit-entity / audit-prop types;
/// * selecting a specific audit type by version;
/// * obtaining all related audit types in a stable, version-aware order.
///
/// If auditing is disabled, this finder cannot be used and will throw [AuditingModeException]
/// upon navigation.
///
final class AuditTypeFinder implements IAuditTypeFinder {

    private static final String
            ERR_INCORRECT_AUDIT_TYPE_VERSIONS = """
                Incorrect audit type versions for audited type [%s].
                Expected: %s
                Actual audit-entity type versions: %s
                Actual audit-prop type versions: %s""",
            ERR_MUST_HAVE_ONE_SYN_AUDIT_ENTITY_TYPE = "Audited type [%s] must have exactly one synthetic audit-entity type, but had %s: [%s].",
            ERR_MUST_HAVE_ONE_SYN_AUDIT_PROP_TYPE = "Audited type [%s] must have exactly one synthetic audit-prop type, but had %s: [%s].",
            ERR_MUST_HAVE_AT_LEAST_ONE_PERSISTENT_AUDIT_ENTITY_TYPE = "Audited type [%s] must have at least one persistent audit-entity type, but none were found.",
            ERR_VERSION_MUST_BE_GREATER_THAN_ZERO = "Version must be greater than zero, but was %s.",
            ERR_NO_RELATED_AUDIT_TYPES = "Could not find audit types related to [%s]. Ensure that the given type is either audited or is a discoverable audit type.",
            ERR_SYN_AUDIT_ENTITY_TYPE_MISSING = "Synthetic audit-entity type does not exist for audited type [%s].",
            ERR_SYN_AUDIT_PROP_TYPE_MISSING = "Synthetic audit-prop type does not exist for audited type [%s].",
            ERR_AUDIT_ENTITY_TYPE_MISSING = "Persistent audit-entity type does not exist for audited type [%s].",
            ERR_AUDIT_ENTITY_TYPE_VERSION_MISSING = "Persistent audit-entity type with version %s for [%s] does not exist.",
            ERR_AUDIT_PROP_TYPE_MISSING = "Persistent audit-prop type does not exist for audited type [%s].",
            ERR_AUDIT_PROP_TYPE_VERSION_MISSING = "Persistent audit-prop type with version %s for [%s] does not exist.",
            ERR_UNEXPECTED_AUDIT_TYPE = "Expected an audit type, but the actual type was [%s].";

    private final AuditingMode auditingMode;

    /// Key: audited type or audit type.
    /// Value: context.
    private final Map<Class<?>, Context<?>> contextMap;

    AuditTypeFinder(final Iterable<Class<?>> types, final AuditingMode auditingMode) {
        this.auditingMode = auditingMode;

        if (auditingMode == AuditingMode.DISABLED) {
            contextMap = ImmutableMap.of();
        }
        else {
            // { auditedType : [auditType] (zero or more) }
            // Collect both the audited type and its audit types to ensure that empty [auditType] are also recorded.
            final var auditedToAuditTypesMap = Streams.stream(types)
                    .filter(ty -> isAudited(ty) || ty.isAnnotationPresent(AuditFor.class))
                    .collect(groupingBy(ty -> isAudited(ty) ? ty : ty.getAnnotation(AuditFor.class).value(),
                                        // Exclude the audited type from [auditType].
                                        filtering(not(AuditTypeFinder::isAudited), toList())));

            final var contextMapBuilder = ImmutableMap.<Class<?>, Context<?>>builderWithExpectedSize(
                    auditedToAuditTypesMap.size() * 4);

            // Skip audited types that are not actually audited.
            // This enables one to remove the @Audited annotation without having to delete audit types.
            auditedToAuditTypesMap.forEach((auditedType, auditTypes) -> {
                if (isAudited(auditedType)) {
                    final var context = makeContext((Class<? extends AbstractEntity<?>>) auditedType, auditTypes, auditingMode);
                    contextMapBuilder.put(auditedType, context);
                    auditTypes.forEach(auditType -> contextMapBuilder.put(auditType, context));
                }
            });

            this.contextMap = contextMapBuilder.buildOrThrow();
        }
    }

    private Navigator<?> _navigate(final Class<? extends AbstractEntity<?>> type) {
        if (auditingMode == AuditingMode.DISABLED) {
            throw AuditingModeException.cannotBeUsed(IAuditTypeFinder.class, auditingMode);
        }

        final var baseType = PropertyTypeDeterminator.baseEntityType(type);

        final var context = contextMap.get(baseType);
        if (context == null) {
            throw new InvalidArgumentException(ERR_NO_RELATED_AUDIT_TYPES.formatted(baseType.getTypeName()));
        }
        return context;
    }

    @Override
    public <E extends AbstractEntity<?>> Navigator<E> navigate(final Class<E> type) {
        return (Navigator<E>) _navigate(type);
    }

    @Override
    public <E extends AbstractEntity<?>> Navigator<E> navigateAudit(final Class<AbstractAuditEntity<E>> type) {
        return (Navigator<E>) _navigate(type);
    }

    @Override
    public <E extends AbstractEntity<?>> Navigator<E> navigateAuditProp(final Class<AbstractAuditProp<E>> type) {
        return (Navigator<E>) _navigate(type);
    }

    @Override
    public <E extends AbstractEntity<?>> Navigator<E> navigateSynAudit(final Class<AbstractSynAuditEntity<E>> type) {
        return (Navigator<E>) _navigate(type);
    }

    @Override
    public <E extends AbstractEntity<?>> Navigator<E> navigateSynAuditProp(final Class<AbstractSynAuditProp<E>> type) {
        return (Navigator<E>) _navigate(type);
    }

    /// @param _auditEntityTypes  sorted by version ascending
    /// @param _auditPropTypes  sorted by version ascending
    /// @param _synAuditEntityType  may be null if the auditing mode is [#GENERATION]
    /// @param _synAuditPropType  may be null if the auditing mode is [#GENERATION]
    ///
    private record Context<E extends AbstractEntity<?>> (
            Class<E> _auditedType,
            List<Class<AbstractAuditEntity<E>>> _auditEntityTypes,
            List<Class<AbstractAuditProp<E>>> _auditPropTypes,
            @Nullable Class<AbstractSynAuditEntity<E>> _synAuditEntityType,
            @Nullable Class<AbstractSynAuditProp<E>> _synAuditPropType)
            implements Navigator<E>
    {

        @Override
        public Class<E> auditedType() {
            return _auditedType;
        }

        @Override
        public Class<AbstractSynAuditEntity<E>> synAuditEntityType() {
            if (_synAuditEntityType == null) {
                throw new InvalidStateException(ERR_SYN_AUDIT_ENTITY_TYPE_MISSING.formatted(_auditedType));
            }
            return _synAuditEntityType;
        }

        @Override
        public Optional<Class<AbstractSynAuditEntity<E>>> findSynAuditEntityType() {
            return Optional.ofNullable(_synAuditEntityType);
        }

        @Override
        public Class<AbstractSynAuditProp<E>> synAuditPropType() {
            if (_synAuditPropType == null) {
                throw new InvalidStateException(ERR_SYN_AUDIT_PROP_TYPE_MISSING.formatted(_auditedType));
            }
            return _synAuditPropType;
        }

        @Override
        public Optional<Class<AbstractSynAuditProp<E>>> findSynAuditPropType() {
            return Optional.ofNullable(_synAuditPropType);
        }

        @Override
        public Collection<Class<AbstractAuditEntity<E>>> allAuditEntityTypes() {
            return _auditEntityTypes;
        }

        @Override
        public Class<AbstractAuditEntity<E>> auditEntityType() {
            if (_auditEntityTypes.isEmpty()) {
                throw new InvalidStateException(ERR_AUDIT_ENTITY_TYPE_MISSING.formatted(_auditedType.getSimpleName()));
            }

            return _auditEntityTypes.getLast();
        }

        @Override
        public Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType() {
            return _auditEntityTypes.isEmpty() ? Optional.empty() : Optional.of(_auditEntityTypes.getLast());
        }

        @Override
        public Class<AbstractAuditEntity<E>> auditEntityType(final int version) {
            if (version <= 0) {
                throw new InvalidArgumentException(ERR_VERSION_MUST_BE_GREATER_THAN_ZERO.formatted(version));
            }
            if (version > _auditEntityTypes.size()) {
                throw new InvalidArgumentException(ERR_AUDIT_ENTITY_TYPE_VERSION_MISSING.formatted(version, _auditedType.getSimpleName()));

            }
            return _auditEntityTypes.get(version - 1);
        }

        @Override
        public Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(final int version) {
            if (version <= 0) {
                throw new InvalidArgumentException(ERR_VERSION_MUST_BE_GREATER_THAN_ZERO.formatted(version));
            }
            return version > _auditEntityTypes.size()
                    ? Optional.empty()
                    : Optional.of(_auditEntityTypes.get(version - 1));
        }

        @Override
        public Collection<Class<AbstractAuditProp<E>>> allAuditPropTypes() {
            return _auditPropTypes;
        }

        @Override
        public Class<AbstractAuditProp<E>> auditPropType() {
            if (_auditPropTypes.isEmpty()) {
                throw new InvalidStateException(ERR_AUDIT_PROP_TYPE_MISSING.formatted(_auditedType.getSimpleName()));
            }

            return _auditPropTypes.getLast();
        }

        @Override
        public Optional<Class<AbstractAuditProp<E>>> findAuditPropType() {
            return _auditPropTypes.isEmpty() ? Optional.empty() : Optional.of(_auditPropTypes.getLast());
        }

        @Override
        public Class<AbstractAuditProp<E>> auditPropType(final int version) {
            if (version <= 0) {
                throw new InvalidArgumentException(ERR_VERSION_MUST_BE_GREATER_THAN_ZERO.formatted(version));
            }
            if (version > _auditPropTypes.size()) {
                throw new InvalidArgumentException(ERR_AUDIT_PROP_TYPE_VERSION_MISSING.formatted(version, _auditedType.getSimpleName()));

            }
            return _auditPropTypes.get(version - 1);
        }

        @Override
        public Optional<Class<AbstractAuditProp<E>>> findAuditPropType(final int version) {
            if (version <= 0) {
                throw new InvalidArgumentException(ERR_VERSION_MUST_BE_GREATER_THAN_ZERO.formatted(version));
            }
            return version > _auditPropTypes.size()
                    ? Optional.empty()
                    : Optional.of(_auditPropTypes.get(version - 1));
        }

        @Override
        public List<Class<? extends AbstractEntity<?>>> allPersistentAuditTypes() {
            return Stream.concat(_auditEntityTypes.stream(), _auditPropTypes.stream())
                    .sorted(comparing(AuditUtils::getAuditTypeVersion)
                                    .thenComparingInt(type -> AuditUtils.isAuditEntityType(type) ? 0 : 1))
                    .collect(toImmutableList());
        }

        @Override
        public Collection<Class<? extends AbstractEntity<?>>> allAuditTypes() {
            final var builder = ImmutableList.<Class<? extends AbstractEntity<?>>>
                    builderWithExpectedSize(2 + _auditEntityTypes.size() + _auditPropTypes.size());
            if (_synAuditEntityType != null)  {
                builder.add(_synAuditEntityType);
            }
            if (_synAuditPropType != null)  {
                builder.add(_synAuditPropType);
            }
            builder.addAll(_auditEntityTypes).addAll(_auditPropTypes);
            return builder.build();
        }

    }

    private <E extends AbstractEntity<?>> Context<E> makeContext(
            final Class<E> auditedType,
            final Collection<Class<?>> auditTypes,
            final AuditingMode auditingMode)
    {
        enum Kind { AUDIT_ENTITY, AUDIT_PROP, SYN_AUDIT_ENTITY, SYN_AUDIT_PROP };

        final var groups = auditTypes.stream()
                .collect(groupingBy(ty -> {
                    if (AuditUtils.isAuditEntityType(ty)) {
                        return Kind.AUDIT_ENTITY;
                    }
                    else if (AuditUtils.isAuditPropEntityType(ty)) {
                        return Kind.AUDIT_PROP;
                    }
                    else if (AuditUtils.isSynAuditEntityType(ty)) {
                        return Kind.SYN_AUDIT_ENTITY;
                    }
                    else if (AuditUtils.isSynAuditPropEntityType(ty)) {
                        return Kind.SYN_AUDIT_PROP;
                    }
                    else {
                        throw new InvalidStateException(ERR_UNEXPECTED_AUDIT_TYPE.formatted(ty.getTypeName()));
                    }
                }));

        final List<Class<AbstractAuditEntity<?>>> auditEntityTypes =
                ImmutableList.sortedCopyOf((Comparator) comparingLong(AuditUtils::getAuditTypeVersion),
                                           groups.getOrDefault(Kind.AUDIT_ENTITY, ImmutableList.of()));
        final List<Class<AbstractAuditProp<?>>> auditPropTypes =
                ImmutableList.sortedCopyOf((Comparator) comparingLong(AuditUtils::getAuditTypeVersion),
                                           groups.getOrDefault(Kind.AUDIT_PROP, ImmutableList.of()));
        final var synAuditEntityGroup = groups.getOrDefault(Kind.SYN_AUDIT_ENTITY, List.of());
        final var synAuditPropGroup = groups.getOrDefault(Kind.SYN_AUDIT_PROP, List.of());

        // If auditing is enabled, validate existence of audit types.
        if (auditingMode == AuditingMode.ENABLED) {
            if (auditEntityTypes.isEmpty()) {
                throw new EntityDefinitionException(ERR_MUST_HAVE_AT_LEAST_ONE_PERSISTENT_AUDIT_ENTITY_TYPE.formatted(auditedType.getSimpleName()));
            }

            final var expectedTypeVersions = IntStream.rangeClosed(1, auditEntityTypes.size()).toArray();
            final var auditEntityTypeVersions = auditEntityTypes.stream().mapToInt(AuditUtils::getAuditTypeVersion).toArray();
            final var auditPropTypeVersions = auditPropTypes.stream().mapToInt(AuditUtils::getAuditTypeVersion).toArray();
            if (!Arrays.equals(expectedTypeVersions, auditEntityTypeVersions) || !Arrays.equals(expectedTypeVersions, auditPropTypeVersions)) {
                throw new EntityDefinitionException(format(
                        ERR_INCORRECT_AUDIT_TYPE_VERSIONS,
                        auditedType.getTypeName(),
                        Arrays.toString(expectedTypeVersions),
                        Arrays.toString(auditEntityTypeVersions),
                        Arrays.toString(auditPropTypeVersions)));
            }

            if (synAuditEntityGroup.size() != 1) {
                throw new EntityDefinitionException(format(
                        ERR_MUST_HAVE_ONE_SYN_AUDIT_ENTITY_TYPE,
                        auditedType,
                        synAuditEntityGroup.size(),
                        synAuditEntityGroup.stream().map(Class::getTypeName).collect(joining(", "))));
            }

            if (synAuditPropGroup.size() != 1) {
                throw new EntityDefinitionException(format(
                        ERR_MUST_HAVE_ONE_SYN_AUDIT_PROP_TYPE,
                        auditedType,
                        synAuditPropGroup.size(),
                        synAuditPropGroup.stream().map(Class::getTypeName).collect(joining(", "))));
            }
        }

        return new Context<E>(auditedType,
                              (List) auditEntityTypes,
                              (List) auditPropTypes,
                              (Class) synAuditEntityGroup.stream().findFirst().orElse(null),
                              (Class) synAuditPropGroup.stream().findFirst().orElse(null));
    }

    private static boolean isAudited(final Class<?> type) {
        return AbstractEntity.class.isAssignableFrom(type) && AuditUtils.isAudited((Class<? extends AbstractEntity<?>>) type);
    }

}
