package ua.com.fielden.platform.eql.dbschema;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

/**
 * This class is responsible for generating instances of {@link ColumnDefinition} based on the entity property definition. This information then can be used for generation of table DDL.
 * 
 * @author TG Team
 *
 */
public class ColumnDefinitionExtractor {
    private final HibernateTypeDeterminer hibernateTypeDeterminer;
    private static final IsProperty defaultIsPropertyAnnotation = new IsPropertyAnnotation().newInstance();
    
    public ColumnDefinitionExtractor(final Injector hibTypesInjector, final Map<Class<?>, Object> hibTypesDefaults) {
        this.hibernateTypeDeterminer = new HibernateTypeDeterminer(hibTypesInjector, hibTypesDefaults);
    }

    /**
     * Generates column definition for the provided entity property.
     * <p>
     * In the majority of cases the resultant set would contain a single instance. 
     * However, in case of custom user types consisting of more than one field such as {@link Money} the resultant set would contain column definitions for each field.
     * <p>
     * Also properties of union types result in multiple column definitions -- one per each property in the union type.
     * 
     * @param propName
     * @param propType
     * @param isProperty
     * @param mapTo
     * @param persistedType
     * @param required
     * @return
     */
    public Set<ColumnDefinition> extractFromProperty(
            final String propName, 
            final Class<?> propType, 
            final IsProperty isProperty, 
            final MapTo mapTo, 
            final PersistentType persistedType, 
            final boolean required, 
            final boolean unique, 
            final Optional<Integer> compositeKeyMemberOrder) {

        final Set<ColumnDefinition> result = new LinkedHashSet<>();
        final String columnName = nameClause(propName, mapTo.value());
        final Object hibTypeConverter = hibernateTypeDeterminer.getHibernateType(propType, persistedType);
        final int length = isProperty.length();
        final int precision = isProperty.precision();
        final int scale = isProperty.scale();
        
        if (isUnionEntityType(propType)) {
            for (final Field subpropField : unionProperties((Class<? extends AbstractUnionEntity>) propType)) {
                final MapTo mapToUnionSubprop = getAnnotation(subpropField, MapTo.class);
                if (mapToUnionSubprop == null) {
                    throw new DbSchemaException(format("Property [%s] in union entity type [%s] is not annotated MapTo.", subpropField.getName(), propType)); 
                }

                final IsProperty isPropertyUnionSubprop = getAnnotation(subpropField, IsProperty.class);
                if (isPropertyUnionSubprop == null) {
                    throw new DbSchemaException(format("Property [%s] in union entity type [%s] is not annotated IsProperty.", subpropField.getName(), propType)); 
                }
                
                final String unionPropColumnName = columnName + "_" + (isEmpty(mapToUnionSubprop.value()) ? subpropField.getName().toUpperCase() : mapToUnionSubprop.value());
                result.add(new ColumnDefinition(unique, compositeKeyMemberOrder, true, unionPropColumnName, subpropField.getType(), jdbcSqlTypeFor((Type) hibTypeConverter), isPropertyUnionSubprop.length(), isPropertyUnionSubprop.scale(), isPropertyUnionSubprop.precision(), mapToUnionSubprop.defaultValue()));
            }
        } else {
            if (hibTypeConverter instanceof Type) {
                result.add(new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required), columnName, propType, jdbcSqlTypeFor((Type) hibTypeConverter), length, scale, precision, mapTo.defaultValue()));
            } else if (hibTypeConverter instanceof UserType) {
                result.add(new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required), columnName, propType, jdbcSqlTypeFor((UserType) hibTypeConverter), length, scale, precision, mapTo.defaultValue()));
            } else if (hibTypeConverter instanceof CompositeUserType) {
                final CompositeUserType compositeUserType = (CompositeUserType) hibTypeConverter;
                final List<Pair<String, Integer>> subprops = jdbcSqlTypeFor(compositeUserType);
                for (final Pair<String, Integer> pair : subprops) {
                    final String parentColumn = columnName;
                    final Field subpropField = findFieldByName(compositeUserType.returnedClass(), pair.getKey());
                    final MapTo subpropMapTo = getAnnotation(subpropField, MapTo.class);
                    final IsProperty subpropIsProperty = getAnnotation(subpropField, IsProperty.class);
                    final String subpropColumnNameSuggestion = subpropMapTo.value();
                    final int subpropLength = subpropIsProperty.length();

                    // properties of type Money need special handling as the precision and scale for Money.amount can be overridden
                    final int subpropPrecision;
                    if (Money.class.isAssignableFrom(compositeUserType.returnedClass()) && "amount".equals(subpropField.getName()) && precision != IsProperty.DEFAULT_PRECISION) {
                        subpropPrecision = precision; 
                    } else {
                        subpropPrecision = subpropIsProperty.precision();
                    }
                    
                    final int subpropScale;
                    if (Money.class.isAssignableFrom(compositeUserType.returnedClass()) && "amount".equals(subpropField.getName()) && scale != IsProperty.DEFAULT_SCALE) {
                        subpropScale = scale; 
                    } else {
                        subpropScale = subpropIsProperty.scale();
                    }
                    
                    
                    final String subpropColumnName = subprops.size() == 1 ? parentColumn
                            : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(subpropColumnNameSuggestion) ? pair.getKey().toUpperCase() : subpropColumnNameSuggestion));

                    result.add(new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required), subpropColumnName, subpropField.getType(), pair.getValue(), subpropLength, subpropScale, subpropPrecision, subpropMapTo.defaultValue()));
                }
            } else {
                throw new DbSchemaException(format("Unexpected hibernate type converter [%s] for property [%s] of type [%s].", hibTypeConverter, propName, propType));
            }
        }

        return result;
    }

    private boolean isNullable(final Class<?> propType, final boolean required) {
        return !required && !boolean.class.isAssignableFrom(propType) && !Boolean.class.isAssignableFrom(propType);
    }
    
    public ColumnDefinition extractVersionProperty() {
        final Field versionField = Finder.getFieldByName(AbstractEntity.class, VERSION);
        return extractFromProperty(versionField.getName(), versionField.getType(), defaultIsPropertyAnnotation, getAnnotation(versionField, MapTo.class), null, true, false, empty()).iterator().next();
    }

    public ColumnDefinition extractIdProperty() {
        final Field idField = Finder.getFieldByName(AbstractEntity.class, ID);
        return extractFromProperty(idField.getName(), idField.getType(), defaultIsPropertyAnnotation, getAnnotation(idField, MapTo.class), null, true, false, empty()).iterator().next();
    }

    public Optional<ColumnDefinition> extractSimpleKeyProperty(final Class<? extends AbstractEntity<?>> entityType) {
        if (isCompositeEntity(entityType) || isOneToOne(entityType)) {
            return empty();
        }
        final Field keyField = Finder.getFieldByName(AbstractEntity.class, KEY);
        return of(extractFromProperty(keyField.getName(), getKeyType(entityType), getAnnotation(keyField, IsProperty.class), getAnnotation(keyField, MapTo.class), null, true, true, empty()).iterator().next());
    }
    
    private String nameClause(final String propName, final String columnNameSuggestion) {
        return (isNotBlank(columnNameSuggestion) ? columnNameSuggestion : propName.toUpperCase() + "_");
    }
}