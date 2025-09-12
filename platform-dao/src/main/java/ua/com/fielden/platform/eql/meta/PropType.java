package ua.com.fielden.platform.eql.meta;

import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import static ua.com.fielden.platform.persistence.HibernateConstants.*;

/**
 * A data container for property type composition.
 * <p>
 * The <i>null type</i> is represented by {@link #NULL_TYPE}.
 *
 * @author TG Team
 */
public final class PropType {

    public final static PropType NULL_TYPE = new PropType(null, null);
    public final static PropType LONG_PROP_TYPE = new PropType(Long.class, H_LONG);
    public final static PropType INTEGER_PROP_TYPE = new PropType(Integer.class, H_INTEGER);
    public final static PropType INT_PROP_TYPE = new PropType(int.class, H_INTEGER);
    public final static PropType STRING_PROP_TYPE = new PropType(String.class, H_STRING);
    public final static PropType NSTRING_PROP_TYPE = new PropType(String.class, H_NSTRING);
    public final static PropType DATE_PROP_TYPE = new PropType(Date.class, H_DATE);
    public final static PropType DATETIME_PROP_TYPE = new PropType(Date.class, H_DATETIME);
    public final static PropType UTCDATETIME_PROP_TYPE = new PropType(Date.class, H_UTCDATETIME);
    public final static PropType BIGDECIMAL_PROP_TYPE = new PropType(BigDecimal.class, H_BIGDECIMAL);

    public static PropType propType(final Class<?> javaType, final Object hibType) {
        if (javaType == null && hibType == null) {
            return NULL_TYPE;
        }
        return new PropType(javaType, hibType);
    }

    private final @Nullable Class<?> javaType;
    private final @Nullable Object hibType;

    private PropType(final Class<?> javaType, final Object hibType) {
        this.javaType = javaType;
        this.hibType = hibType;
    }

    public @Nullable Class<?> javaType() {
        return javaType;
    }

    public @Nullable Object hibType() {
        return hibType;
    }

    public boolean isNull() {
        return this == NULL_TYPE;
    }

    public boolean isNotNull() {
        return this != NULL_TYPE;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof PropType that &&
                Objects.equals(this.javaType, that.javaType) && Objects.equals(this.hibType, that.hibType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(javaType, hibType);
    }

    @Override
    public String toString() {
        return "PropType(Java=%s, Hibernate=%s)".formatted(javaType == null ? "null" : javaType.getTypeName(), hibType);
    }

}
