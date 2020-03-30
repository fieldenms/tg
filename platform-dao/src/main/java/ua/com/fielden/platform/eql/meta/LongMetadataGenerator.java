package ua.com.fielden.platform.eql.meta;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COLLECTIONAL;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.EXPRESSION;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.SYNTHETIC_COMPONENT_HEADER;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.VIRTUAL_OVERRIDE;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.metadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.metadata.PropertyColumn;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.model.PropColumn;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class LongMetadataGenerator {
    
    private static final TypeResolver typeResolver = new TypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");
    private static final Type H_STRING = typeResolver.basic("string");
    private static final Type H_BOOLEAN = typeResolver.basic("yes_no");
    private static final Type H_BIG_DECIMAL = typeResolver.basic("big_decimal");
    private static final Type H_BIG_INTEGER = typeResolver.basic("big_integer");

    public static final List<String> specialProps = unmodifiableListOf(ID, KEY, VERSION);

    private final PropColumn id;
    private final PropColumn version;
    private final PropColumn key = new PropColumn("KEY_");

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;


    private final List<Class<? extends AbstractEntity<?>>> entityTypes;
    
    private Injector hibTypesInjector;

    public LongMetadataGenerator(//
            final Map<Class, Class> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;

        this.hibTypesDefaults = new ConcurrentHashMap<>(entityTypes.size());
        
        this.entityTypes = new ArrayList<>(entityTypes);
        
        // initialise meta-data for basic entity properties, which is RDBMS dependent
        if (dbVersion != DbVersion.ORACLE) {
            id = new PropColumn("_ID");
            version = new PropColumn("_VERSION");
        } else {
            id = new PropColumn("TG_ID");
            version = new PropColumn("TG_VERSION");
        }

        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class, Class> entry : hibTypesDefaults.entrySet()) {
                try {
                    this.hibTypesDefaults.put(entry.getKey(), entry.getValue().newInstance());
                } catch (final Exception e) {
                    throw new IllegalStateException("Couldn't generate instantiate hibernate type [" + entry.getValue() + "] due to: " + e);
                }
            }
        }
        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);

        this.hibTypesInjector = hibTypesInjector;
    }
    
    /**
     * Determines hibernate type instance for entity property based on provided property's meta information.
     *
     * @param entityType
     * @param field
     * @return
     * @throws Exception
     * @throws
     */
    private Object getHibernateType(final Class<?> propType, final String propName, final Class<? extends AbstractEntity<?>> parentType) {
        if (isPersistedEntityType(propType)) {
            return H_LONG;
        }

        final PersistentType persistentType = getPropertyAnnotation(PersistentType.class, parentType, propName);
        final String hibernateTypeName = persistentType != null ? persistentType.value() : null;
        final Class<?> hibernateUserTypeImplementor = persistentType != null ? persistentType.userType() : Void.class;

        if (isNotEmpty(hibernateTypeName)) {
            return typeResolver.basic(hibernateTypeName);
        }

        if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
            return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
        } else {
            final Object defaultHibType = hibTypesDefaults.get(propType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                return typeResolver.basic(propType.getName());
            }
        }
    }
    
    private List<PropertyColumn> getPropColumns(final Field field, final IsProperty isProperty, final MapTo mapTo, final Object hibernateType) throws Exception {
        final String columnName = isNotEmpty(mapTo.value()) ? mapTo.value() : field.getName().toUpperCase() + "_";
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;

        final List<PropertyColumn> result = new ArrayList<>();
        if (hibernateType instanceof ICompositeUserTypeInstantiate) {
            final ICompositeUserTypeInstantiate hibCompositeUSerType = (ICompositeUserTypeInstantiate) hibernateType;
            for (final PropertyColumn column : getCompositeUserTypeColumns(hibCompositeUSerType, columnName)) {
                result.add(column);
            }
        } else {
            result.add(new PropertyColumn(columnName, length, precision, scale));
        }
        return result;
    }
    
    /**
     * Generates list of column names for mapping of CompositeUserType implementors.
     *
     * @param hibType
     * @param parentColumn
     * @return
     * @throws Exception
     */
    private List<PropertyColumn> getCompositeUserTypeColumns(final ICompositeUserTypeInstantiate hibType, final String parentColumn) throws Exception {
        final String[] propNames = hibType.getPropertyNames();
        final List<PropertyColumn> result = new ArrayList<>();
        for (final String propName : propNames) {
            final MapTo mapTo = getPropertyAnnotation(MapTo.class, hibType.returnedClass(), propName);
            final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, hibType.returnedClass(), propName);
            final String mapToColumn = mapTo.value();
            final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
            final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
            final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
            final String columnName = propNames.length == 1 ? parentColumn
                    : (parentColumn + (parentColumn.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
            result.add(new PropertyColumn(columnName, length, precision, scale));
        }
        return result;
    }
    
    private LongPropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType) throws Exception {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();

        final boolean nullable = !PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);

        final Object hibernateType = getHibernateType(javaType, propName, entityType);

        final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propName);
        final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, entityType, propName);
        
        return new LongPropertyMetadata.
                Builder(propName, javaType, nullable).
                hibType(hibernateType).
                //columns(getPropColumns(propField, isProperty, mapTo, hibernateType)).
                build();
    }
    
    private PropertyMetadata getVirtualPropInfoForDynamicEntityKey(final EntityTypeInfo <? extends AbstractEntity<DynamicEntityKey>> parentInfo) throws Exception {
        return new PropertyMetadata.Builder(KEY, String.class, true, parentInfo).expression(generateCompositeKeyEqlExpression(parentInfo.entityType)).hibType(H_STRING).category(VIRTUAL_OVERRIDE).build();
    }

    private PropertyMetadata getCalculatedPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final String propName = propField.getName();
        final boolean aggregatedExpression = (AGGREGATED_EXPRESSION == getAnnotation(propField, Calculated.class).category());
        
        final Class<?> javaType = propField.getType();
        final Object hibernateType = getHibernateType(javaType, propName, parentInfo.entityType);

        final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(parentInfo.entityType, propField);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? COMPONENT_HEADER : EXPRESSION;
        return new PropertyMetadata.
                Builder(propName, propField.getType(), true, parentInfo).
                expression(expressionModel).
                hibType(hibernateType).
                category(propCat).
                aggregatedExpression(aggregatedExpression).
                build();
    }

    private PropertyMetadata getOneToOnePropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object hibernateType = getHibernateType(javaType, propName, parentInfo.entityType);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new PropertyMetadata.
                Builder(propName, javaType, true, parentInfo).
                expression(expressionModel).
                hibType(hibernateType).
                category(EXPRESSION).
                build();
    }

    private PropertyMetadata getSyntheticPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object hibernateType = getHibernateType(javaType, propName, parentInfo.entityType);
        final PropertyCategory propCat = hibernateType instanceof ICompositeUserTypeInstantiate ? SYNTHETIC_COMPONENT_HEADER : SYNTHETIC;
        return new PropertyMetadata.
                Builder(propField.getName(), propField.getType(), true, parentInfo).
                hibType(hibernateType).
                category(propCat).
                build();
    }
    

    private PropertyMetadata getCollectionalPropInfo(final Field propField, final EntityTypeInfo <? extends AbstractEntity<?>> parentInfo) throws Exception {
        return new PropertyMetadata.Builder(propField.getName(), determinePropertyType(parentInfo.entityType, propField.getName()), true, parentInfo).category(COLLECTIONAL).build();
    }

}
