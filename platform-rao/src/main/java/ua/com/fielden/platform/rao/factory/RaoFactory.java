package ua.com.fielden.platform.rao.factory;

import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.rao.DynamicEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;

import com.google.inject.Inject;

/**
 * Factory for instantiating DAO by means of Guice injection.
 *
 * @author TG Team
 *
 */
public class RaoFactory implements IDaoFactory {
    private final RestClientUtil util;

    @Inject
    public RaoFactory(final RestClientUtil util) {
	this.util = util;
    }

    @Override
    public IEntityDao<?> newDao(final Class<? extends AbstractEntity<?>> entityType) {
	final DynamicEntityRao rao = new DynamicEntityRao(util);
	rao.setEntityType(entityType);
	return rao;
    }
}
