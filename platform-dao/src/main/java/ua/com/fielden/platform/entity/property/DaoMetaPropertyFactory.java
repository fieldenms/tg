package ua.com.fielden.platform.entity.property;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

import com.google.inject.Inject;

/**
 * DAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class DaoMetaPropertyFactory extends AbstractMetaPropertyFactory {

    private final DaoFactory factory;

    @Inject
    public DaoMetaPropertyFactory(final DaoFactory factory, final DomainValidationConfig domainConfig, final DomainMetaPropertyConfig domainMetaConfig) {
	super(domainConfig, domainMetaConfig);
	this.factory = factory;
    }

    @Override
    protected synchronized IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
	final Class<? extends AbstractEntity> key = anotation.value();
	if (!entityExistsValidators.containsKey(key)) {
	    final IEntityDao dao = factory.newDao(key);
	    entityExistsValidators.put(key, new EntityExistsValidator(dao));
	}

	return entityExistsValidators.get(key);
    }

}
