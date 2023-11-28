package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class EqlPropertyMetadata implements Comparable<EqlPropertyMetadata> {
    public final String name;
    public final Class<?> javaType;
    public final Object hibType;
    public final boolean required;
    public final boolean critOnly;

    public final PropColumn column;
    private final List<EqlPropertyMetadata> subitems;
    public final ExpressionModel expressionModel;
    public final boolean implicit;

    private EqlPropertyMetadata(final Builder builder) {
        name = Objects.requireNonNull(builder.name);
        javaType = Objects.requireNonNull(builder.javaType);
        hibType = builder.hibType;
        required = builder.required;
        critOnly = builder.critOnly;
        column = builder.column;
        subitems = builder.subitems;
        expressionModel = builder.expressionModel;
        implicit = builder.implicit;
    }

    public List<EqlPropertyMetadata> subitems() {
        return unmodifiableList(subitems);
    }

    public boolean isVirtualKey() {
        return KEY.equals(name) && expressionModel != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + javaType.hashCode();
        result = prime * result + hibType.hashCode();
        result = prime * result + (required ? 0 : (required ? 1231 : 1237));
        result = prime * result + (critOnly ? 0 : (critOnly ? 1231 : 1237));
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((expressionModel == null) ? 0 : expressionModel.hashCode());
        result = prime * result + (implicit ? 0 : (implicit ? 1231 : 1237));
        result = prime * result + subitems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EqlPropertyMetadata)) {
            return false;
        }

        final EqlPropertyMetadata other = (EqlPropertyMetadata) obj;

        return Objects.equals(name, other.name) &&
                Objects.equals(javaType, other.javaType) &&
                Objects.equals(hibType, other.hibType) &&
                Objects.equals(required, other.required) &&
                Objects.equals(critOnly, other.critOnly) &&
                Objects.equals(expressionModel, other.expressionModel) &&
                Objects.equals(implicit, other.implicit) &&
                Objects.equals(subitems, other.subitems) &&
                Objects.equals(column, other.column);
    }

    public static class Builder {
        private final String name;
        private final Class<?> javaType;
        private Object hibType;
        private boolean required;
        private boolean critOnly = false;


        private PropColumn column;
        private final List<EqlPropertyMetadata> subitems = new ArrayList<>();
        private ExpressionModel expressionModel;
        private boolean implicit = false;

        public EqlPropertyMetadata build() {
            return new EqlPropertyMetadata(this);
        }

        public Builder(final String name, final Class<?> javaType, final Object hibType) {
            this.name = name;
            this.javaType = javaType;
            this.hibType = hibType;
        }

        public Builder required() {
            required = true;
            return this;
        }

        public Builder critOnly() {
            critOnly = true;
            return this;
        }

        public Builder required(final boolean isRequired) {
            required = isRequired;
            return this;
        }

        public Builder notRequired() {
            required = false;
            return this;
        }

        public Builder expression(final ExpressionModel val) {
            expressionModel = val;
            return this;
        }

        public Builder implicit() {
            implicit = true;
            return this;
        }

        public Builder column(final PropColumn column) {
            this.column = column;
            return this;
        }

        public Builder subitems(final List<EqlPropertyMetadata> subitems) {
            this.subitems.addAll(subitems);
            return this;
        }
    }

    @Override
    public int compareTo(final EqlPropertyMetadata that) {
        return this.name.compareTo(that.name);
    }
}