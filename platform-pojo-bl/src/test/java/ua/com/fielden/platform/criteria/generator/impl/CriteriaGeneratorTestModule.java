package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Scopes;

public class CriteriaGeneratorTestModule extends EntityModuleWithPropertyFactory {

    protected final DefaultCompanionObjectFinderImpl defaultControllerProvider;

    public CriteriaGeneratorTestModule() {
        defaultControllerProvider = new DefaultCompanionObjectFinderImpl();
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ITopLevelEntity.class).to(TopLevelEntityDaoStub.class);
        bind(ILastLevelEntity.class).to(LastLevelEntityDaoStub.class);
        bind(ISecondLevelEntity.class).to(SecondLevelEntityDaoStub.class);
        bind(ICompanionObjectFinder.class).toInstance(defaultControllerProvider);
        bind(IDaoFactory.class).to(StubDaoFactory.class).in(Scopes.SINGLETON);
        bind(IGeneratedEntityController.class).to(GeneratedEntityControllerStub.class).in(Scopes.SINGLETON);
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(StubSerialiser.class).in(Scopes.SINGLETON);
        bind(ISerialisationClassProvider.class).to(StubSerialisationClassProvider.class).in(Scopes.SINGLETON);
    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        defaultControllerProvider.setInjector(injector);
    }

}
