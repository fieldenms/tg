package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory2;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory2;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Scopes;

public class CriteriaGeneratorTestModule extends EntityModuleWithPropertyFactory {

    @Override
    protected void configure() {
	super.configure();
	bind(IDaoFactory2.class).to(StubDaoFactory.class).in(Scopes.SINGLETON);
	bind(IValueMatcherFactory2.class).to(ValueMatcherFactory2.class).in(Scopes.SINGLETON);
	bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
    }

}
