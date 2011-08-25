package ua.com.fielden.platform.client.ioc;

import java.util.Properties;

import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.ioc.CommonRestFactoryModule;
import ua.com.fielden.platform.rao.EntityAggregatesRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.factory.RaoFactory;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.RestAuthorisationModel;
import ua.com.fielden.platform.security.SecurityTokenControllerRao;
import ua.com.fielden.platform.security.UserControllerRao;
import ua.com.fielden.platform.security.UserRoleRao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.LocalTreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.review.EntityMasterManager;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.configuration.LocalCenterConfigurationController;
import ua.com.fielden.platform.swing.review.configuration.LocalLocatorConfigurationController;
import ua.com.fielden.platform.swing.review.configuration.LocalMasterConfigurationController;
import ua.com.fielden.platform.ui.config.api.EntityCentreConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.EntityLocatorConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.EntityMasterConfigControllerRao;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.api.MainMenuItemControllerRao;
import ua.com.fielden.platform.ui.config.api.MainMenuItemInvisibilityControllerRao;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.impl.interaction.RemoteLocatorConfigurationController;
import ua.com.fielden.platform.ui.config.impl.interaction.RemoteMasterConfigurationController;
import ua.com.fielden.platform.update.IReferenceDependancyController;
import ua.com.fielden.platform.update.ReferenceDependancyController;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Basic IoC module for client web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicatonSettings});
 * <li>Serialisation mechanism;
 * <li>All essential RAO interfaces such as {@link IUserProvider}, {@link IReferenceDependancyController}, {@link IDaoFactory}, {@link IValueMatcherFactory}, {@link IUserDao}, {@link IAuthorisationModel} and more;
 * <li>Provides workflow sensitive application main menu configuration related bindings.
 * </ul>
 *
 * @author TG Team
 *
 */
public class BasicWebClientModule extends CommonRestFactoryModule {

    protected final Properties props;
    private final Class<? extends ISerialisationClassProvider> serialisationClassProviderType;
    private final Class<? extends IMainMenuStructureBuilder> mainMenuStructureBuilderType;


    public BasicWebClientModule(final RestClientUtil restUtil, final Class<? extends IMainMenuStructureBuilder> mainMenuStructureBuilderType, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Properties props) {
	super(restUtil);
	this.serialisationClassProviderType = serialisationClassProviderType;
	this.props = props;
	this.mainMenuStructureBuilderType = mainMenuStructureBuilderType;
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
	// bind user provider
	bind(IUserProvider.class).to(RestClientUtil.class);
	// bind reference dependency controller required for the application update mechanism
	bind(IReferenceDependancyController.class).to(ReferenceDependancyController.class);
	// serialisation related binding
	bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Scopes.SINGLETON);
	bind(ISerialiser.class).to(TgKryo.class).in(Scopes.SINGLETON);
	/////////////////////////////////////////////////////////////////////////
	/////////////// bind some required platform specific RAOs ///////////////
	/////////////////////////////////////////////////////////////////////////
	bind(IEntityAggregatesDao.class).to(EntityAggregatesRao.class).in(Scopes.SINGLETON);
	// bind value matcher factory to support autocompleters and entity master factory
	bind(IDaoFactory.class).to(RaoFactory.class).in(Scopes.SINGLETON);
	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
	// security and user management
	bind(IUserDao.class).to(UserControllerRao.class).in(Scopes.SINGLETON);
	bind(IUserRoleDao.class).to(UserRoleRao.class).in(Scopes.SINGLETON);
	bind(IUserController.class).to(UserControllerRao.class).in(Scopes.SINGLETON);
	bind(ISecurityTokenController.class).to(SecurityTokenControllerRao.class).in(Scopes.SINGLETON);
	bind(IAuthorisationModel.class).to(RestAuthorisationModel.class).in(Scopes.SINGLETON);

	////////////////////////////////////////////////////////////////////////
	//////////////// bind UI configuration controllers /////////////////////
	////////////////////////////////////////////////////////////////////////
	bind(IMainMenuItemController.class).to(MainMenuItemControllerRao.class).in(Scopes.SINGLETON);
	bind(IEntityMasterConfigController.class).to(EntityMasterConfigControllerRao.class).in(Scopes.SINGLETON);
	bind(IEntityLocatorConfigController.class).to(EntityLocatorConfigControllerRao.class).in(Scopes.SINGLETON);
	bind(IEntityCentreConfigController.class).to(EntityCentreConfigControllerRao.class).in(Scopes.SINGLETON);

	if (Workflows.valueOf(props.getProperty("workflow")).equals(Workflows.deployment)) {
	    bind(IMainMenuItemInvisibilityController.class).to(MainMenuItemInvisibilityControllerRao.class).in(Scopes.SINGLETON);
	    bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);

	    bind(ILocatorConfigurationController.class).to(RemoteLocatorConfigurationController.class).in(Scopes.SINGLETON);
	    bind(IMasterConfigurationController.class).to(RemoteMasterConfigurationController.class).in(Scopes.SINGLETON);
	} else {
	    ///////////////////////////// local
	    bind(ITreeMenuItemVisibilityProvider.class).to(LocalTreeMenuItemVisibilityProvider.class).in(Scopes.SINGLETON);
	    bind(IMainMenuStructureBuilder.class).to(mainMenuStructureBuilderType);

	    bind(ICenterConfigurationController.class).to(LocalCenterConfigurationController.class).in(Scopes.SINGLETON);
	    bind(ILocatorConfigurationController.class).to(LocalLocatorConfigurationController.class).in(Scopes.SINGLETON);
	    bind(IMasterConfigurationController.class).to(LocalMasterConfigurationController.class).in(Scopes.SINGLETON);
	}
	//////////////////////////////////////////////////////////////////////////////
	bind(IEntityMasterManager.class).to(EntityMasterManager.class).in(Scopes.SINGLETON);
    }

}
