package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.dao.DomainMetadataExpressionsGenerator.getVirtualKeyPropForEntityWithCompositeKey;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PURE;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import ua.com.fielden.platform.dao.DomainMetadataExpressionsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;
import ua.com.fielden.platform.reflection.AnnotationReflector;
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

    public final Map<String, EntityInfo> generate(final Set<Class<? extends AbstractEntity<?>>> entities) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, EntityInfo> result = entities.stream()
                .filter(t -> isPersistedEntityType(t) || isSyntheticEntityType(t) || isSyntheticBasedOnPersistentEntityType(t) || isUnionEntityType(t))
                .collect(toMap(k -> k, k -> new EntityInfo(k.getName(), determineCategory(k))));
        
        for (Entry<Class<? extends AbstractEntity<?>>, EntityInfo> re : result.entrySet()) {
            addProps(re.getKey(), re.getValue(), result);
            
        }
        
        
        //result.values().stream().forEach(ei -> addProps(ei, result));
        return result.entrySet().stream().collect(toMap(k -> k.getKey().getName(), k -> k.getValue()));
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
        if (!isPersistedEntityType(entityType)) {
            return null;
        }

        final MapEntityTo mapEntityToAnnotation = AnnotationReflector.getAnnotation(entityType, MapEntityTo.class);

        final String providedTableName = mapEntityToAnnotation.value();
        if (!isEmpty(providedTableName)) {
            return providedTableName;
        } else {
            return getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
        }
    }

    private boolean isOneToOne(final Class<? extends AbstractEntity<?>> entityType) {
        return isPersistedEntityType(getKeyType(entityType));
    }

    private <T extends AbstractEntity<?>> Optional<PrimTypePropInfo> generateIdPropertyMetadata(final Class<T> entityType, final EntityInfo entityInfo) {
        switch (entityInfo.getCategory()) {
        case PERSISTED:
            //TODO Need to handle expression for one-2-one case.
            return of(isOneToOne(entityType) ? new PrimTypePropInfo(ID, Long.class, entityInfo)
                    : new PrimTypePropInfo(ID, Long.class, entityInfo)); 
        case QUERY_BASED:
            if (EntityUtils.isSyntheticBasedOnPersistentEntityType(entityType)) {
                if (isEntityType(getKeyType(entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persystent type with an entity-typed key. This is not supported.", entityType.getName()));
                }
                return of(new PrimTypePropInfo(ID, Long.class, entityInfo));
            } else if (isEntityType(getKeyType(entityType))) {
                return of(new PrimTypePropInfo(ID, Long.class, entityInfo)); //TODO need to move this to createYieldAllQueryModel -- entQryExpression(expr().prop("key").model())));
            } else {
                return empty();
            }
        case UNION:
            throw new EqlException("Not yet"); //TODO uncomment; new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(typeResolver.basic("long")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "id")).type(EXPRESSION).build();
        default:
            throw new EqlException(format("Generation of ID property metadata is not supported for category [%s] on entity [%s].", entityInfo.getCategory(), entityType.getName()));
        }
    }

    private <T extends AbstractEntity<?>> void addProps(final Class<T> entityType, final EntityInfo entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo> allEntitiesInfo) {
        generateIdPropertyMetadata(entityType, entityInfo).ifPresent(id -> entityInfo.addProp(id));
        entityInfo.addProp(new PrimTypePropInfo(VERSION, Long.class, entityInfo));
        
        EntityUtils.getRealProperties(entityType).stream()
        .filter(f -> f.isAnnotationPresent(MapTo.class) || f.isAnnotationPresent(Calculated.class)).forEach(field -> {
            final Class<?> javaType = determinePropertyType(entityType, field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;

            if (AbstractEntity.class.isAssignableFrom(javaType)) {
                entityInfo.addProp(new EntityTypePropInfo(field.getName(), allEntitiesInfo.get(javaType), entityInfo));
            } else {
                entityInfo.addProp(new PrimTypePropInfo(field.getName(), javaType, entityInfo));
            }

        });
       
        
//        for (final Field field : getRealProperties(entityInfo.javaType())) {
//            final Class<?> javaType = determinePropertyType(entityInfo.javaType(), field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
//
//            if (AbstractEntity.class.isAssignableFrom(javaType)) {
//                entityInfo.addProp(new EntityTypePropInfo(field.getName(), allEntitiesInfo.get(javaType), entityInfo));
//            } else {
//                entityInfo.addProp(new PrimTypePropInfo(field.getName(), javaType, entityInfo));
//            }
//            //	    if (!result.containsKey(field.getName())) {
//            //		if (Collection.class.isAssignableFrom(field.getType()) && Finder.hasLinkProperty(entityType, field.getName())) {
//            //		    safeMapAdd(result, getCollectionalPropInfo(entityType, field));
//            //		} else if (field.isAnnotationPresent(Calculated.class)) {
//            //		    safeMapAdd(result, getCalculatedPropInfo(entityType, field));
//            //		} else if (field.isAnnotationPresent(MapTo.class)) {
//            //		    safeMapAdd(result, getCommonPropHibInfo(entityType, field));
//            //		} else if (Finder.isOne2One_association(entityType, field.getName())) {
//            //		    safeMapAdd(result, getOneToOnePropInfo(entityType, field));
//            //		} else if (!field.isAnnotationPresent(CritOnly.class)) {
//            //		    safeMapAdd(result, getSyntheticPropInfo(entityType, field));
//            //		} else {
//            //		    //System.out.println(" --------------------------------------------------------- " + entityType.getSimpleName() + ": " + field.getName());
//            //		}
//            //	    }
//        }
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
    public static <T extends AbstractEntity<?>> EntityResultQueryModel<T> createYieldAllQueryModel(final Class<T> entityType) {
        final ISubsequentCompletedAndYielded<T> yield = select(entityType).yield().prop(ID).as(ID).yield().prop(VERSION).as(VERSION);
        return EntityUtils.getRealProperties(entityType).stream().reduce(of(yield), yieldAnother(entityType), (o1, o2) -> {
            throw new EqlException("Parallel reduction is not supported.");
        }).get().modelAsEntity(entityType);
    }

    private static <T extends AbstractEntity<?>> BiFunction<Optional<ISubsequentCompletedAndYielded<T>>, Field, Optional<ISubsequentCompletedAndYielded<T>>> yieldAnother(final Class<T> entityType) {
        return (yield, field) -> {
            if (field.isAnnotationPresent(MapTo.class)) {
                return yield.map(y -> {
                    if (KEY.equals(field.getName()) && isCompositeEntity(entityType)) {
                        return y.yield().expr(getVirtualKeyPropForEntityWithCompositeKey((Class<? extends AbstractEntity<DynamicEntityKey>>) entityType)).as(KEY);
                    } else {
                        return y.yield().prop(field.getName()).as(field.getName());
                    }
                });
            } else if (field.isAnnotationPresent(Calculated.class)) {
                return yield.map(y -> y.yield().expr(extractExpressionModelFromCalculatedProperty(entityType, field)).as(field.getName()));
            } else {
                return yield;
            }
        };
    }
}