package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;

public class LongPropertyMetadata {
    public final String name;
    public final Class<?> javaType;
    public final Object hibType;
    public final Boolean required;

    public final PropColumn column;
    private final List<LongPropertyMetadata> subitems;
    public final ExpressionModel expressionModel;
    
    private LongPropertyMetadata(final Builder builder) {
        name = Objects.requireNonNull(builder.name);
        javaType = Objects.requireNonNull(builder.javaType);
        hibType = builder.hibType;
        required = builder.required;
        column = builder.column;
        subitems = builder.subitems;
        expressionModel = builder.expressionModel;
    }

    public List<LongPropertyMetadata> subitems() {
        return unmodifiableList(subitems);
    }


    public String getName() {
        return name;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + javaType.hashCode();
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + (required ? 0 : (required ? 1231 : 1237));
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime * result + ((expressionModel == null) ? 0 : expressionModel.hashCode());
        result = prime * result + subitems.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof LongPropertyMetadata)) {
            return false;
        }

        final LongPropertyMetadata other = (LongPropertyMetadata) obj;

        return Objects.equals(name, other.name) &&
                Objects.equals(javaType, other.javaType) &&
                Objects.equals(hibType, other.hibType) && 
                Objects.equals(required, other.required) && 
                Objects.equals(expressionModel, other.expressionModel) &&
                Objects.equals(subitems, other.subitems) &&
                Objects.equals(column, other.column);
    }

    public static class Builder {
        private final String name;
        private final Class<?> javaType;
        private Object hibType;
        private Boolean required;

        
        private PropColumn column;
        private final List<LongPropertyMetadata> subitems = new ArrayList<>();
        private ExpressionModel expressionModel;

        public LongPropertyMetadata build() {
            return new LongPropertyMetadata(this);
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