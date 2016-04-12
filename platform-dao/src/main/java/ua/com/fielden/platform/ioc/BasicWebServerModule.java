package ua.com.fielden.platform.ioc;

import java.util.Map;
import java.util.Properties;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.AttachmentDao;
import ua.com.fielden.platform.dao.EntityAttachmentAssociationDao;
import ua.com.fielden.platform.dao.GeneratedEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.EntityDeleteActionDao;
import ua.com.fielden.platform.entity.EntityEditActionDao;
import ua.com.fielden.platform.entity.EntityNewActionDao;
import ua.com.fielden.platform.entity.IEntityDeleteAction;
import ua.com.fielden.platform.entity.IEntityEditAction;
import ua.com.fielden.platform.entity.IEntityNewAction;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.keygen.IKeyNumber;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao;
import ua.com.fielden.platform.security.dao.UserRoleDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenInfoDao;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.provider.UserRoleTokensUpdaterDao;
import ua.com.fielden.platform.security.provider.UserRolesUpdaterDao;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSessionDao;
import ua.com.fielden.platform.security.user.ISecurityTokenInfo;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.IUserRolesUpdater;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuDao;
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

/**
 * Basic IoC module for server web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicatonSettings});
 * <li>Serialisation mechanism;
 * <li>All essential DAO interfaces such as {@link IFilter}, {@link IUserEx}, {@link IDaoFactory}, {@link IValueMatcherFactory}, {@link IUser}, {@link IAuthorisationModel} and
 * more;
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
    private final Class<? extends IAuthorisationModel> authorisationModelType;

    public BasicWebServerModule(final Map<Class, Class> defaultHibernateTypes, //
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
        this.authorisationModelType = ServerAuthorisationModel.class;
    }

    public BasicWebServerModule(final Map<Class, Class> defaultHibernateTypes, //
            final IApplicationDomainProvider applicationDomainProvider,//
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
            final Class<? extends IFilter> automaticDataFilterType, //
            final Class<? extends IAuthorisationModel> authorisationModelType,
            final SecurityTokenProvider tokenProvider,//
            final Properties props) throws Exception {
        super(props, defaultHibernateTypes, applicationDomainProvider.entityTypes());
        this.props = props;
        this.tokenProvider = tokenProvider;
        this.applicationDomainProvider = applicationDomainProvider;
        this.serialisationClassProviderType = serialisationClassProviderType;
        this.automaticDataFilterType = automaticDataFilterType;
        this.authorisationModelType = authorisationModelType;
    }

    @Override
    protected void configure() {
        super.configure();
        // bind application specific constants
        bindConstant().annotatedWith(Names.named("app.name")).to(props.getProperty("app.name"));
        bindConstant().annotatedWith(Names.named("reports.path")).to("");
        bindConstant().annotatedWith(Names.named("domain.path")).to(props.getProperty("domain.path"));
        bindConstant().annotatedWith(Names.named("domain.package")).to(props.getProperty("domain.package"));
        bindConstant().annotatedWith(Names.named("tokens.path")).to(props.getProperty("tokens.path"));
        bindConstant().annotatedWith(Names.named("tokens.package")).to(props.getProperty("tokens.package"));
        bindConstant().annotatedWith(Names.named("workflow")).to(props.getProperty("workflow"));
        bindConstant().annotatedWith(Names.named("attachments.location")).to(props.getProperty("attachments.location"));
        bindConstant().annotatedWith(Names.named("email.smtp")).to(props.getProperty("email.smtp"));
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to(props.getProperty("email.fromAddress"));

        bind(IApplicationSettings.class).to(ApplicationSettings.class).in(Scopes.SINGLETON);
        bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider);
        // serialisation related binding
        bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Scopes.SINGLETON); // FleetSerialisationClassProvider.class
        bind(ISerialiser0.class).to(Serialiser0.class).in(Scopes.SINGLETON);
        bind(ISerialiser.class).to(Serialiser.class).in(Scopes.SINGLETON); //

        // bind DAO and any other implementations of the required application controllers
        bind(IFilter.class).to(automaticDataFilterType); // UserDrivenFilter.class
        bind(IKeyNumber.class).to(KeyNumberDao.class);

        // bind attachment controllers
        bind(IAttachment.class).to(AttachmentDao.class);
        bind(IEntityAttachmentAssociationController.class).to(EntityAttachmentAssociationDao.class);

        // configuration related binding
        bind(IMainMenuItemController.class).to(MainMenuItemControllerDao.class);
        bind(IMainMenuItemInvisibilityController.class).to(MainMenuItemInvisibilityControllerDao.class);
        bind(IMainMenu.class).to(MainMenuDao.class);
        bind(IMainMenuStructureBuilder.class).to(PersistedMainMenuStructureBuilder.class);
        bind(IEntityMasterConfigController.class).to(EntityMasterConfigControllerDao.class);
        bind(IEntityLocatorConfigController.class).to(EntityLocatorConfigControllerDao.class);
        bind(IEntityCentreConfigController.class).to(EntityCentreConfigControllerDao.class);
        bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigDao.class);
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
        bind(IGeneratedEntityController.class).to(GeneratedEntityDao.class).in(Scopes.SINGLETON);

        // bind entity manipulation controller
        bind(IEntityNewAction.class).to(EntityNewActionDao.class).in(Scopes.SINGLETON);
        bind(IEntityEditAction.class).to(EntityEditActionDao.class).in(Scopes.SINGLETON);
        bind(IEntityDeleteAction.class).to(EntityDeleteActionDao.class).in(Scopes.SINGLETON);

        // user security related bindings
        bind(IUser.class).to(UserDao.class);
        bind(IUserRolesUpdater.class).to(UserRolesUpdaterDao.class);
        
        bind(IUserRoleDao.class).to(UserRoleDao.class);
        bind(IUserRoleTokensUpdater.class).to(UserRoleTokensUpdaterDao.class);
        bind(ISecurityTokenInfo.class).to(SecurityTokenInfoDao.class);

        bind(IUserAndRoleAssociationDao.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociationDao.class).to(SecurityRoleAssociationDao.class);
        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
        bind(ISecurityRoleAssociationBatchAction.class).to(SecurityRoleAssociationBatchActionDao.class);

        bind(IUserSession.class).to(UserSessionDao.class);
        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        if (tokenProvider != null) {
            bind(SecurityTokenProvider.class).toInstance(tokenProvider);
        }
        bind(IAuthorisationModel.class).to(authorisationModelType);

        // bind value matcher factory to support autocompleters
        // TODO is this binding really needed for the server side???
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);
    }
}