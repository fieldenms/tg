package ua.com.fielden.platform.ioc;

import static ua.com.fielden.platform.reflection.CompanionObjectAutobinder.bindCo;

import java.util.Map;
import java.util.Properties;

import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.name.Names;

import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.GeneratedEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.menu.Action;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionCo;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchActionDao;
import ua.com.fielden.platform.ref_hierarchy.AbstractTreeEntry;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchActionDao;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.web_api.GraphQLService;
import ua.com.fielden.platform.web_api.IWebApi;

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
 * <p>
 * Instantiation of singletons occurs in accordance with <a href="https://github.com/google/guice/wiki/Scopes#eager-singletons">Guice Eager Singletons</a>,
 * where values of {@link Workflows} are mapped to {@link Stage}.
 *
 * @author TG Team
 *
 */
public class BasicWebServerModule extends CommonFactoryModule {

    private final Properties props;
    private final Class<? extends ISecurityTokenProvider> tokenProviderType;
    private final IApplicationDomainProvider applicationDomainProvider;
    private final Class<? extends ISerialisationClassProvider> serialisationClassProviderType;
    private final Class<? extends IFilter> automaticDataFilterType;
    private final Class<? extends IAuthorisationModel> authorisationModelType;

    public BasicWebServerModule(final Map<Class, Class> defaultHibernateTypes,
            final IApplicationDomainProvider applicationDomainProvider,
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType,
            final Class<? extends IFilter> automaticDataFilterType,
            final Class<? extends ISecurityTokenProvider> tokenProviderType,
            final Properties props) throws Exception {
        super(props, defaultHibernateTypes, applicationDomainProvider.entityTypes());
        this.props = props;
        this.tokenProviderType = tokenProviderType;
        this.applicationDomainProvider = applicationDomainProvider;
        this.serialisationClassProviderType = serialisationClassProviderType;
        this.automaticDataFilterType = automaticDataFilterType;
        this.authorisationModelType = ServerAuthorisationModel.class;
    }

    public BasicWebServerModule(final Map<Class, Class> defaultHibernateTypes,
            final IApplicationDomainProvider applicationDomainProvider,
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType,
            final Class<? extends IFilter> automaticDataFilterType,
            final Class<? extends IAuthorisationModel> authorisationModelType,
            final Class<? extends ISecurityTokenProvider> tokenProviderType,
            final Properties props) throws Exception {
        super(props, defaultHibernateTypes, applicationDomainProvider.entityTypes());
        this.props = props;
        this.tokenProviderType = tokenProviderType;
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
        bindConstant().annotatedWith(Names.named("independent.time.zone")).to(Boolean.valueOf(props.getProperty("independent.time.zone")));
        final boolean webApiPresent = Boolean.valueOf(props.getProperty("web.api"));
        bindConstant().annotatedWith(Names.named("web.api")).to(webApiPresent);
        bindConstant().annotatedWith(Names.named("web.api.maxQueryDepth")).to(Integer.valueOf(props.getProperty("web.api.maxQueryDepth", "13")));
        // authentication parameters
        bindConstant().annotatedWith(Names.named("auth.mode")).to(props.getProperty("auth.mode", AuthMode.RSO.name()));
        bindConstant().annotatedWith(Names.named("auth.sso.provider")).to(props.getProperty("auth.sso.provider", "Identity Provider"));
        // date related parameters
        bindConstant().annotatedWith(Names.named("weekStart")).to(Integer.valueOf(props.getProperty("weekStart", "0")));
        bindConstant().annotatedWith(Names.named("financialYearStartDate")).to(Integer.valueOf(props.getProperty("financialYearStartDate", "1")));
        bindConstant().annotatedWith(Names.named("financialYearStartMonth")).to(Integer.valueOf(props.getProperty("financialYearStartMonth", "7")));

        bind(IApplicationSettings.class).to(ApplicationSettings.class).in(Singleton.class);
        bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider);
        if (tokenProviderType != null) {
            bind(ISecurityTokenProvider.class).to(tokenProviderType).in(Singleton.class);
        } else {
            bind(ISecurityTokenProvider.class).to(SecurityTokenProvider.class).in(Singleton.class);
        }
        // serialisation related binding
        bind(ISerialisationClassProvider.class).to(serialisationClassProviderType).in(Singleton.class);
        bind(ISerialiser.class).to(Serialiser.class).in(Singleton.class);

        // bind DAO and any other implementations of the required application controllers
        bind(IFilter.class).to(automaticDataFilterType); // UserDrivenFilter.class

        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class).in(Singleton.class);
        bind(IGeneratedEntityController.class).to(GeneratedEntityDao.class);

        PlatformDomainTypes.typesNotDependentOnWebUI.stream()
            .filter(type -> !AbstractTreeEntry.class.isAssignableFrom(type) && !Action.class.isAssignableFrom(type)) // these entity types have no companions
            .forEach(type -> bindCo(type, binder()));
        bind(IUserAndRoleAssociationBatchAction.class).to(UserAndRoleAssociationBatchActionDao.class);
        bind(UserMenuInvisibilityAssociationBatchActionCo.class).to(UserMenuInvisibilityAssociationBatchActionDao.class);
        bind(ISecurityRoleAssociationBatchAction.class).to(SecurityRoleAssociationBatchActionDao.class);

        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bind(IAuthorisationModel.class).to(authorisationModelType);

        // bind value matcher factory to support autocompleters
        // TODO is this binding really needed for the server side???
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class).in(Singleton.class);

        if (webApiPresent) { // in case where Web API has been turned-on in application.properties ...
            // ... bind Web API to platform-dao GraphQL-based implementation
            bind(IWebApi.class).to(GraphQLService.class).in(Singleton.class);
        }
    }

    public Properties getProps() {
        return props;
    }
}