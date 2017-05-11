package ua.com.fielden.platform.eql.dbschema;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
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
     * @param mapTo
     * @param persistedType
     * @param required
     * @return
     */
    public Set<ColumnDefinition> extractFromProperty(final String propName, final Class<?> propType, final MapTo mapTo, final PersistentType persistedType, final boolean required) {

        final Set<ColumnDefinition> result = new LinkedHashSet<>();

        if (isUnionEntityType(propType)) {
            // TODO UnionEntity prop (persistedType ignore, required ignore (set to FALSE), l,s,p from MapTo ignore)
        } else {
            final Object hibTypeConverter = hibernateTypeDeterminer.getHibernateType(propType, persistedType);
            final String columnName = nameClause(propName, mapTo.value());
            final int length = mapTo.length();
            final int precision = mapTo.precision();
            final int scale = mapTo.scale();

            if (hibTypeConverter instanceof Type) {
                result.add(new ColumnDefinition(!required, columnName, propType, jdbcSqlTypeFor((Type) hibTypeConverter), length, scale, precision, mapTo.defaultValue()));
            } else if (hibTypeConverter instanceof UserType) {
                result.add(new ColumnDefinition(!required, columnName, propType, jdbcSqlTypeFor((UserType) hibTypeConverter), length, scale, precision, mapTo.defaultValue()));
            } else if (hibTypeConverter instanceof CompositeUserType) {
                final CompositeUserType compositeUserType = (CompositeUserType) hibTypeConverter;
                final List<Pair<String, Integer>> subprops = jdbcSqlTypeFor(compositeUserType);
                for (final Pair<String, Integer> pair : subprops) {
                    final String parentColumn = columnName;
                    final Field subpropField = Finder.findFieldByName(compositeUserType.returnedClass(), pair.getKey());
                    final MapTo subpropMapTo = getAnnotation(subpropField, MapTo.class);
                    final String subpropColumnNameSuggestion = subpropMapTo.value();
                    final Integer subpropLength = subpropMapTo.length();
                    final Integer subpropPrecision = subpropMapTo.precision();
                    final Integer subpropScale = subpropMapTo.scale();
                    final String subpropColumnName = subprops.size() == 1 ? parentColumn
                            : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(subpropColumnNameSuggestion) ? pair.getKey().toUpperCase() : subpropColumnNameSuggestion));

                    result.add(new ColumnDefinition(!required, subpropColumnName, subpropField.getType(), pair.getValue(), subpropLength, subpropScale, subpropPrecision, subpropMapTo.defaultValue()));
                }
            } else {
                throw new DbSchemaException(String.format("Unexpected hibernate type converter [%s].", hibTypeConverter));
            }
        }

        return result;
    }

    private String nameClause(final String propName, final String columnNameSuggestion) {
        return (StringUtils.isNotBlank(columnNameSuggestion) ? columnNameSuggestion : propName.toUpperCase() + "_");
    }
}