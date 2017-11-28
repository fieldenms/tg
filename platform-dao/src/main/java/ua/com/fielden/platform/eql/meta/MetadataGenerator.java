package ua.com.fielden.platform.eql.meta;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PURE;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.DomainMetadataExpressionsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.elements.Expression1;
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
    protected final EntQueryGenerator qb;// = new EntQueryGenerator1(null); //DOMAIN_METADATA_ANALYSER
    //    
    private final DomainMetadataExpressionsGenerator dmeg = new DomainMetadataExpressionsGenerator();

    public MetadataGenerator(final EntQueryGenerator qb) {
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
        return (Expression1) new StandAloneExpressionBuilder(qb, exprModel).getResult().getValue();
    }

    private PrimTypePropInfo generateIdPropertyMetadata(final Class<? extends AbstractEntity<?>> entityType, final EntityInfo entityInfo) {
        switch (entityInfo.getCategory()) {
        case PERSISTED:
            return isOneToOne(entityType) ? new PrimTypePropInfo(AbstractEntity.ID, entityInfo, Long.class, null)
                    : new PrimTypePropInfo(AbstractEntity.ID, entityInfo, Long.class, null)/*(entityType)*/;
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
            throw new EqlException("Expression parsing for calculated properties yet to be modified according to the EQL3 approach.");
            //return entQryExpression(dmeg.extractExpressionModelFromCalculatedProperty(entityType, field));
        }
        return null;
    }

    private void addProps(final EntityInfo<?> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo> allEntitiesInfo) throws Exception {
        final PrimTypePropInfo idProp = generateIdPropertyMetadata(entityInfo.javaType(), entityInfo);
        if (idProp != null) {
            entityInfo.getProps().put(idProp.getName(), idProp);
        }

        for (final Field field : getRealProperties(entityInfo.javaType())) {
            final Class<?> javaType = determinePropertyType(entityInfo.javaType(), field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;

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

    /**
     * Creates an EQL model for an entity that yields all yield-able properties.
     * <ul>
     * <li>persistent properties,
     * <li>explicitly calculated properties (with <code>@Calculated</code>),
     * <li>implicitly calculated properties (their formulae are created and added during EQL metadata creation and available only at EQL processing level; e.g. composite key as
     * string).
     * </ul>
     * 
     * @param entityType
     * @return
     */
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> createYieldAllQueryModel(final Class<T> entityType) {
        final IFromAlias<T> qryStart = select(entityType);
        return streamRealProperties(entityType)
                .reduce(empty(), yieldAnother(entityType, qryStart), (o1, o2) -> o2)
                .get().modelAsEntity(entityType);
    }

    private <T extends AbstractEntity<?>> BiFunction<Optional<ISubsequentCompletedAndYielded<T>>, Field, Optional<ISubsequentCompletedAndYielded<T>>> yieldAnother(
            final Class<T> entityType,
            final IFromAlias<T> qryStart) {

        return (yield, field) -> {
            if (field.isAnnotationPresent(MapTo.class)) {
                return of(yield.map(y -> y.yield().prop(field.getName()).as(field.getName())).orElseGet(() -> qryStart.yield().prop(field.getName()).as(field.getName())));
            } else if (field.isAnnotationPresent(Calculated.class)) {
                return of(yield.map(y -> y.yield().expr(extractExpressionModelFromCalculatedProperty(entityType, field)).as(field.getName())).orElseGet(() -> qryStart.yield().expr(extractExpressionModelFromCalculatedProperty(entityType, field)).as(field.getName())));
            } else {
                return yield;
            }
        };
    }
}
