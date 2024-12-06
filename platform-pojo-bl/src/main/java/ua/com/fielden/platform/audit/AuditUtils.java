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
     * Locates and returns the audit-entity type for the specified entity type using the specified class loader.
     * Returns an empty optional if the audit type cannot be located.
     *
     * @see #getAuditType(Class)
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditType(
            final Class<E> entityType,
            final ClassLoader classLoader)
    {
        try {
            return Optional.of(getAuditTypeOrThrow(entityType, classLoader));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Equivalent to {@link #findAuditType(Class, ClassLoader)} using the class loader of the specified entity.
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditType(final Class<E> entityType) {
        return findAuditType(entityType, entityType.getClassLoader());
    }

    /**
     * Locates and returns the audit-entity type for the specified entity using the specified class loader.
     * Throws an exception if the audit type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     *
     * @see #findAuditType(Class)
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditType(
            final Class<E> entityType,
            final ClassLoader classLoader)
    {
        try {
            return getAuditTypeOrThrow(entityType, classLoader);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    /**
     * Equivalent to {@link #getAuditType(Class, ClassLoader)} using the class loader of the specified entity type.
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditType(final Class<E> entityType) {
        return getAuditType(entityType, entityType.getClassLoader());
    }

    private static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditTypeOrThrow(
            final Class<E> entityType,
            final ClassLoader classLoader)
        throws ClassNotFoundException
    {
        // TODO Support multiple versions of audit-entity types
        final var auditTypeName = getAuditTypeName(entityType, 1);
        // TODO verify type is really audit-type
        return (Class<AbstractAuditEntity<E>>) classLoader.loadClass(auditTypeName);
    }

    static String getAuditTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        final var simpleName = type.getSimpleName() + "_" + AbstractAuditEntity.A3T + "_" + version;
        return type.getPackageName() + "." + simpleName;
    }

    public static boolean isAuditEntityType(final Class<?> type) {
        return AbstractAuditEntity.class.isAssignableFrom(type);
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified entity type using the specified class loader.
     * Returns an empty optional if the audit-prop type cannot be located.
     *
     * @see #getAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditProp<AbstractAuditEntity<E>>>> findAuditPropType(
            final Class<E> entityType,
            final ClassLoader classLoader)
    {
        try {
            return Optional.of(getAuditPropTypeOrThrow(entityType, classLoader));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Equivalent to {@link #findAuditPropType(Class, ClassLoader)} using the class loader of the specified entity type.
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditProp<AbstractAuditEntity<E>>>> findAuditPropType(final Class<E> entityType) {
        return findAuditPropType(entityType, entityType.getClassLoader());
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified entity type using the specified class loader.
     * Throws an exception if the audit-prop type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     *
     * @see #findAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropType(
            final Class<E> entityType,
            final ClassLoader classLoader)
    {
        try {
            return getAuditPropTypeOrThrow(entityType, classLoader);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-prop entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    /**
     * Equivalent to {@link #getAuditPropType(Class, ClassLoader)} using the class loader of the specified entity type.
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropType(final Class<E> entityType) {
        return getAuditPropType(entityType, entityType.getClassLoader());
    }

    private static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropTypeOrThrow(
            final Class<E> entityType,
            final ClassLoader classLoader)
        throws ClassNotFoundException
    {
        // TODO Support multiple versions of audit-entity types
        final var auditPropTypeName = getAuditPropTypeName(entityType, 1);
        return (Class<AbstractAuditProp<AbstractAuditEntity<E>>>) classLoader.loadClass(auditPropTypeName);
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
     * Throws an exception if the audit-prop type cannot be located.
     *
     * @see #findAuditPropType(Class)
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
        // TODO Support multiple versions of audit-entity types
        final var auditPropTypeName = auditType.getName() + "_Prop";
        return (Class<AP>) auditType.getClassLoader().loadClass(auditPropTypeName);
    }

    static String getAuditPropTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        return getAuditTypeName(type, version) + "_Prop";
    }

    public static boolean isAuditPropEntityType(final Class<?> type) {
        return AbstractAuditProp.class.isAssignableFrom(type);
    }

    /**
     * Locates and returns the audit-entity type for the specified audit-prop entity type.
     * Returns an empty optional if the audit type cannot be located.
     *
     * @see #getAuditTypeForAuditPropType(Class)
     */
    public static <AE extends AbstractAuditEntity<?>, AP extends AbstractAuditProp<AE>>
    Optional<Class<AE>> findAuditTypeForAuditPropType(final Class<AP> auditEntity)
    {
        return Optional.ofNullable(getAuditTypeForAuditPropTypeOrNull(auditEntity));
    }

    /**
     * Locates and returns the audit-entity type for the specified audit-prop entity type.
     * Throws an exception if the audit type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     *
     * @see #findAuditTypeForAuditPropType(Class)
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
     * Throws an exception if the audited type cannot be located.
     * <p>
     * This method is the inverse of {@link #getAuditType(Class)}.
     */
    public static <E extends AbstractEntity<?>, AE extends AbstractAuditEntity<E>>
    Class<E> getAuditedType(final Class<AE> auditType)
    {
        // TODO Support multiple versions of audit-entity types
        final var atAuditType = auditType.getAnnotation(AuditFor.class);
        if (atAuditType == null) {
            throw new EntityDefinitionException(format("Audit-entity [%s] is missing required annotation @%s",
                                                       auditType.getTypeName(), AuditFor.class.getTypeName()));
        }
        return (Class<E>) atAuditType.value();
    }

    /**
     * Locates and returns the audited entity type for the specified audit-prop entity type.
     * Throws an exception if the audited type cannot be located.
     * <p>
     * This method is the inverse of {@link #getAuditPropType(Class)}.
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
        return getAuditEntityTypeVersion(getAuditTypeForAuditPropType(type));
    }

    private AuditUtils() {}

}
