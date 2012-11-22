package ua.com.fielden.platform.ioc;

import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.attachment.IAttachmentController;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.EntityAttachmentAssociationDao;
import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.keygen.IKeyNumberGenerator;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.NoAuthorisation;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.provider.UserController;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemInvisibilityControllerDao;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Basic IoC module for server web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicatonSettings});
 * <li>Serialisation mechanism;
 * <li>All essential DAO interfaces such as {@link IFilter}, {@link IUserController}, {@link IDaoFactory}, {@link IValueMatcherFactory}, {@link IUserDao}, {@link IAuthorisationModel} and more;
 * <li>Provides application main menu configuration related DAO bindings.
 * </ul>
 *
 * @author TG Team
 *
 */
public class BasicWebServerModule extends CommonFactoryModule {

    private final Properties props;
    private final SecurityTokenProvider tokenProvider;
    private final IApplicationDomainProvider applicationDomainProvider;
    private final Class<? extends ISerialisationClassProvider> serialisationClassProviderType;
    private final Class<? extends IFilter> automaticDataFilterType;


    public BasicWebServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final IApplicationDomainProvider applicationDomainProvider,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(props, defaultHibernateTypes, applicationDomainProvider.entityTypes());
	this.props = props;
	this.tokenProvider = tokenProvider;
	this.applicationDomainProvider = applicationDomainProvider;
	this.serialisationClassProviderType = serialisationClassProviderType;
	this.automaticDataFilterType = automaticDataFilterType;
    }

    @Override
    protected void configure() {
	super.configure();
	// bind application specific constants
	bindConstant().annotatedWith(Names.named("app.home")).to("");
	bindConstant().annotatedWith(Names.named("reports.path")).to("");
	bindConstant().annotatedWith(Names.named("domain.path")).to(props.getProperty("domain.path"));
	bindConstant().annotatedWith(Names.named("domain.package")).to(props.getProperty("domain.package"));
	bindConstant().annotatedWith(Names.named("private-key")).to(props.getProperty("private-key"));
	bindConstant().annotatedWith(Names.named("tokens.path")).to(props.getProperty("tokens.path"));
	bindConstant().annotatedWith(Names.named("tokens.package")).to(props.getProperty("tokens.package"));
	bindConstant().annotatedWith(Names.named("workflow")).to(props.getProperty("workflow"));
	bind(IApplicationSettings.class).to(ApplicationSettings.class).in(Scopes.SINGLETON);
	bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider);
	// serialisation related binding
	bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Scopes.SINGLETON); // FleetSerialisationClassProvider.class
	bind(ISerialiser.class).to(TgKryo.class).in(Scopes.SINGLETON); //

	// bind DAO and any other implementations of the required application controllers
	bind(IFilter.class).to(automaticDataFilterType); // UserDrivenFilter.class
	bind(IKeyNumberGenerator.class).to(KeyNumberDao.class);

	bind(IAttachmentController.class).to(AttachmentDao.class);
	bind(IEntityAttachmentAssociationController.class).to(EntityAttachmentAssociationDao.class);

	// configuration related binding
	bind(IMainMenuItemController.class).to(MainMenuItemControllerDao.class);
	bind(IMainMenuItemInvisibilityController.class).to(MainMenuItemInvisibilityControllerDao.class);
	bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);
	bind(IEntityMasterConfigController.class).to(EntityMasterConfigControllerDao.class);
	bind(IEntityLocatorConfigController.class).to(EntityLocatorConfigControllerDao.class);
	bind(IEntityCentreConfigController.class).to(EntityCentreConfigControllerDao.class);
	bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigDao.class);

	// user security related bindings
	bind(IUserRoleDao.class).to(UserRoleDao.class);
	bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
	bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);
	bind(IUserController.class).to(UserController.class);
	bind(IUserDao.class).to(UserController.class);
	bind(ISecurityTokenController.class).to(SecurityTokenController.class);
	if (tokenProvider != null) {
	    bind(SecurityTokenProvider.class).toInstance(tokenProvider);
	}
	bind(IAuthorisationModel.class).to(NoAuthorisation.class);

	// bind value matcher factory to support autocompleters
	bind(IDaoFactory.class).toInstance(getDaoFactory());
	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
    }
}