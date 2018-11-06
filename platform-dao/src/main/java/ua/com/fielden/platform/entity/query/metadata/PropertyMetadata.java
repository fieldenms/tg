package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.COMPOSITE_TYPE_HEADER;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.USUAL_PROP;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COMPONENT_DETAILS;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ONE2ONE_ID;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.PRIMITIVE_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC_COMPONENT_DETAILS;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC_COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_DETAILS;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.utils.EntityUtils;

public class PropertyMetadata implements Comparable<PropertyMetadata> {
    private final String name;
    private final Class javaType;
    private final Object hibType;
    private final PropertyCategory category;
    private final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo;
    private final boolean nullable;

    private final List<PropertyColumn> columns;
    private final ExpressionModel expressionModel;
    private final boolean aggregatedExpression; // contains aggregation function on the root level (i.e. Totals in entity centre tree)

    transient private final Logger logger = Logger.getLogger(this.getClass());
    
    private PropertyMetadata(final Builder builder) {
        category = builder.category;
        parentInfo = builder.parentInfo;
        name = builder.name;
        javaType = builder.javaType;
        hibType = builder.hibType;
        columns = builder.columns;
        nullable = builder.nullable;
        expressionModel = builder.expressionModel;
        aggregatedExpression = builder.aggregatedExpression;
    }

    public YieldDetailsType getYieldDetailType() {
        if (aggregatedExpression) {
            return AGGREGATED_EXPRESSION;
        }
        return isCompositeProperty() ? COMPOSITE_TYPE_HEADER : (isUnionEntity() ? YieldDetailsType.UNION_ENTITY_HEADER : USUAL_PROP);
    }

