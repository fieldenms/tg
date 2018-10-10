package ua.com.fielden.platform.dao;

import com.google.inject.ImplementedBy;

import ua.com.fielden.platform.persistence.types.EntityWithMoney;

@ImplementedBy(EntityWithMoneyDao.class)
public interface IEntityWithMoney extends IEntityDao<EntityWithMoney> {

}
