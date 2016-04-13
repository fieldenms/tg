package ua.com.fielden.platform.client.ioc;

import java.util.Properties;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.ioc.CommonRestFactoryModule;
import ua.com.fielden.platform.rao.EntityAggregatesRao;
import ua.com.fielden.platform.rao.GeneratedEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.RestAuthorisationModel;
import ua.com.fielden.platform.security.SecurityTokenControllerRao;
import ua.com.fielden.platform.security.UserControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigRao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuRao;
import ua.com.fielden.platform.ui.config.api.EntityCentreConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.EntityLocatorConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.EntityMasterConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.MainMenuItemControllerRao;
import ua.com.fielden.platform.ui.config.api.MainMenuItemInvisibilityControllerRao;
import ua.com.fielden.platform.update.IReferenceDependancyController;
import ua.com.fielden.platform.update.ReferenceDependancyController;

/**
 * Basic IoC module for client web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicatonSettings});
 * <li>Serialisation mechanism;
 * <li>All essential RAO interfaces such as {@link IUserProvider}, {@link IReferenceDependancyController}, {@link IDaoFactory}, {@link IValueMatcherFactory}, {@link IUser},
 * {@link IAuthorisationModel} and more;
 * <li>Provides workflow sensitive application main menu configuration related bindings.
 * </ul>
 *
 * @author TG Team
 *
 */
@Deprecated
public class BasicWebClientModule extends CommonRestFactoryModule {
    protected final Properties props;
    private final Class<? extends ISerialisationClassProvider> serialisationClassProviderType;
    private final IApplicationDomainProvider applicationDomainProvider;

    public BasicWebClientModule(final RestClientUtil restUtil, final IApplicationDomainProvider applicationDomainProvider, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Properties props) {
        super(restUtil);
        this.applicationDomainProvider = applicationDomainProvider;
        this.serialisationClassProviderType = serialisationClassProviderType;
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        // bind application specific constants
        bindConstant().annotatedWith(Names.named("app.home")).to(props.getProperty("app.home"));
        bindConstant().annotatedWith(Names.named("reports.path")).to(props.getProperty("reports.path"));
        bindConstant().annotatedWith(Names.named("domain.path")).to(props.getProperty("domain.path"));
        bindConstant().annotatedWith(Names.named("domain.package")).to(props.getProperty("domain.package"));
        bindConstant().annotatedWith(Names.named("private-key")).to(props.getProperty("private-key"));
        bindConstant().annotatedWith(Names.named("tokens.path")).to(props.getProperty("tokens.path"));
        bindConstant().annotatedWith(Names.named("tokens.package")).to(props.getProperty("tokens.package"));
        bindConstant().annotatedWith(Names.named("workflow")).to(props.getProperty("workflow"));
        bind(IApplicationSettings.class).to(ApplicationSettings.class).in(Scopes.SINGLETON);
        bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider);
        // bind user provider
        bind(IUserProvider.class).to(RestClientUtil.class);
        // bind reference dependency controller required for the application update mechanism
        bind(IReferenceDependancyController.class).to(ReferenceDependancyController.class);
        // serialisation related binding
        bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Scopes.SINGLETON);
        bind(ISerialiser0.class).to(Serialiser0.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(Serialiser.class).in(Scopes.SINGLETON);
        /////////////////////////////////////////////////////////////////////////
        /////////////// bind some required platform specific RAOs ///////////////
        /////////////////////////////////////////////////////////////////////////
        bind(IEntityAggregatesDao.class).to(EntityAggregatesRao.class).in(Scopes.SINGLETON);
        bind(IGeneratedEntityController.class).to(GeneratedEntityRao.class); // should not be a singleton
        // bind value matcher factory to support autocompleters and entity master factory
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
        // security and user management
        bind(IUser.class).to(UserControllerRao.class).in(Scopes.SINGLETON);
        bind(IUserRoleDao.class).to(UserRoleRao.class).in(Scopes.SINGLETON);
        bind(ISecurityTokenController.class).to(SecurityTokenControllerRao.class).in(Scopes.SINGLETON);
        bind(IAuthorisationModel.class).to(RestAuthorisationModel.class).in(Scopes.SINGLETON);

        ////////////////////////////////////////////////////////////////////////
        //////////////// bind domain tree configuration manager ////////////////
        ////////////////////////////////////////////////////////////////////////
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
        bind(IGlobalDomainTreeManager.class).to(GlobalDomainTreeManager.class).in(Scopes.SINGLETON);

        ////////////////////////////////////////////////////////////////////////
        //////////////// bind UI configuration controllers /////////////////////
        ////////////////////////////////////////////////////////////////////////
        bind(IMainMenuItemController.class).to(MainMenuItemControllerRao.class).in(Scopes.SINGLETON);
        bind(IMainMenu.class).to(MainMenuRao.class).in(Scopes.SINGLETON);
        bind(IEntityMasterConfig.class).to(EntityMasterConfigControllerRao.class).in(Scopes.SINGLETON);
        bind(IEntityLocatorConfig.class).to(EntityLocatorConfigControllerRao.class).in(Scopes.SINGLETON);
        bind(IEntityCentreConfig.class).to(EntityCentreConfigControllerRao.class).in(Scopes.SINGLETON);
        bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigRao.class).in(Scopes.SINGLETON);
        bind(IMainMenuItemInvisibility.class).to(MainMenuItemInvisibilityControllerRao.class).in(Scopes.SINGLETON); // this specific binding is required only for the main menu migration utility
        //////////////////////////////////////////////////////////////////////////////
        bind(IEntityMasterManager.class).to(EntityMasterManager.class).in(Scopes.SINGLETON);
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, initialises the REST utility instance with {@link ISerialiser} and {@link IUserEx}.
     */
    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        restUtil.initSerialiser(injector.getInstance(ISerialiser.class));
        restUtil.setUserController(injector.getInstance(IUser.class));
    }
}
