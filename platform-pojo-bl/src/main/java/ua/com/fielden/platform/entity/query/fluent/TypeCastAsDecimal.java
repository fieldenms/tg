package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.entity.query.DbVersion.H2;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.utils.Pair;

public class TypeCastAsDecimal implements ITypeCast {

    private static final Map<Pair<Integer, Integer>, TypeCastAsDecimal> instances = new HashMap<>();

    private final int precision;
    private final int scale;

    private TypeCastAsDecimal(final int precision, final int scale) {
        this.precision = precision;
        this.scale = scale;
    }

    public static TypeCastAsDecimal getInstance(final int precision, final int scale) {
        final Pair<Integer, Integer> params = new Pair<>(precision, scale);
        final TypeCastAsDecimal existing = instances.get(params);
        final TypeCastAsDecimal result;
        if (existing != null) {
            result = existing;
        } else {
            result = new TypeCastAsDecimal(precision, scale);
            instances.put(params, result);
        }
        return result;
    }

    
    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
        if (dbVersion == H2) {
            return "CAST(" + argument + " AS DECIMAL(" + precision + "," + scale + "))";
        } else {
            return argument;
        }
    }
}