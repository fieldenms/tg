package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.parser.IValueParser;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.util.List;
import java.util.Optional;

import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static java.util.function.Predicate.not;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.parser.IValueParser.enumIgnoreCaseParser;
import static ua.com.fielden.platform.parser.IValueParser.optPropertyParser;
import static ua.com.fielden.platform.utils.MiscUtilities.mkProperties;

/// IoC module that provides auditing configuration that pertains to modelling.
///
/// ####  Bindings
/// * Discovery of audit types through [IAuditTypeFinder] if [#AUDIT_PATH] is specified.
/// * Auditing mode.
///
/// There are several ways to specify the [auditing mode][#AUDIT_MODE], ordered from highest priority to lowest:
/// 1. System property.
/// 2. Application property.
///
/// If the auditing mode is not specified, the default is [AuditingMode#ENABLED].
///
public final class AuditingIocModule extends AbstractPlatformIocModule {

    /// Application property (**not a system property**) that specifies the location of class files for audit types.
    /// * For development environment - a path to a directory (e.g., `app-pojo-bl/target/classes`).
    /// * For deployment - a path to a JAR (e.g., `libs/app-pojo-bl-VERSION.jar`).
    ///
    /// This name is to be used with the [jakarta.inject.Named] annotation.
    /// An optional binding is created for this name.
    /// Blank values are treated as if absent.
    ///
    public static final String AUDIT_PATH = "audit.path";
    // We could support audit path as a system property, but that would require other IoC modules to use OptionalBinder to bind an audit path.
    // More details can be found in the documentation of OptionalBinder.

    /// System and application property that specifies the auditing mode.
    ///
    /// This name is to be used with the [jakarta.inject.Named] annotation.
    /// An optional binding is created for this name.
    /// Blank values are treated as if absent.
    ///
    /// This name should only be bound to a [String] value.
    /// This means that [AuditingMode#name()] should be used, rather than the enum value itself.
    ///
    /// **NOTE**: Any code outside of this module should inject [AuditingMode] directly instead of this named binding.
    /// This is because the value of a system property, if specified, will be reflected in [AuditingMode], but not in the named binding.
    ///
    public static final String AUDIT_MODE = "audit.mode";

    private static final IValueParser<Object, AuditingMode> auditingModeParser = enumIgnoreCaseParser(AuditingMode.values());

    private static final String
            ERR_MISSING_APP_PROPERTY = "Application property [%s] must be specified when auditing mode is [%s].",
            ERR_RETRIEVING_CLASSES = "Error while retrieving all classes from JAR or directory at [%s].",
            ERR_PARSING_SYSTEM_PROPERTY = "Could not parse system property [%s].",
            ERR_PARSING_APP_PROPERTY = "Could not parse application property [%s].";

    private static final AuditingMode DEFAULT_AUDITING_MODE = AuditingMode.ENABLED;

    private static final Logger LOGGER = getLogger();

    @Override
    protected void configure() {
        super.configure();

        // No defaults are set so that other modules can bind these names as usual, without using OptionalBinder
        newOptionalBinder(binder(), Key.get(String.class, Names.named(AUDIT_PATH)));
        newOptionalBinder(binder(), Key.get(String.class, Names.named(AUDIT_MODE)));

        requestStaticInjection(AuditingIocModule.class);
    }

    @Provides
    @Singleton
    IAuditTypeFinder provideAuditTypeFinder(final @Named(AUDIT_PATH) Optional<String> maybeAuditPath, final AuditingMode auditingMode) {
        return switch (auditingMode) {
            // No need to scan the audit path if auditing is disabled
            case DISABLED -> new AuditTypeFinder(List.of(), auditingMode);
            default -> {
                final var auditPath = maybeAuditPath.filter(not(String::isBlank))
                        .orElseThrow(() -> new ApplicationConfigurationException(ERR_MISSING_APP_PROPERTY.formatted(AUDIT_PATH, auditingMode)));
                final List<Class<?>> types;
                try {
                    types = ClassesRetriever.getAllClassesInPackage(auditPath, "");
                } catch (final Exception ex) {
                    throw new ReflectionException(ERR_RETRIEVING_CLASSES.formatted(auditPath), ex);
                }
                yield new AuditTypeFinder(types, auditingMode);
            }
        };
    }

    /// Decides which of the configured auditing modes is the definite one.
    ///
    @Provides
    @Singleton
    AuditingMode auditingMode(final @Named(AUDIT_MODE) Optional<String> maybeAppAuditMode) {
        final var parser = optPropertyParser(AUDIT_MODE, auditingModeParser);
        return parser.apply(System.getProperties())
                .refineError(() -> ERR_PARSING_SYSTEM_PROPERTY.formatted(AUDIT_MODE))
                .getOrThrow()
                .or(() -> maybeAppAuditMode
                        .filter(not(String::isBlank))
                        .flatMap(it -> parser.apply(mkProperties(AUDIT_MODE, it))
                                .refineError(() -> ERR_PARSING_APP_PROPERTY.formatted(AUDIT_MODE))
                                .getOrThrow()))
                .orElse(DEFAULT_AUDITING_MODE);
    }

    @Inject
    static void logAuditingMode(final AuditingMode auditingMode) {
        LOGGER.info(() -> "Active auditing mode: %s".formatted(auditingMode));
    }

}
