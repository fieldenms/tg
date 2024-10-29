package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;

public final class AuditUtils {

    public static boolean isAudited(final Class<? extends AbstractEntity<?>> type) {
        return isAnnotationPresentForClass(Audited.class, type);
    }

    private AuditUtils() {}

}
