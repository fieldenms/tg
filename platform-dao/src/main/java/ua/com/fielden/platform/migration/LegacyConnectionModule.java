package ua.com.fielden.platform.migration;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

/**
 * A module used purely for providing JDBC connection in a configurable manner during data migration between two different databases.
 *
 * @author TG Team
 *
 */
public class LegacyConnectionModule extends AbstractModule {

    private final Provider<? extends Connection> provider;

    /**
     * Principle constructor, which requires a provider for construction of the JDBC connection.
     * @param provider
     */
    public LegacyConnectionModule(final Provider<Connection> provider){
	this.provider = provider;
    }

    @Override
    protected void configure() {
	bind(Connection.class).toProvider(provider);
    }

}
