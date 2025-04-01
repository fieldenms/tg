package ua.com.fielden.platform.audit;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.util.List;

import static com.google.inject.multibindings.OptionalBinder.newOptionalBinder;

/**
 * IoC module that provides auditing configuration that pertains to modelling.
 *
 * <h4>Requirements</h4>
 * <ul>
 *   <li> Application property {@link #AUDIT_PATH} must be specified.
 * </ul>
 *
 * <h4>Bindings</h4>
 * <ul>
 *   <li> Discovery of audit types through {@link IAuditTypeFinder}.
 *   <li> Default {@link AuditingMode}.
 * </ul>
 */
public final class AuditingIocModule extends AbstractPlatformIocModule {

    /**
     * Name of a required binding that specifies the location of class files for audit types.
     * <ul>
     *   <li> For development environment - a path to a directory (e.g., {@code app-pojo-bl/target/classes}).
     *   <li> For deployment - a path to a JAR (e.g., {@code libs/app-pojo-bl-VERSION.jar}).
     * </ul>
     * <p>
     * This name is to be used with the {@link jakarta.inject.Named} annotation.
     */
    public static final String AUDIT_PATH = "audit.path";

    /**
     * Returns an IoC module that sets the specified auditing mode.
     */
    public static Module withAuditingMode(final AuditingMode mode) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                newOptionalBinder(binder(), AuditingMode.class).setBinding().toInstance(mode);
            }
        };
    }

    @Override
    protected void configure() {
        super.configure();

        newOptionalBinder(binder(), AuditingMode.class).setDefault().toInstance(AuditingMode.ENABLED);
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


}
