package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.metadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.model.PropColumn;

public class LongPropertyMetadata implements Comparable<LongPropertyMetadata> {
    private final String name;
    private final Class<?> javaType;
    private final Object hibType;
    private final boolean nullable;

    private final PropColumn column;
    private final List<LongPropertyMetadata> subitems;
    private final ExpressionModel expressionModel;
    private final boolean aggregatedExpression; // contains aggregation function on the root level (i.e. Totals in entity centre tree)

    private LongPropertyMetadata(final Builder builder) {
        name = builder.name;
        javaType = builder.javaType;
        hibType = builder.hibType;
        column = builder.column;
        subitems = builder.subitems;
        nullable = builder.nullable;
        expressionModel = builder.expressionModel;
        aggregatedExpression = builder.aggregatedExpression;
    }

    public IUserTypeInstantiate getHibTypeAsUserType() {
        return hibType instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibType : null;
    }

    public ICompositeUserTypeInstantiate getHibTypeAsCompositeUserType() {
        return hibType instanceof ICompositeUserTypeInstantiate ? (ICompositeUserTypeInstantiate) hibType : null;
    }

    public String getSinglePropertyOfCompositeUserType() {
        final ICompositeUserTypeInstantiate compositeUserTypeInstance = getHibTypeAsCompositeUserType();
        if (compositeUserTypeInstance != null) {
            final String[] propNames = compositeUserTypeInstance.getPropertyNames();
            if (propNames.length == 1) {
                return propNames[0];
            }
        }
        return null;
    }

    public boolean isCompositeProperty() {
        return getHibTypeAsCompositeUserType() != null;
    }

    public String getTypeString() {
        if (hibType != null) {
            return hibType.getClass().getName();
        } else {
            return null;
        }
    }

    public Set<LongPropertyMetadata> getCompositeTypeSubprops(final ICompositeUserTypeInstantiate hibTypeP) {
        final Set<LongPropertyMetadata> result = new HashSet<LongPropertyMetadata>();
        final List<PropColumn> columns = new ArrayList<>();
        final List<String> subprops = Arrays.asList(hibTypeP.getPropertyNames());
        final List<Object> subpropsTypes = Arrays.asList(hibTypeP.getPropertyTypes());
        if (subprops.size() == 1) {
            final Object hibType = subpropsTypes.get(0);
            if (expressionModel != null) {
                result.add(new LongPropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) hibType).getReturnedClass(), nullable).expression(getExpressionModel()).aggregatedExpression(aggregatedExpression).hibType(hibType).build());
            } else if (columns.size() == 0) { // synthetic entity context
                result.add(new LongPropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) hibType).getReturnedClass(), nullable).aggregatedExpression(aggregatedExpression).hibType(hibType).build());
            } else {
                result.add(new LongPropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) subpropsTypes.get(0)).getReturnedClass(), nullable).column(columns.get(0)).hibType(subpropsTypes.get(0)).build());
            }
        } else {
            int index = 0;
            for (final String subpropName : subprops) {
                final PropColumn column = columns.get(index);
                final Object hibType = subpropsTypes.get(index);
                result.add(new LongPropertyMetadata.Builder(name + "." + subpropName, ((Type) hibType).getReturnedClass(), nullable).column(column).hibType(hibType).build());
                index = index + 1;
            }
        }

        return result;
    }

    public Set<LongPropertyMetadata> getComponentTypeSubprops() {
        final Set<LongPropertyMetadata> result = new HashSet<LongPropertyMetadata>();
        final PropertyCategory category = null;
        if (UNION_ENTITY_HEADER == category) {
            final List<Field> propsFields = unionProperties((Class<? extends AbstractUnionEntity>) javaType);
            for (final Field subpropField : propsFields) {
                final MapTo mapTo = getAnnotation(subpropField, MapTo.class);
                if (mapTo == null) {
                    throw new EqlException(format("Property [%s] in union entity type [%s] has no @MapTo.", subpropField.getName(), javaType));
                }
                final PropColumn column = new PropColumn(getColumn() + "_" + (isEmpty(mapTo.value()) ? subpropField.getName() : mapTo.value()));
                result.add(new LongPropertyMetadata.Builder(name + "." + subpropField.getName(), subpropField.getType(), true).column(column).hibType(LongType.INSTANCE).build());
            }
        }
        return result;
    }

    public PropColumn getColumn() {
        return column;
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Object getHibType() {
        return hibType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public ExpressionModel getExpressionModel() {
        return expressionModel;
    }

    public boolean isAggregatedExpression() {
        return aggregatedExpression;
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
        result = prime * result + (aggregatedExpression ? 1231 : 1237);
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
        if (aggregatedExpression != other.aggregatedExpression) {
            return false;
        }

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
        private boolean aggregatedExpression = false;

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

        public Builder aggregatedExpression(final boolean val) {
            aggregatedExpression = val;
            return this;
        }

    }
}