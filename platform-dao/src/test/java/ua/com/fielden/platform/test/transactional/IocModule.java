package ua.com.fielden.platform.test.transactional;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Module needed for instantiation of test case helper classes.
 * 
 * @author 01es
 * 
 */
public class IocModule extends AbstractModule {

    @Override
    protected void configure() {
        /*
         * This is a very interesting binding as it basically binds a generic declaration IEntityWithMoney, String>, which gets erased at runtime.
         * The use of TypeLiteral is a special Guice trick to allow for capturing of the type information.
         */
        bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
        }).to(EntityWithMoneyDao.class);

    }

}
