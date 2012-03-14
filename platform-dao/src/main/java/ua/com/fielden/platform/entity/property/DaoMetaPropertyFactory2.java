package ua.com.fielden.platform.entity.property;

import ua.com.fielden.platform.dao.factory.DaoFactory2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory2;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator2;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

import com.google.inject.Inject;

/**
 * DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class DaoMetaPropertyFactory2 extends AbstractMetaPropertyFactory2 {

    private final DaoFactory2 factory;

    @Inject
    public DaoMetaPropertyFactory2(final DaoFactory2 factory, final DomainValidationConfig domainConfig, final DomainMetaPropertyConfig domainMetaConfig) {
	super(domainConfig, domainMetaConfig);
	this.factory = factory;
    }

    @Override
    protected synchronized IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
	final Class<? extends AbstractEntity<?>> key = anotation.value();
	if (!entityExistsValidators.containsKey(key)) {
	    final IEntityDao2 dao = factory.newDao(key);
	    entityExistsValidators.put(key, new EntityExistsValidator2(dao));
	}

	return entityExistsValidators.get(key);
    }

}
