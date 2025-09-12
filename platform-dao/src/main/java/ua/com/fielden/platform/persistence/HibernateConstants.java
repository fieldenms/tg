package ua.com.fielden.platform.persistence;

import org.hibernate.type.*;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;

public final class HibernateConstants {

    public static final Type H_ENTITY = LongType.INSTANCE;
    public static final Type H_LONG = LongType.INSTANCE;
    public static final Type H_INTEGER = IntegerType.INSTANCE;
    public static final Type H_BIGDECIMAL = BigDecimalType.INSTANCE;
    public static final Type H_STRING = StringType.INSTANCE;
    public static final Type H_NSTRING = StringNVarcharType.INSTANCE;
    public static final Type H_DATE = DateType.INSTANCE;
    public static final Type H_DATETIME = DateTimeType.INSTANCE;
    public static final Type H_UTCDATETIME = UtcDateTimeType.INSTANCE;
    public static final Type H_BOOLEAN = YesNoType.INSTANCE;
    public static final String Y = "Y";
    public static final String N = "N";

    private HibernateConstants() {}

}
