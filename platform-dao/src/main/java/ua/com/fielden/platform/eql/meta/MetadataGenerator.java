package ua.com.fielden.platform.eql.meta;

import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.PURE;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory.UNION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.eql.DomainMetadataExpressionsGenerator;
import ua.com.fielden.platform.dao.eql.EntityMetadata.EntityCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s1.processing.EntQueryGenerator1;
import ua.com.fielden.platform.eql.s1.processing.StandAloneExpressionBuilder1;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.utils.EntityUtils;

public class MetadataGenerator {
//    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();
//    public static final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata = new HashMap<>();
//
//    static {
//        hibTypeDefaults.put(Date.class, DateTimeType.class);
//        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
//    }
//
//    protected static final DomainMetadata DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), PlatformTestDomainTypes.entityTypes, AnnotationReflector.getAnnotation(User.class, MapEntityTo.class), DbVersion.H2);
//
//    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);
//
    protected final EntQueryGenerator1 qb;// = new EntQueryGenerator1(null); //DOMAIN_METADATA_ANALYSER
//    
    private final DomainMetadataExpressionsGenerator dmeg = new DomainMetadataExpressionsGenerator();

    
    public MetadataGenerator(final EntQueryGenerator1 qb) {
        this.qb = qb;
    }

    public final Map<Class<? extends AbstractEntity<?>>, EntityInfo> generate(final Set<Class<? extends AbstractEntity<?>>> entities) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, EntityInfo> result = new HashMap<>();

        for (final Class<? extends AbstractEntity<?>> entityType : entities) {
            result.put(entityType, new EntityInfo(entityType, determineCategory(entityType)));
        }

        for (final EntityInfo entityInfo : result.values()) {
            addProps(entityInfo, result);
        }

        return result;
    }

    private <ET extends AbstractEntity<?>> EntityCategory determineCategory(final Class<ET> entityType) {

        final String tableClause = getTableClause(entityType);
        if (tableClause != null) {
            return PERSISTED;
        }

        final List<EntityResultQueryModel<ET>> entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
        if (entityModels.size() > 0) {
            return QUERY_BASED;
        }

        if (isUnionEntityType(entityType)) {
            return UNION;
        }

        return PURE;
    }

    private String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
        if (!EntityUtils.isPersistedEntityType(entityType)) {
            return null;
        }

        final MapEntityTo mapEntityToAnnotation = AnnotationReflector.getAnnotation(entityType, MapEntityTo.class);

        final String providedTableName = mapEntityToAnnotation.value();
        if (!StringUtils.isEmpty(providedTableName)) {
            return providedTableName;
        } else {
            return DynamicEntityClassLoader.getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
        }
    }

    private boolean isOneToOne(final Class<? extends AbstractEntity<?>> entityType) {
        return isPersistedEntityType(getKeyType(entityType));
    }

    private Expression1 entQryExpression(final ExpressionModel exprModel) {
        return (Expression1) new StandAloneExpressionBuilder1(qb, exprModel).getResult().getValue();
    }

    
    private PrimTypePropInfo generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityInfo entityInfo) {
        switch (entityInfo.getCategory()) {
        case PERSISTED:
            return isOneToOne(entityType) ? new PrimTypePropInfo(AbstractEntity.ID, entityInfo, Long.class, null) : new PrimTypePropInfo(AbstractEntity.ID, entityInfo, Long.class, null)/*(entityType)*/;
        case QUERY_BASED:
            if (isEntityType(getKeyType(entityType))) {
                return new PrimTypePropInfo(AbstractEntity.ID, entityInfo, Long.class, entQryExpression(expr().prop("key").model()));
            } else {
                return null;
            }
        case UNION:
            return null;//TODO uncomment; new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(typeResolver.basic("long")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "id")).type(EXPRESSION).build();
        default:
            return null;
        }
    }
    
    private Expression1 getExpression(final Class<? extends AbstractEntity<?>> entityType, final Field field) throws Exception {
        if (AnnotationReflector.isAnnotationPresent(field, Calculated.class)) {
            return entQryExpression(dmeg.extractExpressionModelFromCalculatedProperty(entityType, field));
        }
        return null;
    }
    
    private void addProps(final EntityInfo entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo> allEntitiesInfo) throws Exception {
        final PrimTypePropInfo idProp = generateIdPropertyMetadata(entityInfo.javaType(), entityInfo);
        if (idProp != null) {
            entityInfo.getProps().put(idProp.getName(), idProp);
        }
        
        for (final Field field : getRealProperties(entityInfo.javaType())) {
            final Class javaType = determinePropertyType(entityInfo.javaType(), field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;

            if (AbstractEntity.class.isAssignableFrom(javaType)) {
                entityInfo.getProps().put(field.getName(), new EntityTypePropInfo(field.getName(), entityInfo, allEntitiesInfo.get(javaType/*field.getType()*/), getExpression(entityInfo.javaType(), field)));
            } else {
                entityInfo.getProps().put(field.getName(), new PrimTypePropInfo(field.getName(), entityInfo, javaType/*field.getType()*/, getExpression(entityInfo.javaType(), field)));
            }
            //	    if (!result.containsKey(field.getName())) {
            //		if (Collection.class.isAssignableFrom(field.getType()) && Finder.hasLinkProperty(entityType, field.getName())) {
            //		    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
            //		} else if (field.isAnnotationPresent(Calculated.class)) {
            //		    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
            //		} else if (field.isAnnotationPresent(MapTo.class)) {
            //		    safeMapAdd(result, getCommonPropHibInfo(entityType, field));
            //		} else if (Finder.isOne2One_association(entityType, field.getName())) {
            //		    safeMapAdd(result, getOneToOnePropInfo(entityType, field));
            //		} else if (!field.isAnnotationPresent(CritOnly.class)) {
            //		    safeMapAdd(result, getSyntheticPropInfo(entityType, field));
            //		} else {
            //		    //System.out.println(" --------------------------------------------------------- " + entityType.getSimpleName() + ": " + field.getName());
            //		}
            //	    }
        }
    }
}
