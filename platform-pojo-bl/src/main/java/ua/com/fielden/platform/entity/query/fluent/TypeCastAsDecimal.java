package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.DbVersion;

public class TypeCastAsDecimal implements ITypeCast {

    private final int scale;
    private final int precision;

    public TypeCastAsDecimal(final int scale, final int precision) {
        super();
        this.scale = scale;
        this.precision = precision;
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