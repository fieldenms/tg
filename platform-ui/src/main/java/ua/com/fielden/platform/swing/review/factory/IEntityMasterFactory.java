/**
 *
 */
package ua.com.fielden.platform.swing.review.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

/**
 * Interface for creating master frames for entities of specific type.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public interface IEntityMasterFactory<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> {

    /**
     * This method should create and return master frame for the specified {@code entity}. Other parameters may be used for specific purposes in different masters.
     *
     * @param entityProducer
     * @param cache
     * @param entity
     * @param vmf
     * @param masterManager
     * @param ownerView
     * @return
     */
    BaseFrame createMasterFrame(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final IValueMatcherFactory vmf, final IMasterDomainTreeManager masterManager, IUmViewOwner ownerView);

}