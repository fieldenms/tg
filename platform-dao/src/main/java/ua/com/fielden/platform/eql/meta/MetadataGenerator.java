package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PURE;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;
import ua.com.fielden.platform.eql.stage3.elements.Column;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

public class MetadataGenerator {
    protected final DomainMetadata dm;

    public MetadataGenerator(final DomainMetadata dm) {
        this.dm = dm;
    }
    
    protected final EntQueryGenerator qb() {
        return new EntQueryGenerator(new DomainMetadataAnalyser(dm), null, null, null, emptyMap());
    }


    public final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> generate(final Set<Class<? extends AbstractEntity<?>>> entities) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> result = entities.stream()
                .filter(t -> isPersistedEntityType(t) || isSyntheticEntityType(t) || isSyntheticBasedOnPersistentEntityType(t) || isUnionEntityType(t))
                .collect(toMap(k -> k, k -> new EntityInfo<>(k, determineCategory(k))));
        result.values().stream().forEach(ei -> addProps(ei, result));
        return result;
    }

    public final Map<String, Table> generateTables(final Collection<Class<? extends AbstractEntity<?>>> entities) throws Exception {
        final Map<String, Table> result = entities.stream()
                .filter(t -> isPersistedEntityType(t))
                .collect(toMap(k -> k.getName(), k -> new Table(getTableClause(k), getColumns(k))));
        //result.values().stream().forEach(ei -> addProps(ei, result));
        return result;
    }
    
    private <ET extends AbstractEntity<?>> EntityCategory determineCategory(final Class<ET> entityType) {

        final String tableClause = getTableClause(entityType);
        if (tableClause != null) {
            return PERSISTED;
        }

        final List<EntityResultQueryModel<ET>> entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
        if (!entityModels.isEmpty()) {
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

    private <T extends AbstractEntity<?>> Optional<PrimTypePropInfo<?>> generateIdPropertyMetadata(final Class<T> entityType, final EntityCategory category) {
        switch (category) {
        case PERSISTED:
            //TODO Need to handle expression for one-2-one case.
            return of(isOneToOne(entityType) ? new PrimTypePropInfo<Long>(ID, LongType.INSTANCE, Long.class)
                    : new PrimTypePropInfo<Long>(ID, LongType.INSTANCE, Long.class)); 
        case QUERY_BASED:
            if (EntityUtils.isSyntheticBasedOnPersistentEntityType(entityType)) {
                if (isEntityType(getKeyType(entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persystent type with an entity-typed key. This is not supported.", entityType.getName()));
                }
                return of(new PrimTypePropInfo<Long>(ID, LongType.INSTANCE, Long.class));
            } else if (isEntityType(getKeyType(entityType))) {
                return of(new PrimTypePropInfo<Long>(ID, LongType.INSTANCE, Long.class)); //TODO need to move this to createYieldAllQueryModel -- entQryExpression(expr().prop("key").model())));
            } else {
                return empty();
            }
        case UNION:
            throw new EqlException("Not yet"); //TODO uncomment; new PropertyMetadata.Builder(AbstractEntity.ID, Long.class, false).hibType(typeResolver.basic("long")).expression(dmeg.generateUnionEntityPropertyExpression((Class<? extends AbstractUnionEntity>) entityType, "id")).type(EXPRESSION).build();
        default:
            throw new EqlException(format("Generation of ID property metadata is not supported for category [%s] on entity [%s].", category, entityType.getName()));
        }
    }

    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo) {
        //generateIdPropertyMetadata(entityInfo.javaType(), entityInfo.getCategory()).ifPresent(id -> entityInfo.addProp(id));
        entityInfo.addProp(new EntityTypePropInfo<T>(ID, (EntityInfo<T>) allEntitiesInfo.get(entityInfo.javaType()), LongType.INSTANCE, true));
        entityInfo.addProp(new PrimTypePropInfo<Long>(VERSION, LongType.INSTANCE, Long.class));
        
        EntityUtils.getRealProperties(entityInfo.javaType()).stream()
        .filter(f -> f.isAnnotationPresent(MapTo.class) || f.isAnnotationPresent(Calculated.class)).forEach(field -> {
            final Class<?> javaType = determinePropertyType(entityInfo.javaType(), field.getName()); // redetermines prop type in platform understanding (e.g. type of Set<MeterReading> readings property will be MeterReading;
            Expression1 expr = null;
            if (field.isAnnotationPresent(Calculated.class)) {
                try {
                    final ExpressionModel expressionModel = extractExpressionModelFromCalculatedProperty(entityInfo.javaType(), field);
                    expr = (Expression1) (new StandAloneExpressionBuilder(qb(), expressionModel)).getResult().getValue();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            
            if (AbstractUnionEntity.class.isAssignableFrom(javaType)) {
                //entityInfo.addProp(new UnionTypePropInfo(field.getName(), allEntitiesInfo.get(javaType), LongType.INSTANCE, expr));
            } else if (AbstractEntity.class.isAssignableFrom(javaType)) {
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(field, javaType);
                entityInfo.addProp(new EntityTypePropInfo(field.getName(), allEntitiesInfo.get(javaType), LongType.INSTANCE, required, expr));
            } else {
                entityInfo.addProp(new PrimTypePropInfo(field.getName(),EntityUtils.isBoolean(javaType) ? YesNoType.INSTANCE : StringType.INSTANCE, javaType, expr));
            }

        });
        
        if (EntityUtils.isCompositeEntity(entityInfo.javaType())) {
            //System.out.println(entityInfo.javaType().getSimpleName());
            final ExpressionModel expressionModel = CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression((Class<? extends AbstractEntity<DynamicEntityKey>>) entityInfo.javaType());
            final Expression1 expr = (Expression1) (new StandAloneExpressionBuilder(qb(), expressionModel)).getResult().getValue();
            entityInfo.addProp(new PrimTypePropInfo<String>(KEY, StringType.INSTANCE, String.class, expr));
        }        
    }

    private <T extends AbstractEntity<?>> Map<String, Column> getColumns(final Class<? extends AbstractEntity<?>> entityType) {
        final Map<String, Column> result = new HashMap<>();
        result.put("id", new Column("_ID"));
        result.put("version", new Column("_VERSION"));
        
        EntityUtils.getRealProperties(entityType).stream()
        .filter(f -> f.isAnnotationPresent(MapTo.class)).forEach(field -> {
            result.put(field.getName(), new Column(field.getName().toUpperCase() + "_"));
        });
        
        return result;
    }
}