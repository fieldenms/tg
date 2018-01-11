package ua.com.fielden.platform.ddl;

import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;

/**
 * This metadata provider is needed for the scheme export with Hibernate.
 * Refer https://stackoverflow.com/questions/34612019/programmatic-schemaexport-schemaupdate-with-hibernate-5-and-spring-4 for more details.
 * <p>
 * An important part there is to register this class as a service to be located at runtime by specifying its full name in
 * <code>src/main/resources/META-INF/services/org.hibernate.boot.spi.SessionFactoryBuilderFactory</code> for a specific application that needs to use it.
 * 
 * @author TG Team
 *
 */
public class MetadataProvider implements SessionFactoryBuilderFactory {

    private static MetadataImplementor metadata;

    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(final MetadataImplementor metadata, final SessionFactoryBuilderImplementor defaultBuilder) {
        MetadataProvider.metadata = metadata; // this should happen just once
        return defaultBuilder; //Just return the one provided in the argument itself. All we care about is the metadata :)
    }

    public static MetadataImplementor getMetadata() {
        return metadata;
    }
}