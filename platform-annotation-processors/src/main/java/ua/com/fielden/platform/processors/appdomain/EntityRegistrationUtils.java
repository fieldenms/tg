package ua.com.fielden.platform.processors.appdomain;

import ua.com.fielden.platform.annotations.appdomain.SkipEntityRegistration;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;

import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isAbstract;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isGeneric;

public final class EntityRegistrationUtils {

    public static boolean isRegisterable(final EntityElement entity) {
        return !isAbstract(entity) && canBeRegistered(entity) && !shouldSkipRegistration(entity);
    }

    public static boolean shouldSkipRegistration(final EntityElement entity) {
        return entity.getAnnotation(SkipEntityRegistration.class) != null;
    }

    public static boolean canBeRegistered(final EntityElement entity) {
        // generic entity types cannot be registered due to type restrictions of IApplicationDomainProvider#entityTypes
        return !isGeneric(entity);
    }

    private EntityRegistrationUtils() {}

}
