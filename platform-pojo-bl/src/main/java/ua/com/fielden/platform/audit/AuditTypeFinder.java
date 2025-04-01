package ua.com.fielden.platform.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import ua.com.fielden.platform.audit.exceptions.AuditingModeException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import javax.annotation.Nullable;
import java.util.*;

import static java.lang.String.format;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

final class AuditTypeFinder implements IAuditTypeFinder {

    private final AuditingMode auditingMode;
    /**
     * Key: audited type or audit type.
     * Value: context.
     */
    private final Map<Class<?>, Context<?>> contextMap;

    AuditTypeFinder(final Iterable<Class<?>> types, final AuditingMode auditingMode) {
        this.auditingMode = auditingMode;

        if (auditingMode == AuditingMode.DISABLED) {
            contextMap = ImmutableMap.of();
        }
        else {
            // { auditedType : [auditType] }
            final var auditedToAuditTypesMap = Streams.stream(types)
                    .filter(ty -> ty.isAnnotationPresent(AuditFor.class))
                    .collect(groupingBy(ty -> ty.getAnnotation(AuditFor.class).value()));

            final var contextMapBuilder = ImmutableMap.<Class<?>, Context<?>>builderWithExpectedSize(
                    auditedToAuditTypesMap.size() * 4);

            auditedToAuditTypesMap.forEach((auditedType, auditTypes) -> {
                final var context = makeContext(auditedType, auditTypes, auditingMode);
                contextMapBuilder.put(auditedType, context);
                auditTypes.forEach(auditType -> contextMapBuilder.put(auditType, context));
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
            throw new IllegalArgumentException(
                    format("Could not find audit types related to type [%s]. Ensure that the given type is either audited or is a discoverable audit type.",
                           baseType.getTypeName()));
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

    /**
     * @param _auditEntityTypes  sorted by version ascending
     * @param _auditPropTypes  sorted by version ascending
     */
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
                throw new IllegalStateException(format("Synthetic audit-entity type does not exist for audited type [%s].", _auditedType));
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
                throw new IllegalStateException(format("Synthetic audit-prop type does not exist for audited type [%s].", _auditedType));
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
                throw new IllegalStateException(format("Persistent audit-entity type does not exist for audited type [%s].", _auditedType.getSimpleName()));
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
                throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
            }
            if (version > _auditEntityTypes.size()) {
                throw new IllegalArgumentException("Persistent audit-entity type with version %s for [%s] does not exist.".formatted(version, _auditedType.getSimpleName()));

            }
            return _auditEntityTypes.get(version - 1);
        }

        @Override
        public Optional<Class<AbstractAuditEntity<E>>> findAuditEntityType(final int version) {
            if (version <= 0) {
                throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
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
                throw new IllegalStateException(format("Persistent audit-prop type does not exist for audited type [%s].", _auditedType.getSimpleName()));
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
                throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
            }
            if (version > _auditPropTypes.size()) {
                throw new IllegalArgumentException("Persistent audit-prop type with version %s for [%s] does not exist.".formatted(version, _auditedType.getSimpleName()));

            }
            return _auditPropTypes.get(version - 1);
        }

        @Override
        public Optional<Class<AbstractAuditProp<E>>> findAuditPropType(final int version) {
            if (version <= 0) {
                throw new IllegalArgumentException("Version must be greater than zero, but was %s.".formatted(version));
            }
            return version > _auditPropTypes.size()
                    ? Optional.empty()
                    : Optional.of(_auditPropTypes.get(version - 1));
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
                        throw new IllegalStateException("Expected an audit type, but the actual type was [%s].".formatted(ty.getTypeName()));
                    }
                }));

        final var auditEntityTypes = groups.getOrDefault(Kind.AUDIT_ENTITY, ImmutableList.of());
        final var auditPropTypes = groups.getOrDefault(Kind.AUDIT_PROP, ImmutableList.of());
        final var synAuditEntityGroup = groups.getOrDefault(Kind.SYN_AUDIT_ENTITY, List.of());
        final var synAuditPropGroup = groups.getOrDefault(Kind.SYN_AUDIT_PROP, List.of());

        if (auditingMode == AuditingMode.ENABLED && auditEntityTypes.isEmpty()) {
            throw new EntityDefinitionException("Audited type [%s] must have at least one persistent audit-entity type, but none were found.");
        }

        if (auditingMode == AuditingMode.ENABLED && auditPropTypes.isEmpty()) {
            throw new EntityDefinitionException("Audited type [%s] must have at least one persistent audit-prop type, but none were found.");
        }

        if (synAuditEntityGroup.size() > 1) {
            throw new EntityDefinitionException(format(
                    "Audited type [%s] cannot have more than one synthetic audit-entity type, but had %s: [%s].",
                    auditedType,
                    synAuditEntityGroup.size(),
                    synAuditEntityGroup.stream().map(Class::getSimpleName).collect(joining(", "))));
        }
        else if (auditingMode == AuditingMode.ENABLED && synAuditEntityGroup.isEmpty()) {
            throw new EntityDefinitionException("Audited type [%s] is missing a corresponding synthetic audit-entity type.");
        }

        if (synAuditPropGroup.size() > 1) {
            throw new EntityDefinitionException(format(
                    "Audited type [%s] cannot have more than one synthetic audit-prop type, but had %s: [%s].",
                    auditedType,
                    synAuditPropGroup.size(),
                    synAuditPropGroup.stream().map(Class::getSimpleName).collect(joining(", "))));
        }
        else if (auditingMode == AuditingMode.ENABLED && synAuditPropGroup.isEmpty()) {
            throw new EntityDefinitionException("Audited type [%s] is missing a corresponding synthetic audit-prop type.");
        }

        return new Context<E>(
                auditedType,
                ImmutableList.sortedCopyOf((Comparator) comparingLong(AuditUtils::getAuditTypeVersion),
                                           (List) auditEntityTypes),
                ImmutableList.sortedCopyOf((Comparator) comparingLong(AuditUtils::getAuditTypeVersion),
                                           (List) auditPropTypes),
                (Class) synAuditEntityGroup.stream().findFirst().orElse(null),
                (Class) synAuditPropGroup.stream().findFirst().orElse(null));
    }

}
