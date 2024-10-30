package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;

public final class AuditUtils {

    public static boolean isAudited(final Class<? extends AbstractEntity<?>> type) {
        return isAnnotationPresentForClass(Audited.class, type);
    }

    /**
     * Locates and returns the audit-entity type for the specified entity type.
     * Throws an exception if the audit type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditEntity<E>> getAuditType(final Class<E> entityType) {
        // TODO Support multiple versions of audit-entity types
        final var auditTypeName = getAuditTypeName(entityType, 1);
        try {
            // TODO verify type is really audit-type
            return (Class<AbstractAuditEntity<E>>) entityType.getClassLoader().loadClass(auditTypeName);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    static String getAuditTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        final var simpleName = type.getSimpleName() + "_" + AbstractAuditEntity.A3T + "_" + version;
        return type.getPackageName() + "." + simpleName;
    }

    /**
     * Locates and returns the {@linkplain AbstractAuditProp audit-prop entity type} for the specified entity type.
     * Throws an exception if the audit-prop type cannot be located.
     * <p>
     * The return type of this method deliberately doesn't contain wildcards, as that would greatly reduce the ergonomics around its usage.
     */
    public static <E extends AbstractEntity<?>> Class<AbstractAuditProp<AbstractAuditEntity<E>>> getAuditPropType(final Class<E> entityType) {
        // TODO Support multiple versions of audit-entity types
        final var auditPropTypeName = getAuditPropTypeName(entityType, 1);
        try {
            return (Class<AbstractAuditProp<AbstractAuditEntity<E>>>) entityType.getClassLoader().loadClass(auditPropTypeName);
        } catch (final ClassNotFoundException e) {
            throw new InvalidArgumentException("Audit-prop entity type doesn't exist for entity type [%s]".formatted(entityType.getTypeName()), e);
        }
    }

    static String getAuditPropTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        return getAuditTypeName(type, version) + "_Prop";
    }

    private AuditUtils() {}

}
