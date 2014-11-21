package ua.com.fielden.platform.entity.query.fluent;

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
        if (DbVersion.H2.equals(dbVersion)) {
            return "CAST(" + argument + " AS VARCHAR(" + length + "))";
        } else {
            return argument;
        }
    }
}
 