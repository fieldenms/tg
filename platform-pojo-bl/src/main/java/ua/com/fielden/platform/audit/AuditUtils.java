package ua.com.fielden.platform.audit;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import javax.annotation.Nullable;

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
        return PropertyTypeDeterminator.baseEntityType(type).isAnnotationPresent(Audited.class);
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
            throw new InvalidArgumentException("Argument [property] must not be null.");
        }
        return StringUtils.startsWith(property, "a3t_");
    }

    public static String getAuditTypeName(final Class<? extends AbstractEntity<?>> type, final int version) {
        final var simpleName = type.getSimpleName() + "_" + AbstractAuditEntity.A3T + "_" + version;
        return type.getPackageName() + "." + simpleName;
    }

    public static String getAuditTypeName(final CharSequence auditedTypeName, final int version) {
        return auditedTypeName + "_" + AbstractAuditEntity.A3T + "_" + version;
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

    public static boolean isAuditPropEntityType(final Class<?> type) {
        return AbstractAuditProp.class.isAssignableFrom(type);
    }

    /**
     * Returns the version of the specified audit type, which must be annotated with {@link AuditFor}.
     */
    public static int getAuditTypeVersion(Class<? extends AbstractEntity<?>> type) {
        final var atAuditFor = type.getAnnotation(AuditFor.class);
        if (atAuditFor == null) {
            throw new EntityDefinitionException(format("Audit type [%s] is missing required annotation @%s",
                                                       type.getTypeName(), AuditFor.class.getSimpleName()));
        }
        return atAuditFor.version();
    }

    private AuditUtils() {}

}
