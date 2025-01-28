package ua.com.fielden.platform.audit;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import javax.annotation.Nullable;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Static utilities for auditing.
 * <p>
 * To locate audit types for audited entity types, {@link IAuditTypeFinder} should be used.
 */
public final class AuditUtils {

    /**
     * This predicate is true for entity types that are audited.
     * This includes canonical entity types annotated with {@link Audited} and generated entity types based on them.
     */
    public static boolean isAudited(final Class<? extends AbstractEntity<?>> type) {
        return PropertyTypeDeterminator.baseEntityType(type).getDeclaredAnnotation(Audited.class) != null;
    }

    /**
     * The specified property name is assumed to be the name of an audited property.
     * This method constructs a name for a corresponding property of an audit-entity.
     */
    public static String auditPropertyName(final CharSequence auditedPropertyName) {
        return "a3t_" + auditedPropertyName;
    }

    /**
     * If the specified property name was constructed with {@link #auditPropertyName(CharSequence)}, this method works as an inverse,
     * and returns the name of the audited property.
     * Otherwise, {@code null} is returned.
     * <p>
     * In other words, given the name of a property of an audit-entity type, this method may return the name of a corresponding
     * property of an audited entity type.
     * <p>
     * It is an error if {@code auditedPropertyName} is {@code null} or blank.
     */
    public static @Nullable String auditedPropertyName(final CharSequence auditPropertyName) {
        if (auditPropertyName == null) {
            throw new InvalidArgumentException("Argument [auditPropertyName] must not be null.");
        }
        if (StringUtils.isBlank(auditPropertyName)) {
            throw new InvalidArgumentException("Argument [auditPropertyName] must not be blank.");
        }
        final var result = substringAfter(auditPropertyName.toString(), "a3t_");
        return result.isEmpty() ? null : result;
    }

    /**
     * Returns {@code true} if the specified property names an audit property (i.e., was constructed with {@link #auditPropertyName(CharSequence)}).
     */
    public static boolean isAuditProperty(final CharSequence property) {
        if (property == null) {
            throw new InvalidArgumentException("Argument [auditPropertyName] must not be null.");
        }
        return StringUtils.startsWith(property, "a3t_");
    }

