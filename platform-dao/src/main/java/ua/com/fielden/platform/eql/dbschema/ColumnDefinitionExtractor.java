package ua.com.fielden.platform.eql.dbschema;

import com.google.common.collect.ImmutableMap;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.Pair;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditEntityType;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.eql.dbschema.ColumnIndex.Order.ASC;
import static ua.com.fielden.platform.eql.dbschema.ColumnIndex.Order.DESC;
import static ua.com.fielden.platform.eql.dbschema.HibernateToJdbcSqlTypeCorrespondence.jdbcSqlTypeFor;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// Responsible for generating instances of [ColumnDefinition] based on the definitions of entity properties.
///
/// The resulting column definitions can then be used to generate DDL statements for creating database tables, indexes, and integrity constraints.
///
public class ColumnDefinitionExtractor {

    private static final String
            ERR_UNEXPECTED_HIB_TYPE = "Unexpected Hibernate type [%s] for property [%s.%s].",
            ERR_MISSING_MAP_TO_IN_UNION_ENTITY = "Property [%s] in union entity type [%s] is not annotated MapTo.",
            ERR_MISSING_IS_PROP_IN_UNION_ENTITY = "Property [%s] in union entity type [%s] is not annotated IsProperty.",
            ERR_UNSUPPORTED_HIB_TYPE = "Property [%s] has unsupported Hibernate type: %s.";

    private static final IsProperty DEFAULT_IsProperty_ANNOTATION = new IsPropertyAnnotation().newInstance();

    private final HibernateTypeDeterminer hibernateTypeDeterminer;
    private final Dialect dialect;
    
    public ColumnDefinitionExtractor(final HibernateTypeMappings hibernateTypeMappings, final Dialect dialect) {
        this.hibernateTypeDeterminer = new HibernateTypeDeterminer(hibernateTypeMappings);
        this.dialect = dialect;
    }

