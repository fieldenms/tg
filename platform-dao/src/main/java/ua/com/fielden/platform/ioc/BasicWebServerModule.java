package ua.com.fielden.platform.ioc;

import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaGenerator;
import ua.com.fielden.platform.dao.GeneratedEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.matcher.ValueMatcherFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.web_api.GraphQLService;
import ua.com.fielden.platform.web_api.IWebApi;

import java.util.List;
import java.util.Properties;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.web_api.GraphQLService.DEFAULT_MAX_QUERY_DEPTH;
import static ua.com.fielden.platform.web_api.GraphQLService.WARN_INSUFFICIENT_MAX_QUERY_DEPTH;
/**
 * Basic IoC module for server web applications, which should be enhanced by the application specific IoC module.
 *
 * This IoC provides all the necessary bindings for:
 * <ul>
 * <li>Applications settings (refer {@link IApplicationSettings});
 * <li>Serialisation mechanism;
 * <li>Essential DAO interfaces {@link IValueMatcherFactory}, {@link IUser}, {@link IAuthorisationModel} and more;
 * <li>Provides application main menu configuration related DAO bindings.
 * </ul>
 * <p>
 * Instantiation of singletons occurs in accordance with <a href="https://github.com/google/guice/wiki/Scopes#eager-singletons">Guice Eager Singletons</a>,
 * where values of {@link Workflows} are mapped to {@link Stage}.
 *
 * @author TG Team
 *
 */
public class BasicWebServerModule extends CompanionModule {
    private static final Logger LOGGER = getLogger(BasicWebServerModule.class);

    private final Properties props;
    private final IApplicationDomainProvider applicationDomainProvider;
    private final Class<? extends IAuthorisationModel> authorisationModelType;

    public BasicWebServerModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Properties props)
    {
        super(props, domainEntityTypes);
        this.props = props;
        this.applicationDomainProvider = applicationDomainProvider;
        this.authorisationModelType = ServerAuthorisationModel.class;
    }

    public BasicWebServerModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Class<? extends IAuthorisationModel> authorisationModelType,
            final Properties props)
    {
        super(props, domainEntityTypes);
        this.props = props;
        this.applicationDomainProvider = applicationDomainProvider;
        this.authorisationModelType = authorisationModelType;
    }

    @Override
    protected void configure() {
        super.configure();
        // bind application specific constants
        bindConstant().annotatedWith(Names.named("app.name")).to(props.getProperty("app.name"));
        bindConstant().annotatedWith(Names.named("help.defaultUri")).to(props.getProperty("help.defaultUri", ""));
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
        final var maxQueryDepthKey = "web.api.maxQueryDepth";
        final var maxQueryDepth = Integer.valueOf(props.getProperty(maxQueryDepthKey, DEFAULT_MAX_QUERY_DEPTH + ""));
        final var insufficientMaxQueryDepth = maxQueryDepth < DEFAULT_MAX_QUERY_DEPTH;
        if (insufficientMaxQueryDepth) {
            LOGGER.warn(WARN_INSUFFICIENT_MAX_QUERY_DEPTH.formatted(maxQueryDepth));
        }
        bindConstant().annotatedWith(Names.named(maxQueryDepthKey)).to(insufficientMaxQueryDepth ? DEFAULT_MAX_QUERY_DEPTH : maxQueryDepth);
        // authentication parameters
        bindConstant().annotatedWith(Names.named("auth.mode")).to(props.getProperty("auth.mode", AuthMode.RSO.name()));
        bindConstant().annotatedWith(Names.named("auth.sso.provider")).to(props.getProperty("auth.sso.provider", "Identity Provider"));
        // date related parameters
        bindConstant().annotatedWith(Names.named("dates.weekStart")).to(Integer.valueOf(props.getProperty("dates.weekStart", "1"))); // 1 - Monday
        bindConstant().annotatedWith(Names.named("dates.finYearStartDay")).to(Integer.valueOf(props.getProperty("dates.finYearStartDay", "1"))); // 1 - the first day of the month
        bindConstant().annotatedWith(Names.named("dates.finYearStartMonth")).to(Integer.valueOf(props.getProperty("dates.finYearStartMonth", "7"))); // 7 - July, the 1st of July is the start of Fin Year in Australia

        bind(IApplicationSettings.class).to(ApplicationSettings.class);
        bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider);
        requireBinding(ISecurityTokenProvider.class);
        // serialisation related binding
        requireBinding(ISerialisationClassProvider.class);
        bind(ISerialiser.class).to(Serialiser.class);

        requireBinding(IFilter.class);

        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class);
        bind(IGeneratedEntityController.class).to(GeneratedEntityDao.class);

        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bind(IAuthorisationModel.class).to(authorisationModelType);
        install(new AuthorisationModule());

        // bind value matcher factory to support autocompleters
        // TODO is this binding really needed for the server side???
        bind(IValueMatcherFactory.class).to(ValueMatcherFactory.class);

        if (webApiPresent) { // in case where Web API has been turned-on in application.properties ...
            // ... bind Web API to platform-dao GraphQL-based implementation
            bind(IWebApi.class).to(GraphQLService.class);
        }
    }

    public Properties getProps() {
        return props;
    }
}