    public static String getAuditTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        final var simpleName = type.getSimpleName() + "_" + AbstractAuditEntity.A3T + "_" + version;
        return type.getPackageName() + "." + simpleName;
    }

    public static boolean isAuditEntityType(final Class<?> type) {
        return AbstractAuditEntity.class.isAssignableFrom(type);
    }

    public static boolean isSynAuditEntityType(final Class<?> type) {
        return AbstractSynAuditEntity.class.isAssignableFrom(type);
    }

    public static boolean isSynAuditPropEntityType(final Class<?> type) {
        return AbstractSynAuditProp.class.isAssignableFrom(type);
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified audit-entity type using the class loader of the latter.
     * Returns an empty optional if the audit-prop type cannot be located.
     *
     * @see #getAuditPropTypeForAuditType(Class)
     */
    public static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Optional<Class<AP>> findAuditPropTypeForAuditType(final Class<AE> auditType)
    {
        try {
            return Optional.of(getAuditPropTypeForAuditTypeOrThrow(auditType));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified audit-entity type using the class loader of the latter.
     * <p>
     * It is an error if the audit-prop type cannot be located.
     *
     * @see #findAuditPropTypeForAuditType(Class)
     */
    public static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Class<AP> getAuditPropTypeForAuditType(final Class<AE> auditType)
    {
        try {
            return getAuditPropTypeForAuditTypeOrThrow(auditType);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-prop entity type doesn't exist for audit-entity type [%s]".formatted(auditType.getTypeName()), e);
        }
    }

    private static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Class<AP> getAuditPropTypeForAuditTypeOrThrow(final Class<AE> auditType)
            throws ClassNotFoundException
    {
        final var auditPropTypeName = auditType.getName() + "_Prop";
        return (Class<AP>) auditType.getClassLoader().loadClass(auditPropTypeName);
    }

    /**
     * Locates and returns a {@linkplain AbstractSynAuditProp synthetic audit-prop entity type} for the specified synthetic audit-entity type using the class loader of the latter.
     * Returns an empty optional if the requested type cannot be located.
     *
     * @see #getSynAuditPropTypeForSynAuditType(Class)
     */
    public static <AE extends AbstractSynAuditEntity<?>, AP extends AbstractSynAuditProp<AE>>
    Optional<Class<AP>> findSynAuditPropTypeForSynAuditType(final Class<AE> synAuditType)
    {
        try {
            return Optional.of(getSynAuditPropTypeForSynAuditTypeOrThrow(synAuditType));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Locates and returns a {@linkplain AbstractSynAuditProp synthetic audit-prop entity type} for the specified synthetic audit-entity type using the class loader of the latter.
     * <p>
     * It is an error if the requested type cannot be located.
     *
     * @see #findSynAuditPropTypeForSynAuditType(Class)
     */
    public static <AE extends AbstractSynAuditEntity<?>, AP extends AbstractSynAuditProp<AE>>
    Class<AP> getSynAuditPropTypeForSynAuditType(final Class<AE> synAuditType)
    {
        try {
            return getSynAuditPropTypeForSynAuditTypeOrThrow(synAuditType);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Synthetic audit-prop entity type doesn't exist for synthetic audit-entity type [%s]".formatted(synAuditType.getTypeName()), e);
        }
    }

    private static <AE extends AbstractSynAuditEntity<?>, AP extends AbstractSynAuditProp<AE>>
    Class<AP> getSynAuditPropTypeForSynAuditTypeOrThrow(final Class<AE> synAuditType)
            throws ClassNotFoundException
    {
        final var auditPropTypeName = synAuditType.getName() + "_Prop";
        return (Class<AP>) synAuditType.getClassLoader().loadClass(auditPropTypeName);
    }

    public static boolean isAuditPropEntityType(final Class<?> type) {
        return AbstractAuditProp.class.isAssignableFrom(type);
    }

    /**
     * Locates and returns the audit-entity type for the specified audit-prop entity type.
     * Returns an empty optional if the audit type cannot be located.
     * <p>
     * This method is the inverse of {@link #findAuditPropTypeForAuditType(Class)}.
     */
    public static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Optional<Class<AE>> findAuditTypeForAuditPropType(final Class<AP> auditEntity)
    {
        return Optional.ofNullable(getAuditTypeForAuditPropTypeOrNull(auditEntity));
    }

    /**
     * Locates and returns the audit-entity type for the specified audit-prop entity type.
     * <p>
     * It is an error if the audit type cannot be located.
     * <p>
     * This method is the inverse of {@link #getAuditPropTypeForAuditType(Class)}.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     */
    public static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Class<AE> getAuditTypeForAuditPropType(final Class<AP> auditPropType)
    {
        final var auditType = getAuditTypeForAuditPropTypeOrNull(auditPropType);
        if (auditType == null) {
            throw new EntityDefinitionException("Audit-prop entity [%s] is missing required annotation @%s"
                                                        .formatted(auditPropType.getTypeName(), AuditPropFor.class.getTypeName()));
        }
        return auditType;
    }

    private static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    @Nullable Class<AE> getAuditTypeForAuditPropTypeOrNull(final Class<AP> auditPropType)
    {
        return (Class<AE>) auditPropType.getAnnotation(AuditPropFor.class).value();
    }

    /**
     * Locates and returns the audited entity type for the specified audit-entity type.
     * <p>
     * It is an error if the audited type cannot be located.
     *
     * @see #getAuditedTypeForAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>>
    Class<E> getAuditedType(final Class<AE> auditType)
    {
        final var atAuditFor = auditType.getAnnotation(AuditFor.class);
        if (atAuditFor == null) {
            throw new EntityDefinitionException(format("Audit-entity [%s] is missing required annotation @%s",
                                                       auditType.getTypeName(), AuditFor.class.getTypeName()));
        }
        return (Class<E>) atAuditFor.value();
    }

    /**
     * Locates and returns the audited entity type for the specified audit-entity type.
     * <p>
     * It is an error if the audited type cannot be located.
     *
     * @see #getAuditedTypeForAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>, AE extends AbstractSynAuditEntity<E>>
    Class<E> getAuditedTypeForSyn(final Class<AE> synAuditType)
    {
        final var atAuditFor = synAuditType.getAnnotation(SynAuditFor.class);
        if (atAuditFor == null) {
            throw new EntityDefinitionException(format("Synthetic audit-entity [%s] is missing required annotation @%s",
                                                       synAuditType.getTypeName(), SynAuditFor.class.getTypeName()));
        }
        return (Class<E>) atAuditFor.value();
    }

    /**
     * Locates and returns the audited entity type for the specified audit-prop entity type.
     * <p>
     * It is an error if the audited type cannot be located.
     *
     * @see #getAuditedType(Class)
     */
    public static <E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>, AP extends AbstractAuditProp<AE>>
    Class<E> getAuditedTypeForAuditPropType(final Class<AP> auditPropType)
    {
        return getAuditedType(getAuditTypeForAuditPropType(auditPropType));
    }

    /**
     * Returns the version of the specified audit-entity type, which must be annotated with {@link AuditFor}.
     */
    public static int getAuditEntityTypeVersion(Class<? extends AbstractAuditEntity<?>> type) {
        final var atAuditFor = type.getAnnotation(AuditFor.class);
        if (atAuditFor == null) {
            throw new EntityDefinitionException(format("Audit-entity [%s] is missing required annotation @%s",
                                                       type.getTypeName(), AuditFor.class.getSimpleName()));
        }
        return atAuditFor.version();
    }

    /**
     * Returns the version of the specified audit-prop type, which must be associted with an audit-entity type.
     */
    public static int getAuditPropTypeVersion(Class<? extends AbstractAuditProp<?>> type) {
        return getAuditEntityTypeVersion(getAuditTypeForAuditPropType((Class) type));
    }

    private AuditUtils() {}

}
