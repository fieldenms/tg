package ua.com.fielden.platform.entity.query.fluent;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.utils.Pair;

public class TypeCastAsDecimal implements ITypeCast {

    private static final Map<Pair<Integer, Integer>, TypeCastAsDecimal> instances = new HashMap<>();

    private final int scale;
    private final int precision;

    private TypeCastAsDecimal(final int scale, final int precision) {
        super();
        this.scale = scale;
        this.precision = precision;
    }

    public static TypeCastAsDecimal getInstance(final int scale, final int precision) {
        final Pair<Integer, Integer> params = new Pair<>(scale, precision);
        final TypeCastAsDecimal existing = instances.get(params);
        final TypeCastAsDecimal result;
        if (existing != null) {
            result = existing;
        } else {
            result = new TypeCastAsDecimal(scale, precision);
            instances.put(params, result);
        }
        return result;
    }

    
    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
        if (DbVersion.H2.equals(dbVersion)) {
            return "CAST(" + argument + " AS DECIMAL(" + precision + "," + scale + "))";
        } else {
            return argument;
        }
    }
}