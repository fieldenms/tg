package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class LongPropertyMetadata implements Comparable<LongPropertyMetadata> {
    public final String name;
    public final Class<?> javaType;
    public final Object hibType;
    public final boolean nullable;

    public final PropColumn column;
    private final List<LongPropertyMetadata> subitems;
    public final ExpressionModel expressionModel;
    
    private LongPropertyMetadata(final Builder builder) {
        name = builder.name;
        javaType = builder.javaType;
        hibType = builder.hibType;
        column = builder.column;
        subitems = builder.subitems;
        nullable = builder.nullable;
        expressionModel = builder.expressionModel;
    }

    public List<LongPropertyMetadata> subitems() {
        return unmodifiableList(subitems);
    }

    @Override
    public int compareTo(final LongPropertyMetadata o) {
        final boolean areEqual = this.equals(o);
        final int nameComp = name.compareTo(o.name);
        return nameComp != 0 ? nameComp : (areEqual ? 0 : 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((expressionModel == null) ? 0 : expressionModel.hashCode());
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nullable ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LongPropertyMetadata)) {
            return false;
        }
        final LongPropertyMetadata other = (LongPropertyMetadata) obj;

        if (expressionModel == null) {
            if (other.expressionModel != null) {
                return false;
            }
        } else if (!expressionModel.equals(other.expressionModel)) {
            return false;
        }
        if (hibType == null) {
            if (other.hibType != null) {
                return false;
            }
        } else if (!hibType.equals(other.hibType)) {
            return false;
        }
        if (javaType == null) {
            if (other.javaType != null) {
                return false;
            }
        } else if (!javaType.equals(other.javaType)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nullable != other.nullable) {
            return false;
        }
        return true;
    }

    public static class Builder {
        private final String name;
        private final Class<?> javaType;
        private final boolean nullable;

        private Object hibType;
        private PropColumn column;
        private final List<LongPropertyMetadata> subitems = new ArrayList<>();
        private ExpressionModel expressionModel;

        public LongPropertyMetadata build() {
            return new LongPropertyMetadata(this);
        }

        public Builder(final String name, final Class<?> javaType, final boolean nullable) {
            this.name = name;
            this.javaType = javaType;
            this.nullable = nullable;
        }

        public Builder hibType(final Object val) {
            hibType = val;
            return this;
        }

        public Builder expression(final ExpressionModel val) {
            expressionModel = val;
            return this;
        }

        public Builder column(final PropColumn column) {
            this.column = column;
            return this;
        }

        public Builder subitems(final List<LongPropertyMetadata> subitems) {
            this.subitems.addAll(subitems);
            return this;
        }
    }
}