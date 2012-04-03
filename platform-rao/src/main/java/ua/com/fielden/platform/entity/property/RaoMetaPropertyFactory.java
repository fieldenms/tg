package ua.com.fielden.platform.entity.property;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.IDefaultControllerProvider2;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.meta.AbstractMetaPropertyFactory2;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.validation.DomainValidationConfig;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator2;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.rao.DynamicEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;

import com.google.inject.Inject;

/**
 * RAO driven {@link IMetaPropertyFactory} implementation.
 *
 * @author TG Team
 *
 */
public class RaoMetaPropertyFactory extends AbstractMetaPropertyFactory2 {

    private final IDefaultControllerProvider2 defaultControllerProvider;
    private final RestClientUtil restUtil;

    @Inject
    public RaoMetaPropertyFactory(final DomainValidationConfig domainConfig, final DomainMetaPropertyConfig domainMetaConfig, final IDefaultControllerProvider2 defaultControllerProvider, final RestClientUtil restUtil) {
	super(domainConfig, domainMetaConfig);
	this.defaultControllerProvider = defaultControllerProvider;
	this.restUtil = restUtil;
    }

    @Override
    protected synchronized IBeforeChangeEventHandler createEntityExists(final EntityExists anotation) {
	final Class<? extends AbstractEntity> key = anotation.value();
	if (!entityExistsValidators.containsKey(key)) {
	    final IEntityDao2 dao = defaultControllerProvider.findController(key);
	    if (dao != null) {
		entityExistsValidators.put(key, new EntityExistsValidator2(dao));
	    } else {
		final DynamicEntityRao dynDao = new DynamicEntityRao(restUtil);
		dynDao.setEntityType(key);
		entityExistsValidators.put(key, new EntityExistsValidator2(dynDao));
	    }
	}

	return entityExistsValidators.get(key);
    }

    public RestClientUtil getRestUtil() {
	return restUtil;
    }
}
