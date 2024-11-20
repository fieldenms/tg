package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

import java.util.Objects;

/**
 * @param value  can be {@code null} in case of yield statement
 */
public record Value3 (Object value, String paramName, PropType type)
        implements ISingleOperand3, ToString.IFormattable
{

    public Value3(final Object value, final PropType type) {
        this(value, null, type);
    }

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        if (value == null) {
            return " NULL ";
        } else {
            return paramName == null ? (value instanceof String ? "'" + value + "'" : value.toString()) : ":" + paramName;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
               || obj instanceof Value3 that
                  && Objects.equals(value, that.value) && Objects.equals(paramName, that.paramName);
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("value", value)
                .addIfNotNull("paramName", paramName)
                .add("type", type)
                .$();
    }

}
