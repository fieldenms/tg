package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import javax.annotation.Nullable;
import java.util.Optional;

import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;

public final class AuditUtils {

    public static boolean isAudited(final Class<? extends AbstractEntity<?>> type) {
        return isAnnotationPresentForClass(Audited.class, type);
    }

    /**
     * Locates and returns the audit-entity type for the specified entity type.
     * Returns an empty optional if the audit type cannot be located.
     *
     * @see #getAuditType(Class)
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditEntity<E>>> findAuditType(final Class<E> entityType) {
        try {
            return Optional.of(getAuditTypeOrThrow(entityType));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Locates and returns the audit-entity type for the specified entity type.
     * Throws an exception if the audit type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     * 
     * @see #findAuditType(Class)
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditType(final Class<E> entityType) {
        try {
            return getAuditTypeOrThrow(entityType);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    private static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditTypeOrThrow(final Class<E> entityType)
            throws ClassNotFoundException
    {
        // TODO Support multiple versions of audit-entity types
        final var auditTypeName = getAuditTypeName(entityType, 1);
        // TODO verify type is really audit-type
        return (Class<AbstractAuditEntity<E>>) entityType.getClassLoader().loadClass(auditTypeName);
    }

    static String getAuditTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        final var simpleName = type.getSimpleName() + "_" + AbstractAuditEntity.A3T + "_" + version;
        return type.getPackageName() + "." + simpleName;
    }

    public static boolean isAuditEntityType(final Class<?> type) {
        return AbstractAuditEntity.class.isAssignableFrom(type);
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified entity type.
     * Returns an empty optional if the audit-prop type cannot be located.
     *
     * @see #getAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>> Optional<Class<AbstractAuditProp<AbstractAuditEntity<E>>>> findAuditPropType(final Class<E> entityType) {
        try {
            return Optional.of(getAuditPropTypeOrThrow(entityType));
        } catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified entity type.
     * Throws an exception if the audit-prop type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     *
     * @see #findAuditPropType(Class)
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropType(final Class<E> entityType) {
        try {
            return getAuditPropTypeOrThrow(entityType);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-prop entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    private static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropTypeOrThrow(final Class<E> entityType)
            throws ClassNotFoundException
    {
        // TODO Support multiple versions of audit-entity types
        final var auditPropTypeName = getAuditPropTypeName(entityType, 1);
        return (Class<AbstractAuditProp<AbstractAuditEntity<E>>>) entityType.getClassLoader().loadClass(auditPropTypeName);
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

    private AuditUtils() {}

}
