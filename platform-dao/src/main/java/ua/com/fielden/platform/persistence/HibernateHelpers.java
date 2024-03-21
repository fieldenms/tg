package ua.com.fielden.platform.persistence;

import org.hibernate.dialect.*;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

/**
 * Various utilities for Hibernate.
 */
public final class HibernateHelpers {

    /**
     * Returns the named Hibernate dialect instance.
     *
     * @param name -- binary name of the dialect class
     * @throws ReflectionException if the named Hibernate dialect could not be obtained
     */
    public static Dialect getDialect(final String name) {
        try {
            return (Dialect) Class.forName(name).getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            throw new ReflectionException("Failed to obtain Hibernate dialect [%s]".formatted(name), e);
        }
    }

    /**
     * Returns a Hibernate dialect instance corresponding to the given DB "version" (technically, it's a dialect, not a version).
     * <p>
     * <b>NOTE</b>: This is intended to be used in test environments. The returned dialect's version is chosen arbitrarily.
     * Serious applications should not rely on this utility, but obtain the desired dialect instance properly.
     * 
     * @see #getDbVersion(Dialect)
     */
    public static Dialect getDialect(final DbVersion dbVersion) {
        return switch (dbVersion) {
            case MSSQL -> new SQLServerDialect();
            case ORACLE -> new Oracle8iDialect();
            case MYSQL -> new MySQLDialect();
            case H2 -> new H2Dialect();
            case POSTGRESQL -> new PostgreSQL82Dialect();
        };
    }

    /**
     * Matches a {@link DbVersion} to the given dialect.
     *
     * @throws IllegalArgumentException  if there is no matching {@link DbVersion}
     */
    public static DbVersion getDbVersion(final Dialect dialect) {
        return getDbVersion(dialect.getClass());
    }

    /**
     * Matches a {@link DbVersion} to the given dialect.
     *
     * @throws IllegalArgumentException  if there is no matching {@link DbVersion}
     */
    public static DbVersion getDbVersion(final Class<? extends Dialect> dialect) {
        if (H2Dialect.class.isAssignableFrom(dialect)) {
            return DbVersion.H2;
        } else if (PostgreSQLDialect.class.isAssignableFrom(dialect)) {
            return DbVersion.POSTGRESQL;
        } else if (SQLServerDialect.class.isAssignableFrom(dialect)) {
            return DbVersion.MSSQL;
        } else if (OracleDialect.class.isAssignableFrom(dialect)) {
            return DbVersion.ORACLE;
        } else if (MySQLDialect.class.isAssignableFrom(dialect)) {
            return DbVersion.MYSQL;
        } else {
            throw new IllegalArgumentException("Could not determine DB version from Hibernate dialect [%s].".formatted(
                    dialect.getTypeName()));
        }
    }

    private HibernateHelpers() {}

}
