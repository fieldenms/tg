package ua.com.fielden.platform.audit;

import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.parser.IValueParser;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.util.List;
import java.util.Optional;

import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;
import static java.util.function.Predicate.not;
import static ua.com.fielden.platform.parser.IValueParser.enumIgnoreCaseParser;
import static ua.com.fielden.platform.parser.IValueParser.optPropertyParser;
import static ua.com.fielden.platform.utils.MiscUtilities.mkProperties;

/**
 * IoC module that provides auditing configuration that pertains to modelling.
 *
 * <h4> Requirements </h4>
 * <ul>
 *   <li> Application property {@link #AUDIT_PATH} must be specified.
 * </ul>
 *
 * <h4> Bindings </h4>
 * <ul>
 *   <li> Discovery of audit types through {@link IAuditTypeFinder}.
 *   <li> Auditing mode.
 *   <p> There are several ways to specify the auditing mode, ordered from highest priority to lowest:
 *   <ol>
 *     <li> System property.
 *     <li> Application property.
 *     <li> IoC configuration, via {@link #withAuditingMode(AuditingMode)}.
 *   </ol>
 *   If the auditing mode is not specified, the default is {@link AuditingMode#ENABLED}.
 * </ul>
 */
public final class AuditingIocModule extends AbstractPlatformIocModule {

    /**
     * Application property that specifies the location of class files for audit types.
     * <ul>
     *   <li> For development environment - a path to a directory (e.g., {@code app-pojo-bl/target/classes}).
     *   <li> For deployment - a path to a JAR (e.g., {@code libs/app-pojo-bl-VERSION.jar}).
     * </ul>
     * <p>
     * This name is to be used with the {@link jakarta.inject.Named} annotation.
     */
    public static final String AUDIT_PATH = "audit.path";

    /**
     * Application and system property that specifies the auditing mode.
     * <p>
     * This name is to be used with the {@link jakarta.inject.Named} annotation.
     */
    public static final String AUDIT_MODE = "audit.mode";

    private static final IValueParser<Object, AuditingMode> auditingModeParser = enumIgnoreCaseParser(AuditingMode.values());

    /** Named binding for the default {@link AuditingMode}, which is specified via {@link #withAuditingMode(AuditingMode)}. */
    private static final String DEFAULT_AUDITING_MODE = "DEFAULT_AUDITING_MODE";

    /**
     * Returns an IoC module that sets the specified auditing mode.
     */
    public static Module withAuditingMode(final AuditingMode mode) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                newOptionalBinder(binder(), Key.get(AuditingMode.class, Names.named(DEFAULT_AUDITING_MODE)))
                        .setBinding().toInstance(mode);
            }
        };
    }

    @Override
    protected void configure() {
        super.configure();

        newOptionalBinder(binder(), Key.get(AuditingMode.class, Names.named(DEFAULT_AUDITING_MODE)))
                .setDefault().toInstance(AuditingMode.ENABLED);

        requestStaticInjection(LogAuditingMode.class);
    }

    @Provides
    @Singleton
    IAuditTypeFinder provideAuditTypeFinder(final @Named(AUDIT_PATH) String auditPath, final AuditingMode auditingMode) {
        return switch (auditingMode) {
            // No need to scan the audit path if auditing is disabled
            case DISABLED -> new AuditTypeFinder(List.of(), auditingMode);
            default -> {
                final List<Class<?>> types;
                try {
                    types = ClassesRetriever.getAllClassesInPackage(auditPath, "");
                } catch (final Exception ex) {
                    throw new ReflectionException("Error while retrieving all classes from JAR or directory at [%s]".formatted(auditPath), ex);
                }
                yield new AuditTypeFinder(types, auditingMode);
            }
        };
    }

    /**
     * Decides which of the configured auditing modes is the definite one.
     */
    @Provides
    @Singleton
    AuditingMode auditingMode(
            final @Named(AUDIT_MODE) String appAuditMode,
            final @Named(DEFAULT_AUDITING_MODE) AuditingMode defaultAuditingMode)
    {
        final var parser = optPropertyParser(AUDIT_MODE, auditingModeParser);
        return parser.apply(System.getProperties())
                .refineError(() -> "Could not parse system property [%s]".formatted(AUDIT_MODE))
                .getOrThrow()
                .or(() -> Optional.of(appAuditMode)
                        .filter(not(String::isBlank))
                        .flatMap(it -> parser.apply(mkProperties(AUDIT_MODE, it))
                                .refineError(() -> "Could not parse application property [%s]".formatted(AUDIT_MODE))
                                .getOrThrow()))
                .orElse(defaultAuditingMode);
    }

    private static class LogAuditingMode {

        private static final Logger LOGGER = LogManager.getLogger();

        @Inject
        static void run(final AuditingMode auditingMode) {
            LOGGER.info(() -> "Active auditing mode: %s".formatted(auditingMode));
        }
    }

}
