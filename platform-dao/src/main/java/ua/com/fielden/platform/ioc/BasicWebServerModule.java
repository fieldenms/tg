package ua.com.fielden.platform.ioc;

import java.util.Map;
import java.util.Properties;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

import ua.com.fielden.platform.attachment.AttachmentDao;
import ua.com.fielden.platform.attachment.AttachmentPreviewEntityActionDao;
import ua.com.fielden.platform.attachment.AttachmentUploaderDao;
import ua.com.fielden.platform.attachment.AttachmentsUploadActionDao;
import ua.com.fielden.platform.attachment.IAttachment;
import ua.com.fielden.platform.attachment.IAttachmentPreviewEntityAction;
import ua.com.fielden.platform.attachment.IAttachmentUploader;
import ua.com.fielden.platform.attachment.IAttachmentsUploadAction;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.GeneratedEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.ISecurityRoleAssociation;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.EntityDeleteActionDao;
import ua.com.fielden.platform.entity.EntityEditActionDao;
import ua.com.fielden.platform.entity.EntityNavigationActionDao;
import ua.com.fielden.platform.entity.EntityNewActionDao;
import ua.com.fielden.platform.entity.IEntityDeleteAction;
import ua.com.fielden.platform.entity.IEntityEditAction;
import ua.com.fielden.platform.entity.IEntityNavigationAction;
import ua.com.fielden.platform.entity.IEntityNewAction;
import ua.com.fielden.platform.entity.IKeyLocator;
import ua.com.fielden.platform.entity.ISecurityMatrixInsertionPoint;
import ua.com.fielden.platform.entity.ISecurityMatrixSaveAction;
import ua.com.fielden.platform.entity.ISecurityTokenTreeNodeEntity;
import ua.com.fielden.platform.entity.KeyLocatorDao;
import ua.com.fielden.platform.entity.SecurityMatrixInsertionPointDao;
import ua.com.fielden.platform.entity.SecurityMatrixSaveActionDao;
import ua.com.fielden.platform.entity.SecurityTokenTreeNodeEntityDao;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarningsDao;
import ua.com.fielden.platform.entity.functional.master.IAcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.IPropertyWarning;
import ua.com.fielden.platform.entity.functional.master.PropertyWarningDao;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.keygen.IKeyNumber;
import ua.com.fielden.platform.keygen.KeyNumberDao;
import ua.com.fielden.platform.menu.CustomViewDao;
import ua.com.fielden.platform.menu.EntityCentreViewDao;
import ua.com.fielden.platform.menu.EntityMasterViewDao;
import ua.com.fielden.platform.menu.ICustomView;
import ua.com.fielden.platform.menu.IEntityCentreView;
import ua.com.fielden.platform.menu.IEntityMasterView;
import ua.com.fielden.platform.menu.IMenu;
import ua.com.fielden.platform.menu.IMenuSaveAction;
import ua.com.fielden.platform.menu.IModule;
import ua.com.fielden.platform.menu.IModuleMenuItem;
import ua.com.fielden.platform.menu.IView;
import ua.com.fielden.platform.menu.IWebMenuItemInvisibility;
import ua.com.fielden.platform.menu.MenuDao;
import ua.com.fielden.platform.menu.MenuSaveActionDao;
import ua.com.fielden.platform.menu.ModuleDao;
import ua.com.fielden.platform.menu.ModuleMenuItemDao;
import ua.com.fielden.platform.menu.ViewDao;
import ua.com.fielden.platform.menu.WebMenuItemInvisibilityDao;
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
import ua.com.fielden.platform.security.user.IUserSecret;
import ua.com.fielden.platform.security.user.UserDao;
import ua.com.fielden.platform.security.user.UserSecretDao;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser0;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfigDao;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigDao;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigDao;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemDao;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteActionDao;
import ua.com.fielden.platform.web.centre.CentreConfigDuplicateActionDao;
import ua.com.fielden.platform.web.centre.CentreConfigNewActionDao;
import ua.com.fielden.platform.web.centre.CustomisableColumnDao;
import ua.com.fielden.platform.web.centre.ICentreConfigDeleteAction;
import ua.com.fielden.platform.web.centre.ICentreConfigDuplicateAction;
import ua.com.fielden.platform.web.centre.ICentreConfigNewAction;
import ua.com.fielden.platform.web.centre.ICustomisableColumn;
import ua.com.fielden.platform.web.centre.ILoadableCentreConfig;
import ua.com.fielden.platform.web.centre.IOverrideCentreConfig;
import ua.com.fielden.platform.web.centre.LoadableCentreConfigDao;
import ua.com.fielden.platform.web.centre.OverrideCentreConfigDao;

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

        // bind attachment related companions
        bind(IAttachment.class).to(AttachmentDao.class);
        bind(IAttachmentUploader.class).to(AttachmentUploaderDao.class);
        bind(IAttachmentsUploadAction.class).to(AttachmentsUploadActionDao.class);
        bind(IAttachmentPreviewEntityAction.class).to(AttachmentPreviewEntityActionDao.class);

        // configuration menu related binding
        bind(IModuleMenuItem.class).to(ModuleMenuItemDao.class);
        bind(IEntityCentreView.class).to(EntityCentreViewDao.class);
        bind(IView.class).to(ViewDao.class);
        bind(ICustomView.class).to(CustomViewDao.class);
        bind(IModule.class).to(ModuleDao.class);
        bind(IMenu.class).to(MenuDao.class);
        bind(IEntityMasterView.class).to(EntityMasterViewDao.class);
        bind(IMenuSaveAction.class).to(MenuSaveActionDao.class);
        bind(IWebMenuItemInvisibility.class).to(WebMenuItemInvisibilityDao.class);

        // configuration related binding
        bind(IMainMenuItem.class).to(MainMenuItemDao.class);
        bind(IEntityMasterConfig.class).to(EntityMasterConfigDao.class);
        bind(IEntityLocatorConfig.class).to(EntityLocatorConfigDao.class);
        bind(IEntityCentreConfig.class).to(EntityCentreConfigDao.class);
        bind(IEntityCentreAnalysisConfig.class).to(EntityCentreAnalysisConfigDao.class);
        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Scopes.SINGLETON);
        bind(IGeneratedEntityController.class).to(GeneratedEntityDao.class);

        // bind entity manipulation controller
        bind(IEntityNewAction.class).to(EntityNewActionDao.class);
        bind(IEntityEditAction.class).to(EntityEditActionDao.class);
        bind(IEntityNavigationAction.class).to(EntityNavigationActionDao.class);
        bind(IEntityDeleteAction.class).to(EntityDeleteActionDao.class);

        //Bar Code Scanning
        bind(IKeyLocator.class).to(KeyLocatorDao.class);

        // user security related bindings
        bind(IUser.class).to(UserDao.class);
        bind(IUserSecret.class).to(UserSecretDao.class);
        bind(IUserRolesUpdater.class).to(UserRolesUpdaterDao.class);

        bind(IUserRole.class).to(UserRoleDao.class);
        bind(IUserRoleTokensUpdater.class).to(UserRoleTokensUpdaterDao.class);
        bind(ISecurityTokenInfo.class).to(SecurityTokenInfoDao.class);
        bind(ISecurityMatrixInsertionPoint.class).to(SecurityMatrixInsertionPointDao.class);
        bind(ISecurityTokenTreeNodeEntity.class).to(SecurityTokenTreeNodeEntityDao.class);
        bind(ISecurityMatrixSaveAction.class).to(SecurityMatrixSaveActionDao.class);

        bind(ICustomisableColumn.class).to(CustomisableColumnDao.class);
        bind(ICentreConfigNewAction.class).to(CentreConfigNewActionDao.class);
        bind(ICentreConfigDuplicateAction.class).to(CentreConfigDuplicateActionDao.class);
        bind(ICentreConfigDeleteAction.class).to(CentreConfigDeleteActionDao.class);
        bind(ILoadableCentreConfig.class).to(LoadableCentreConfigDao.class);
        bind(IOverrideCentreConfig.class).to(OverrideCentreConfigDao.class);

        bind(IUserAndRoleAssociation.class).to(UserAndRoleAssociationDao.class);
        bind(ISecurityRoleAssociation.class).to(SecurityRoleAssociationDao.class);
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

        // warnings acknowledgement binding
        bind(IAcknowledgeWarnings.class).to(AcknowledgeWarningsDao.class);
        bind(IPropertyWarning.class).to(PropertyWarningDao.class);
    }

    public Properties getProps() {
        return props;
    }
}