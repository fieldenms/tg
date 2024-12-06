package ua.com.fielden.platform.audit;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.util.List;

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

    @Provides
    @Singleton
    IAuditTypeFinder provideAuditTypeFinder(final @Named(AUDIT_PATH) String auditPath) {
        final List<Class<?>> types;
        try {
            types = ClassesRetriever.getAllClassesInPackage(auditPath, "");
        } catch (final Exception ex) {
            throw new ReflectionException("Error while retrieving all classes from JAR or directory at [%s]".formatted(auditPath), ex);
        }
        return new AuditTypeFinder(types);
    }


}
