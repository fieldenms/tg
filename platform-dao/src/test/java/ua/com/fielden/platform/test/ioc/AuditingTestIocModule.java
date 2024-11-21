package ua.com.fielden.platform.test.ioc;

import com.google.inject.name.Names;
import jakarta.inject.Named;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

import java.util.Properties;

/**
 * IoC module for platform tests that pertain to auditing.
 * <ul>
 *   <li> {@link #GENERATED_AUDIT_SOURCES_PATH} is bound from a configuration property with the same name if it is present.
 * </ul>
 */
public final class AuditingTestIocModule extends AbstractPlatformIocModule {

    private final Properties properties;

    public AuditingTestIocModule(final Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        super.configure();

        bindConstant()
                .annotatedWith(Names.named(GENERATED_AUDIT_SOURCES_PATH))
                .to(properties.getProperty(GENERATED_AUDIT_SOURCES_PATH, ""));
    }

    /**
     * Name of a binding that specifies a directory path where generated audit sources should be written.
     * If the bound value is an empty {@link String}, generated audit sources will not be written.
     * <p>
     * This name is to be used with the {@link Named} annotation.
     */
    public static final String GENERATED_AUDIT_SOURCES_PATH = "generatedAuditSourcesPath";

}
