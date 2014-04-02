package ua.com.fielden.platform.reflection;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

import com.google.inject.Binder;

/**
 * A convenience class that assists in automation of binding companion object implementations to its declarations. It automatically determines the companion object contract based
 * on the type of an entity object.
 * 
 * @author TG Team
 * 
 */
public class CompanionObjectAutobinder {

    /**
     * Returns companion object type or null if its declaration is missing from the entity object.
     * 
     * @param entityType
     * @return
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> Class<T> companionObjectType(final Class<E> entityType) {
        if (entityType.isAnnotationPresent(CompanionObject.class)) {
            return (Class<T>) entityType.getAnnotation(CompanionObject.class).value();
        }
        return null;
    }

    /**
     * Uses the provided binder to bind RAO implementation in a singleton scope to entity's companion object contract.
     * 
     * @param entityType
     * @param binder
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> void bindRao(final Class<E> entityType, final Binder binder) {
        final Class<T> co = companionObjectType(entityType);

        if (co == null) { // check if the companion is declared
            throw new IllegalArgumentException("Entity object " + entityType.getSimpleName() + " is missing companion object declaration.");
        } else {
            // construct the RAO name
            final String name = entityType.getPackage().getName() + "." + co.getSimpleName().substring(1) + "Rao";
            // find class by name
            Class<? extends T> raoType;
            try {
                raoType = (Class<? extends T>) ClassesRetriever.findClass(name);
            } catch (final Exception ex) {
                throw new IllegalArgumentException("Could not find RAO implementation for companion object " + name + ".", ex);
            }
            // perform binding
            binder.bind(co).to(raoType); // as of recently should not be a singleton!

        }
    }

    /**
     * Uses the provided binder to bind DAO implementation to entity's companion object contract.
     * 
     * @param entityType
     * @param binder
     */
    public static <T extends IEntityDao<E>, E extends AbstractEntity<?>> void bindDao(final Class<E> entityType, final Binder binder) {
        final Class<T> co = companionObjectType(entityType);

        if (co == null) { // check if the companion is declared
            throw new IllegalArgumentException("Entity object " + entityType.getSimpleName() + " is missing companion object declaration.");
        } else {
            // construct the DAO name
            final String name = entityType.getPackage().getName() + "." + co.getSimpleName().substring(1) + "Dao";
            // find class by name
            Class<? extends T> daoType;
            try {
                daoType = (Class<? extends T>) ClassesRetriever.findClass(name);
            } catch (final Exception ex) {
                throw new IllegalArgumentException("Could not find DAO implementation for companion object " + name + ".", ex);
            }
            // perform binding
            binder.bind(co).to(daoType); // should never be a singleton!
        }
    }

}
