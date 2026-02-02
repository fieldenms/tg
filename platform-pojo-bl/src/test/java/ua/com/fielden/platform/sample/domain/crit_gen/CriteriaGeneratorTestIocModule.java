package ua.com.fielden.platform.sample.domain.crit_gen;

import com.google.inject.name.Names;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.sample.domain.ITgSystem;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.NoAuthorisation;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.EntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

public class CriteriaGeneratorTestIocModule extends EntityTestIocModuleWithPropertyFactory {

    @Override
    protected void configure() {
        super.configure();
        bindConstant().annotatedWith(Names.named("app.name")).to("Unit Tests");
        bindConstant().annotatedWith(Names.named("email.smtp")).to("192.168.1.8");
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to("tests@tg.org");
        bindConstant().annotatedWith(Names.named("reports.path")).to("path");
        bindConstant().annotatedWith(Names.named("domain.path")).to("path");
        bindConstant().annotatedWith(Names.named("domain.package")).to("package");
        bindConstant().annotatedWith(Names.named("tokens.path")).to("path");
        bindConstant().annotatedWith(Names.named("tokens.package")).to("package");
        bindConstant().annotatedWith(Names.named("workflow")).to("development");
        bindConstant().annotatedWith(Names.named("auth.mode")).to("SSO");
        bindConstant().annotatedWith(Names.named("currency.symbol")).to("$");

        bind(IApplicationSettings.class).to(ApplicationSettings.class);
        bind(IApplicationDomainProvider.class).to(ApplicationDomain.class);
        bind(ITopLevelEntity.class).to(TopLevelEntityDaoStub.class);
        bind(ILastLevelEntity.class).to(LastLevelEntityDaoStub.class);
        bind(ISecondLevelEntity.class).to(SecondLevelEntityDaoStub.class);
        bind(IGeneratedEntityController.class).to(GeneratedEntityControllerStub.class);
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class);
        bind(ISerialiser.class).to(StubSerialiser.class);
        bind(ISerialisationClassProvider.class).to(StubSerialisationClassProvider.class);
        bind(ITgSystem.class).to(TgSystemDaoStub.class);
        bind(IDates.class).to(DatesForTesting.class);
        bind(IUniversalConstants.class).to(UniversalConstantsForTesting.class);
        bind(IAuthorisationModel.class).to(NoAuthorisation.class);
    }

}
