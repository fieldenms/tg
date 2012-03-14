package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.attachment.IAttachmentController2;
import ua.com.fielden.platform.attachment.IEntityAttachmentAssociationController2;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao2.AttachmentDao2;
import ua.com.fielden.platform.dao2.EntityAggregatesDao2;
import ua.com.fielden.platform.dao2.EntityAttachmentAssociationDao2;
import ua.com.fielden.platform.dao2.IDaoFactory2;
import ua.com.fielden.platform.dao2.IEntityAggregatesDao2;
import ua.com.fielden.platform.dao2.ISecurityRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserAndRoleAssociationDao2;
import ua.com.fielden.platform.dao2.IUserRoleDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.keygen.IKeyNumberGenerator;
import ua.com.fielden.platform.keygen.KeyNumberDao2;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.NoAuthorisation;
import ua.com.fielden.platform.security.dao.SecurityRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserAndRoleAssociationDao2;
import ua.com.fielden.platform.security.dao.UserRoleDao2;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.IUserController2;
import ua.com.fielden.platform.security.provider.SecurityTokenController2;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.provider.UserController2;
import ua.com.fielden.platform.security.user.IUserDao2;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfigController2;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfigController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibilityController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder2;
import ua.com.fielden.platform.ui.config.controller.EntityCentreConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.EntityLocatorConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.EntityMasterConfigControllerDao2;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemControllerDao2;
import ua.com.fielden.platform.ui.config.controller.MainMenuItemInvisibilityControllerDao2;
import ua.com.fielden.platform.ui.config.controller.mixin.PersistedMainMenuStructureBuilder2;

import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Basic IoC module for server web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicatonSettings});
 * <li>Serialisation mechanism;
 * <li>All essential DAO interfaces such as {@link IFilter}, {@link IUserController2}, {@link IDaoFactory2}, {@link IValueMatcherFactory}, {@link IUserDao2}, {@link IAuthorisationModel} and more;
 * <li>Provides application main menu configuration related DAO bindings.
 * </ul>
 *
 * @author TG Team
 *
 */
public class BasicWebServerModule2 extends CommonFactoryModule2 {

    private final Properties props;
    private final SecurityTokenProvider tokenProvider;
    private final Class<? extends ISerialisationClassProvider> serialisationClassProviderType;
    private final Class<? extends IFilter> automaticDataFilterType;


    public BasicWebServerModule2(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(props, defaultHibernateTypes, applicationEntityTypes);
	this.props = props;
	this.tokenProvider = tokenProvider;
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

	// serialisation related binding
	bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Scopes.SINGLETON); // FleetSerialisationClassProvider.class
	bind(ISerialiser.class).to(TgKryo.class).in(Scopes.SINGLETON); //

	// bind DAO and any other implementations of the required application controllers
	bind(IFilter.class).to(automaticDataFilterType); // UserDrivenFilter.class
	bind(IEntityAggregatesDao2.class).to(EntityAggregatesDao2.class);
	bind(IKeyNumberGenerator.class).to(KeyNumberDao2.class);

	bind(IAttachmentController2.class).to(AttachmentDao2.class);
	bind(IEntityAttachmentAssociationController2.class).to(EntityAttachmentAssociationDao2.class);

	// configuration related binding
	bind(IMainMenuItemController2.class).to(MainMenuItemControllerDao2.class);
	bind(IMainMenuItemInvisibilityController2.class).to(MainMenuItemInvisibilityControllerDao2.class);
	bind(IMainMenuStructureBuilder2.class).to(PersistedMainMenuStructureBuilder2.class);
	bind(IEntityMasterConfigController2.class).to(EntityMasterConfigControllerDao2.class);
	bind(IEntityLocatorConfigController2.class).to(EntityLocatorConfigControllerDao2.class);
	bind(IEntityCentreConfigController2.class).to(EntityCentreConfigControllerDao2.class);

	// user security related bindings
	bind(IUserRoleDao2.class).to(UserRoleDao2.class);
	bind(IUserAndRoleAssociationDao2.class).to(UserAndRoleAssociationDao2.class);
	bind(ISecurityRoleAssociationDao2.class).to(SecurityRoleAssociationDao2.class);
	bind(IUserController2.class).to(UserController2.class);
	bind(IUserDao2.class).to(UserController2.class);
	bind(ISecurityTokenController.class).to(SecurityTokenController2.class);
	if (tokenProvider != null) {
	    bind(SecurityTokenProvider.class).toInstance(tokenProvider);
	}
	bind(IAuthorisationModel.class).to(NoAuthorisation.class);

	// bind value matcher factory to support autocompleters
	bind(IDaoFactory2.class).toInstance(getDaoFactory());
	bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Scopes.SINGLETON);

    }

}