    /// Generates a column definition for the specified property.
    /// The returned value is a mapping between a property path and its column definition.
    ///
    /// The structure of the returned map depend on the type of the specified property.
    ///
    /// - If the property is component-typed, the map contains entries for each component, with each key representing a full path to the component.
    ///   For example, the set of keys for property `note : RichText` is `{note.coreText, note.formattedText}`.
    /// - If the property is union-typed, the map contains entries for each union member, with each key representing a full path to the member.
    ///   For example, the set of keys for union-typed property `location : Location`, where union members are `workshop, station`, is `{location.workshop, location.station}`.
    /// - Otherwise, the map contains a single entry, with the key equal to the specified property's name.
    ///
    public Map<String, ColumnDefinition> extractFromProperty(
            final Class<? extends AbstractEntity<?>> enclosingEntityType,
            final String propName,
            final Class<?> propType, 
            final IsProperty isProperty, 
            final MapTo mapTo, 
            final PersistentType persistedType, 
            final boolean required, 
            final boolean unique, 
            final Optional<Integer> compositeKeyMemberOrder)
    {
        final String columnName = nameClause(propName, mapTo.value());
        final Object hibType = hibernateTypeDeterminer.getHibernateType(propType, persistedType);
        final int length = isProperty.length();
        final int precision = isProperty.precision();
        final int scale = isProperty.scale();

        if (isUnionEntityType(propType)) {
            final var propUnionEntityType = (Class<? extends AbstractUnionEntity>) propType;
            return unionProperties(propUnionEntityType).stream()
                    .collect(toImmutableMap(
                            sField -> propName + '.' + sField.getName(),
                            sField -> {
                                final MapTo sMapTo = getAnnotation(sField, MapTo.class);
                                if (sMapTo == null) {
                                    throw new DbSchemaException(ERR_MISSING_MAP_TO_IN_UNION_ENTITY.formatted(sField.getName(), propType));
                                }

                                final IsProperty sIsProperty = getAnnotation(sField, IsProperty.class);
                                if (sIsProperty == null) {
                                    throw new DbSchemaException(ERR_MISSING_IS_PROP_IN_UNION_ENTITY.formatted(sField.getName(), propType));
                                }

                                final String sColumnName = columnName + "_" + (isEmpty(sMapTo.value()) ? sField.getName().toUpperCase() : sMapTo.value());
                                return new ColumnDefinition(unique, compositeKeyMemberOrder, true, sColumnName,
                                                            sField.getType(), jdbcSqlTypeFor((Type) hibType),
                                                            sIsProperty.length(), sIsProperty.scale(), sIsProperty.precision(),
                                                            sMapTo.defaultValue(),
                                                            maybeIndexFor(propUnionEntityType, sField.getType(), sField.getName()),
                                                            dialect);
                            }));
        } else {
            if (hibType instanceof Type t) {
                return ImmutableMap.of(propName,
                                       new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required),
                                                            columnName, propType,
                                                            jdbcSqlTypeFor(t),
                                                            length, scale, precision, mapTo.defaultValue(),
                                                            maybeIndexFor(enclosingEntityType, propType, propName),
                                                            dialect));
            } else if (hibType instanceof UserType t) {
                return ImmutableMap.of(propName,
                                       new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required),
                                                            columnName, propType,
                                                            jdbcSqlTypeFor(t),
                                                            length, scale, precision, mapTo.defaultValue(),
                                                            maybeIndexFor(enclosingEntityType, propType, propName),
                                                            dialect));
            } else if (hibType instanceof CompositeUserType compositeUserType) {
                final List<Pair<String, Integer>> subProps = jdbcSqlTypeFor(compositeUserType);
                return subProps.stream().collect(
                        toImmutableMap(pair -> {
                                           final var subPropName = pair.getKey();
                                           return propName + '.' + subPropName;
                                       },
                                       pair -> {
                                           final String parentColumn = columnName;
                                           final var sName = pair.getKey();
                                           final var sType = compositeUserType.returnedClass();
                                           final Field sField = findFieldByName(sType, sName);
                                           final MapTo sMapTo = getAnnotation(sField, MapTo.class);
                                           final IsProperty sIsProperty = getAnnotation(sField, IsProperty.class);
                                           final String sColumnNameSuggestion = sMapTo.value();
                                           final Object sHibType = hibernateTypeDeterminer.getHibernateType(sField.getType(),
                                                                                                            getAnnotation(sField, PersistentType.class));
                                           final var sSqlType = switch (sHibType) {
                                               case UserType t -> jdbcSqlTypeFor(t);
                                               case Type t -> jdbcSqlTypeFor(t);
                                               default -> throw new DbSchemaException(ERR_UNSUPPORTED_HIB_TYPE.formatted("%s.%s".formatted(propType.getTypeName(), sName), sHibType));
                                           };

                                           final int sLength;
                                           if (RichText.class.isAssignableFrom(propType) && RichText.SEARCH_TEXT.equals(sField.getName())) {
                                               sLength = isProperty.length();
                                           } else {
                                               sLength = sIsProperty.length();
                                           }

                                           // properties of type Money need special handling as the precision and scale for Money.amount can be overridden
                                           final int sPrecision;
                                           if (Money.class.isAssignableFrom(sType) && "amount".equals(sField.getName()) && precision != IsProperty.DEFAULT_PRECISION) {
                                               sPrecision = precision;
                                           } else {
                                               sPrecision = sIsProperty.precision();
                                           }

                                           final int sScale;
                                           if (Money.class.isAssignableFrom(sType) && "amount".equals(sField.getName()) && scale != IsProperty.DEFAULT_SCALE) {
                                               sScale = scale;
                                           } else {
                                               sScale = sIsProperty.scale();
                                           }


                                           final String sColumnName = subProps.size() == 1 ? parentColumn
                                                   : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(sColumnNameSuggestion) ? sName.toUpperCase() : sColumnNameSuggestion));

                                           final Optional<ColumnIndex> sMaybeIndex;
                                           if (RichText.class.isAssignableFrom(propType) && RichText.SEARCH_TEXT.equals(sField.getName())) {
                                               sMaybeIndex = Optional.of(new ColumnIndex(ASC));
                                           } else {
                                               sMaybeIndex = maybeIndexFor(sType, sField.getType(), sName);
                                           }

                                           return new ColumnDefinition(unique, compositeKeyMemberOrder, isNullable(propType, required),
                                                                       sColumnName, sField.getType(), sSqlType,
                                                                       sLength, sScale, sPrecision,
                                                                       sMapTo.defaultValue(), sMaybeIndex, dialect);
                                       })
                );
            } else {
                throw new DbSchemaException(ERR_UNEXPECTED_HIB_TYPE.formatted(hibType, propType.getTypeName(), propName));
            }
        }
    }

    private Optional<ColumnIndex> maybeIndexFor(
            final Class<?> enclosingType,
            final Class<?> propType,
            final String propName)
    {
        if (isAuditEntityType(enclosingType) && propName.equals(AbstractAuditEntity.AUDIT_DATE) && propType == Date.class) {
            return Optional.of(new ColumnIndex(DESC));
        }
        if (isPersistentEntityType(propType)) {
            return Optional.of(new ColumnIndex(ASC));
        }
        else {
            return empty();
        }
    }

    private boolean isNullable(final Class<?> propType, final boolean required) {
        return !required && !boolean.class.isAssignableFrom(propType) && !Boolean.class.isAssignableFrom(propType);
    }
    
    public ColumnDefinition extractVersionProperty(final Class<? extends AbstractEntity<?>> enclosingEntityType) {
        final Field versionField = Finder.getFieldByName(AbstractEntity.class, VERSION);
        final var columns = extractFromProperty(enclosingEntityType, versionField.getName(), versionField.getType(), DEFAULT_IsProperty_ANNOTATION, getAnnotation(versionField, MapTo.class), null, true, false, empty());
        return first(columns.values()).orElseThrow();
    }

    public ColumnDefinition extractIdProperty(final Class<? extends AbstractEntity<?>> enclosingEntityType) {
        final Field idField = Finder.getFieldByName(AbstractEntity.class, ID);
        final var columns =  extractFromProperty(enclosingEntityType, idField.getName(), idField.getType(), DEFAULT_IsProperty_ANNOTATION, getAnnotation(idField, MapTo.class), null, true, false, empty());
        return first(columns.values()).orElseThrow();
    }

    public Optional<ColumnDefinition> extractSimpleKeyProperty(final Class<? extends AbstractEntity<?>> enclosingEntityType) {
        if (isCompositeEntity(enclosingEntityType) || isOneToOne(enclosingEntityType)) {
            return empty();
        }
        final Field keyField = Finder.getFieldByName(AbstractEntity.class, KEY);
        final var columns = extractFromProperty(enclosingEntityType, keyField.getName(), getKeyType(enclosingEntityType), getAnnotation(keyField, IsProperty.class), getAnnotation(keyField, MapTo.class), null, true, true, empty());
        return of(first(columns.values()).orElseThrow());
    }
    
    private String nameClause(final String propName, final String columnNameSuggestion) {
        return (isNotBlank(columnNameSuggestion) ? columnNameSuggestion : propName.toUpperCase() + "_");
    }

}
