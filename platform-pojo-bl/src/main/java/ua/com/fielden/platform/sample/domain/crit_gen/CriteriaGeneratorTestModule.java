package ua.com.fielden.platform.sample.domain.crit_gen;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.factory.DefaultCompanionObjectFinderImpl;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.sample.domain.ITgSystem;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

public class CriteriaGeneratorTestModule extends EntityModuleWithPropertyFactory {

    @Override
    protected void configure() {
        super.configure();
        bindConstant().annotatedWith(Names.named("app.name")).to("Unit Tests");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("192.168.1.8");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tests@tg.org"); 

        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(ITopLevelEntity.class).to(TopLevelEntityDaoStub.class);
        bind(ILastLevelEntity.class).to(LastLevelEntityDaoStub.class);
        bind(ISecondLevelEntity.class).to(SecondLevelEntityDaoStub.class);
        bind(ICompanionObjectFinder.class).to(DefaultCompanionObjectFinderImpl.class).in(Scopes.SINGLETON);
        bind(IGeneratedEntityController.class).to(GeneratedEntityControllerStub.class).in(Scopes.SINGLETON);
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(StubSerialiser.class).in(Scopes.SINGLETON);
        bind(ISerialisationClassProvider.class).to(StubSerialisationClassProvider.class).in(Scopes.SINGLETON);
        bind(ITgSystem.class).to(TgSystemDaoStub.class);
        bind(IDates.class).to(DatesForTesting.class).in(Scopes.SINGLETON);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class).in(Scopes.SINGLETON);
    }

}
