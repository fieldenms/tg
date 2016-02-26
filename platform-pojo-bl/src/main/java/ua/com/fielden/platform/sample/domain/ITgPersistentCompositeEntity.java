package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.dao.IEntityDao;

import ua.com.fielden.platform.dao.IMasterDetailsDao;

/** 
 * Companion object for entity {@link TgPersistentCompositeEntity}.
 * 
 * @author Developers
 *
 */
public interface ITgPersistentCompositeEntity extends IEntityDao<TgPersistentCompositeEntity>, IMasterDetailsDao<TgPersistentEntityWithProperties, TgPersistentCompositeEntity> {

}