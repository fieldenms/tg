package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.DbVersion;

public class TypeCastAsString implements ITypeCast {

    private static final Map<Integer, TypeCastAsString> instances = new HashMap<>();
    
    private final int length;

    private TypeCastAsString(final int length) {
        super();
        this.length = length;
    }
    
    public static TypeCastAsString getInstance(final int length) {
        final TypeCastAsString existing = instances.get(length);
        final TypeCastAsString result;
        if (existing != null) {
            result = existing;
        } else {
            result = new TypeCastAsString(length);
            instances.put(length, result);
        }
        return result;
    }

    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
        if (H2.equals(dbVersion) || POSTGRESQL.equals(dbVersion)  || MSSQL.equals(dbVersion)) {
            return "CAST(" + argument + " AS VARCHAR(" + length + "))";
        } else {
            return argument;
        }
    }
}
 