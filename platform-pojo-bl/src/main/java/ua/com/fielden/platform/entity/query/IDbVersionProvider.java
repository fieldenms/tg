package ua.com.fielden.platform.entity.query;

@FunctionalInterface
public interface IDbVersionProvider {

    DbVersion dbVersion();

    static IDbVersionProvider constantDbVersion(final DbVersion dbVersion) {
        return () -> dbVersion;
    }

}
