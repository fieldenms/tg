package ua.com.fielden.platform.eql.meta;

import static ua.com.fielden.platform.persistence.HibernateConstants.H_BIGDECIMAL;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_DATE;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_DATETIME;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_INTEGER;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_LONG;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_STRING;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_UTCDATETIME;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A data container for property type composition.
 * 
 * @author TG Team
 *
 */
public record PropType(
        Class<?> javaType,
        Object hibType) {
    
    public final static PropType LONG_PROP_TYPE = new PropType(Long.class, H_LONG);
    public final static PropType INTEGER_PROP_TYPE = new PropType(Integer.class, H_INTEGER);
    public final static PropType INT_PROP_TYPE = new PropType(int.class, H_INTEGER);
    public final static PropType STRING_PROP_TYPE = new PropType(String.class, H_STRING);
    public final static PropType DATE_PROP_TYPE = new PropType(Date.class, H_DATE);
    public final static PropType DATETIME_PROP_TYPE = new PropType(Date.class, H_DATETIME);
    public final static PropType UTCDATETIME_PROP_TYPE = new PropType(Date.class, H_UTCDATETIME);
    public final static PropType BIGDECIMAL_PROP_TYPE = new PropType(BigDecimal.class, H_BIGDECIMAL);
}
