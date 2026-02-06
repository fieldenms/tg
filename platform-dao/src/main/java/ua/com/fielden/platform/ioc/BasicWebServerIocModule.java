package ua.com.fielden.platform.ioc;

import com.google.inject.Provides;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.audit.AbstractAuditProp;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
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
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenController;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.impl.Serialiser;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web_api.GraphQLService;
import ua.com.fielden.platform.web_api.IWebApi;

import java.util.List;
import java.util.Properties;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AuditUtils.getAuditTypeVersion;
import static ua.com.fielden.platform.audit.AuditUtils.isAudited;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_MODE;
import static ua.com.fielden.platform.audit.AuditingIocModule.AUDIT_PATH;
import static ua.com.fielden.platform.web_api.GraphQLService.DEFAULT_MAX_QUERY_DEPTH;
import static ua.com.fielden.platform.web_api.GraphQLService.WARN_INSUFFICIENT_MAX_QUERY_DEPTH;

/// Basic IoC module for server web applications, which should be extended by an application-specific IoC module.
///
/// This IoC provides all the necessary bindings for:
///
/// - Applications settings (refer [IApplicationSettings]);
/// - Serialisation mechanism;
/// - Essential DAO interfaces [IUser], [IAuthorisationModel], and more;
/// - Provides application main menu configuration related DAO bindings.
/// - Binding of audit-entity types (subtypes of [AbstractAuditEntity] and [AbstractAuditProp]).
///
/// A special audit-entity generation mode is supported, which ignores audit-entity types that were missing at the time of application launch.
/// This mode enables the primordial generation of audit-entity types.
///
/// Instantiation of singletons occurs in accordance with <a href="https://github.com/google/guice/wiki/Scopes#eager-singletons">Guice Eager Singletons</a>,
/// where values of [Workflows] are mapped to [Stage].
///
public class BasicWebServerIocModule extends CompanionIocModule {
    private static final Logger LOGGER = getLogger(BasicWebServerIocModule.class);

    private final Properties props;
    protected final IApplicationDomainProvider applicationDomainProvider;
    private final Class<? extends IAuthorisationModel> authorisationModelType;

    public BasicWebServerIocModule(
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainEntityTypes,
            final Properties props)
    {
        super(props, domainEntityTypes);
        this.props = props;
        this.applicationDomainProvider = applicationDomainProvider;
        // Currently there is no good way of binding the default implementation of IAuthorisationModel other than having multiple constructors in this module.
        // Good old @ImplementedBy cannot be used because the default implementation resides in platform-dao, while the interface is in platform-pojo-bl.
        this.authorisationModelType = ServerAuthorisationModel.class;
    }

