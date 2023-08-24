package ua.com.fielden.platform.eql.meta;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.type.BigDecimalType;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.UtcDateTimeType;

/**
 * A data container for property type composition.
 * 
 * @author TG Team
 *
 */
public record PropType(
        Class<?> javaType,
        Object hibType) {
    
    public final static PropType LONG_PROP_TYPE = new PropType(Long.class, LongType.INSTANCE);
    public final static PropType INTEGER_PROP_TYPE = new PropType(Integer.class, IntegerType.INSTANCE);
    public final static PropType INT_PROP_TYPE = new PropType(int.class, IntegerType.INSTANCE);
    public final static PropType STRING_PROP_TYPE = new PropType(String.class, StringType.INSTANCE);
    public final static PropType DATE_PROP_TYPE = new PropType(Date.class, DateType.INSTANCE);
    public final static PropType DATETIME_PROP_TYPE = new PropType(Date.class, DateTimeType.INSTANCE);
    public final static PropType UTCDATETIME_PROP_TYPE = new PropType(Date.class, UtcDateTimeType.INSTANCE);
    public final static PropType BIGDECIMAL_PROP_TYPE = new PropType(BigDecimal.class, BigDecimalType.INSTANCE);
}