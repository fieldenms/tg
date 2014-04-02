package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;
import ua.com.fielden.platform.swing.view.BaseFrame;

/**
 * Interface representing manager for all entity masters.
 * 
 * @author TG Team
 * 
 */
public interface IEntityMasterManager {

    /**
     * This method should show and return master frame for the specified entity.
     * 
     * @param <T>
     * @param <DAO>
     * @param entity
     * @param owner
     *            - view that requested this master and should be notified of entity changes, it may have a different type parameter.
     * @return
     */
    <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> BaseFrame showMaster(final T entity, final IUmViewOwner owner);

    /**
     * This method should show and return master frame for the specified entity.
     * 
     * @param <T>
     * @param <DAO>
     * @param entity
     * @param owner
     *            - view that requested this master and should be notified of entity changes, it may have a different type parameter.
     * @param postInitCallback
     *            - The call back that will be invoked after the master view was initialised.
     * @return
     */
    <T extends AbstractEntity<?>, DAO extends IEntityDao<T>> BaseFrame showMaster(final T entity, final IUmViewOwner owner, IPostInitCallback<T, DAO> postInitCallback);

    /**
     * This method should return entity producer, that is used by this manager to produce instances of {@code entityClass}
     * 
     * @param <T>
     * @param entityClass
     * @return
     */
    <T extends AbstractEntity<?>> IEntityProducer<T> getEntityProducer(Class<T> entityClass);

}