    public BasicWebServerIocModule(
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
        // bind application-specific constants
        bindConstant().annotatedWith(Names.named("app.name")).to(props.getProperty("app.name"));
        bindConstant().annotatedWith(Names.named("help.defaultUri")).to(props.getProperty("help.defaultUri", ""));
        bindConstant().annotatedWith(Names.named("reports.path")).to("");
        bindConstant().annotatedWith(Names.named("domain.path")).to(props.getProperty("domain.path"));
        bindConstant().annotatedWith(Names.named("domain.package")).to(props.getProperty("domain.package"));
        bindConstant().annotatedWith(Names.named("tokens.path")).to(props.getProperty("tokens.path"));
        bindConstant().annotatedWith(Names.named("tokens.package")).to(props.getProperty("tokens.package"));
        bindConstant().annotatedWith(Names.named("workflow")).to(props.getProperty("workflow"));
        bindConstant().annotatedWith(Names.named("attachments.location")).to(props.getProperty("attachments.location"));
        bindConstant().annotatedWith(Names.named("attachments.allowlist")).to(props.getProperty("attachments.allowlist", ""));
        bindConstant().annotatedWith(Names.named("email.smtp")).to(props.getProperty("email.smtp"));
        bindConstant().annotatedWith(Names.named("email.fromAddress")).to(props.getProperty("email.fromAddress"));
        bindConstant().annotatedWith(Names.named("independent.time.zone")).to(Boolean.parseBoolean(props.getProperty("independent.time.zone")));
        bindConstant().annotatedWith(Names.named("externalSites.allowlist")).to(props.getProperty("externalSites.allowlist", ""));
        bindConstant().annotatedWith(Names.named("externalSites.expiresIn")).to(props.getProperty("externalSites.expiresIn", ""));
        bindConstant().annotatedWith(Names.named("currency.symbol")).to(props.getProperty("currency.symbol", "$"));
        final boolean enableWebApi = Boolean.parseBoolean(props.getProperty("web.api"));
        bindConstant().annotatedWith(Names.named("web.api")).to(enableWebApi);
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
        bindConstant().annotatedWith(Names.named("dates.weekStart")).to(Integer.parseInt(props.getProperty("dates.weekStart", "1"))); // 1 - Monday
        bindConstant().annotatedWith(Names.named("dates.finYearStartDay")).to(Integer.parseInt(props.getProperty("dates.finYearStartDay", "1"))); // 1 - the first day of the month
        bindConstant().annotatedWith(Names.named("dates.finYearStartMonth")).to(Integer.parseInt(props.getProperty("dates.finYearStartMonth", "7"))); // 7 - July, the 1st of July is the start of Fin Year in Australia

        // Auditing
        bindConstant().annotatedWith(Names.named(AUDIT_PATH)).to(props.getProperty(AUDIT_PATH, ""));
        bindConstant().annotatedWith(Names.named(AUDIT_MODE)).to(props.getProperty(AUDIT_MODE, ""));

        // Dates
        bindConstant().annotatedWith(Names.named("dates.dateFormat")).to(props.getProperty("dates.dateFormat", IDates.DEFAULT_DATE_FORMAT));
        bindConstant().annotatedWith(Names.named("dates.timeFormat")).to(props.getProperty("dates.timeFormat", IDates.DEFAULT_TIME_FORMAT));
        bindConstant().annotatedWith(Names.named("dates.timeFormatWithMillis")).to(props.getProperty("dates.timeFormatWithMillis", IDates.DEFAULT_TIME_FORMAT_WITH_MILLIS));
        bindConstant().annotatedWith(Names.named("dates.dateFormat.web")).to(props.getProperty("dates.dateFormat.web", IDates.DEFAULT_DATE_FORMAT_WEB));
        bindConstant().annotatedWith(Names.named("dates.timeFormat.web")).to(props.getProperty("dates.timeFormat.web", IDates.DEFAULT_TIME_FORMAT_WEB));
        bindConstant().annotatedWith(Names.named("dates.timeFormatWithMillis.web")).to(props.getProperty("dates.timeFormatWithMillis.web", IDates.DEFAULT_TIME_FORMAT_WEB_WITH_MILLIS));

        bind(IApplicationSettings.class).to(ApplicationSettings.class);
        requireBinding(ISecurityTokenProvider.class);
        // serialisation related binding
        requireBinding(ISerialisationClassProvider.class);
        bind(ISerialiser.class).to(Serialiser.class);

        requireBinding(IFilter.class);

        bind(ICriteriaGenerator.class).to(CriteriaGenerator.class);
        bind(IGeneratedEntityController.class).to(GeneratedEntityDao.class);

        bind(ISecurityTokenController.class).to(SecurityTokenController.class);
        bind(IAuthorisationModel.class).to(authorisationModelType);
        install(new AuthorisationIocModule());

        if (enableWebApi) { // in case where Web API has been turned on in application.properties ...
            // ... bind Web API to platform-dao GraphQL-based implementation
            bind(IWebApi.class).to(GraphQLService.class);
        }

        requestStaticInjection(MultiInheritanceEntityVerificationService.class);
        requestStaticInjection(EntityQueryCriteriaUtils.class);
    }

    public Properties getProps() {
        return props;
    }

    /// A provider used by Guice to instantiate [IApplicationDomainProvider].
    /// This replaces a direct binding such as `bind(IApplicationDomainProvider.class).toInstance(applicationDomainProvider)`
    /// in order to support different domain providers depending on whether generic auditing is enabled.
    /// 
    @Provides
    @Singleton
    IApplicationDomainProvider provideApplicationDomain(
            final IAuditTypeFinder auditTypeFinder,
            final AuditingMode auditingMode)
    {
        final var newEntityTypes = applicationDomainProvider.entityTypes().stream()
                .<Class<? extends AbstractEntity<?>>> mapMulti((type, sink) -> {
                    sink.accept(type);
                    // For audited types, register their audit types, which must exist, unless we are running in the audit generation mode.
                    if (isAudited(type)) {
                        switch (auditingMode) {
                            case GENERATION -> {
                                final var navigator = auditTypeFinder.navigate(type);
                                navigator.allAuditEntityTypes().forEach(a3tType -> {
                                    sink.accept(a3tType);
                                    navigator.findAuditPropType(getAuditTypeVersion(a3tType)).ifPresent(sink);
                                });
                                navigator.findSynAuditEntityType().ifPresent(synAuditType -> {
                                    sink.accept(synAuditType);
                                    navigator.findSynAuditPropType().ifPresent(sink);
                                });
                            }
                            case ENABLED -> {
                                final var navigator = auditTypeFinder.navigate(type);
                                navigator.allAuditEntityTypes().forEach(a3tType -> {
                                    sink.accept(a3tType);
                                    sink.accept(navigator.auditPropType(getAuditTypeVersion(a3tType)));
                                });
                                final var synAuditType = navigator.synAuditEntityType();
                                sink.accept(synAuditType);
                                sink.accept(navigator.synAuditPropType());
                            }
                            case DISABLED -> {}
                        }
                    }
                })
                .collect(toImmutableList());

        return () -> newEntityTypes;
    }

}