    public boolean affectsMapping() {
        return category.affectsMappings();
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

    public boolean isCalculated() {
        return expressionModel != null && category != COMPONENT_HEADER;
    }

    public boolean isCalculatedCompositeUserTypeHeader() {
        return expressionModel != null && category == COMPONENT_HEADER;
    }

    public boolean isCompositeProperty() {
        return getHibTypeAsCompositeUserType() != null;
    }

    public boolean isEntityOfPersistedType() {
        return isPersistedEntityType(javaType) && !isCollection()/* && !isId()*/;
    }

    public boolean isCollection() {
        return category == COLLECTIONAL;
    }

    public boolean isOne2OneId() {
        return category == ONE2ONE_ID;
    }

    public boolean isUnionEntity() {
        return category == UNION_ENTITY_HEADER;
    }

    public boolean isCompositeKeyExpression() {
        return category == VIRTUAL_OVERRIDE;
    }

    public boolean isSynthetic() {
        return category == SYNTHETIC || category == SYNTHETIC_COMPONENT_HEADER || category == SYNTHETIC_COMPONENT_DETAILS;
    }

    public String getTypeString() {
        if (hibType != null) {
            return hibType.getClass().getName();
        } else {
            return null;
        }
    }

    public Set<PropertyMetadata> getCompositeTypeSubprops() {
        final Set<PropertyMetadata> result = new HashSet<PropertyMetadata>();
        if (COMPONENT_HEADER == category || SYNTHETIC_COMPONENT_HEADER == category || getHibTypeAsCompositeUserType() != null) {
            logger.debug("=== (COMPONENT_HEADER == category) = " + (COMPONENT_HEADER == category) + "=== (SYNTHETIC_COMPONENT_HEADER == category) = " + (SYNTHETIC_COMPONENT_HEADER == category) + " ---- (getHibTypeAsCompositeUserType() != null) = " + (getHibTypeAsCompositeUserType() != null));
            final List<String> subprops = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyNames());
            final List<Object> subpropsTypes = Arrays.asList(((ICompositeUserTypeInstantiate) hibType).getPropertyTypes());
            final PropertyCategory detailsPropCategory = COMPONENT_HEADER == category ?  COMPONENT_DETAILS : SYNTHETIC_COMPONENT_DETAILS;
            if (subprops.size() == 1) {
                final Object hibType = subpropsTypes.get(0);
                if (expressionModel != null) {
                    result.add(new PropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) hibType).getReturnedClass(), nullable, parentInfo).expression(getExpressionModel()).aggregatedExpression(aggregatedExpression).category(detailsPropCategory).hibType(hibType).build());
                } else if (columns.size() == 0) {
                    result.add(new PropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) hibType).getReturnedClass(), nullable, parentInfo).aggregatedExpression(aggregatedExpression).category(detailsPropCategory).hibType(hibType).build());
                } else {
                    result.add(new PropertyMetadata.Builder(name + "." + subprops.get(0), ((Type) subpropsTypes.get(0)).getReturnedClass(), nullable, parentInfo).column(columns.get(0)).category(detailsPropCategory).hibType(subpropsTypes.get(0)).build());
                }
            } else {
                int index = 0;
                for (final String subpropName : subprops) {
                    final PropertyColumn column = columns.get(index);
                    final Object hibType = subpropsTypes.get(index);
                    result.add(new PropertyMetadata.Builder(name + "." + subpropName, ((Type) hibType).getReturnedClass(), nullable, parentInfo).column(column).category(detailsPropCategory).hibType(hibType).build());
                    index = index + 1;
                }
            }

        }
        return result;
    }

    public Set<PropertyMetadata> getComponentTypeSubprops() {
        final Set<PropertyMetadata> result = new HashSet<PropertyMetadata>();
        if (UNION_ENTITY_HEADER == category) {
            final List<Field> propsFields = unionProperties(javaType);
            for (final Field subpropField : propsFields) {
                final MapTo mapTo = getAnnotation(subpropField, MapTo.class);
                if (mapTo == null) {
                    throw new EqlException(format("Property [%s] in union entity type [%s] has no @MapTo.", subpropField.getName(), javaType));
                }
                final PropertyColumn column = new PropertyColumn(getColumn() + "_" + (isEmpty(mapTo.value()) ? subpropField.getName() : mapTo.value()));
                result.add(new PropertyMetadata.Builder(name + "." + subpropField.getName(), subpropField.getType(), true, parentInfo).column(column).category(UNION_ENTITY_DETAILS).hibType(LongType.INSTANCE).build());
            }
        }
        return result;
    }

    public PropertyColumn getColumn() {
        return columns.size() > 0 ? columns.get(0) : null;
    }

    
    public String getName() {
        return name;
    }

    public Class getJavaType() {
        return javaType;
    }

    public Object getHibType() {
        return hibType;
    }

    public PropertyCategory getCategory() {
        return category;
    }

    public List<PropertyColumn> getColumns() {
        return columns;
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
    public String toString() {
        return "\nname = " + name + " javaType = " + (javaType != null ? javaType.getSimpleName() : javaType) + " hibType = "
                + (hibType != null ? hibType/*.getClass().getSimpleName()*/: hibType) + " type = " + category + " aggregatedExpression = " + isAggregatedExpression() + "\ncolumn(s) = " + columns + " nullable = " + nullable
                + " calculated = " + isCalculated() + (isCalculated() ? " exprModel = " + expressionModel : "");
    }

    @Override
    public int compareTo(final PropertyMetadata o) {
        final boolean areEqual = this.equals(o);
        final int nameComp = name.compareTo(o.name);
        return nameComp != 0 ? nameComp : (areEqual ? 0 : 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (aggregatedExpression ? 1231 : 1237);
        result = prime * result + ((columns == null) ? 0 : columns.hashCode());
        result = prime * result + ((expressionModel == null) ? 0 : expressionModel.hashCode());
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nullable ? 1231 : 1237);
        result = prime * result + ((category == null) ? 0 : category.hashCode());
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
        if (!(obj instanceof PropertyMetadata)) {
            return false;
        }
        final PropertyMetadata other = (PropertyMetadata) obj;
        if (aggregatedExpression != other.aggregatedExpression) {
            return false;
        }
        if (columns == null) {
            if (other.columns != null) {
                return false;
            }
        } else if (!columns.equals(other.columns)) {
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
        if (category != other.category) {
            return false;
        }
        return true;
    }

    public static class Builder {
        private final String name;
        private final Class javaType;
        private final boolean nullable;
        private final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo;

        private Object hibType;
        private List<PropertyColumn> columns = new ArrayList<PropertyColumn>();
        private PropertyCategory category;
        private ExpressionModel expressionModel;
        private boolean aggregatedExpression = false;

        public PropertyMetadata build() {
            return new PropertyMetadata(this);
        }

        public Builder(final String name, final Class javaType, final boolean nullable, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) {
            this.name = name;
            this.javaType = javaType;
            this.nullable = nullable;
            this.parentInfo = parentInfo;
        }

        public Builder hibType(final Object val) {
            hibType = val;
            return this;
        }

        public Builder expression(final ExpressionModel val) {
            expressionModel = val;
            return this;
        }

        public Builder category(final PropertyCategory val) {
            category = val;
            return this;
        }

        public Builder column(final PropertyColumn column) {
            columns.add(column);
            return this;
        }

        public Builder aggregatedExpression(final boolean val) {
            aggregatedExpression = val;
            return this;
        }

        public Builder columns(final List<PropertyColumn> columns) {
            this.columns.addAll(columns);
            return this;
        }
    }
}