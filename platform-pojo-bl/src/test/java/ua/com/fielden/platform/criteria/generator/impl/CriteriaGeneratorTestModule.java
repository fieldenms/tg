package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.matcher.development.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.development.ValueMatcherFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Scopes;

public class CriteriaGeneratorTestModule extends EntityModuleWithPropertyFactory {

    @Override
    protected void configure() {
	super.configure();
	bind(IDaoFactory.class).to(StubDaoFactory.class).in(Scopes.SINGLETON);
	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
	bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
	bind(IEntityAggregatesDao.class).to(StubEntityAggregatesDao.class).in(Scopes.SINGLETON);
    }

}
