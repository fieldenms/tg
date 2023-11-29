package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.commonProperties;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity.query.metadata.CompositeKeyEqlExpressionGenerator.generateCompositeKeyEqlExpression;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.extractExpressionModelFromCalculatedProperty;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.generateUnionEntityPropertyContextualExpression;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.Finder.hasLinkProperty;
import static ua.com.fielden.platform.reflection.Finder.isOne2One_association;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isRequiredByDefinition;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.CollectionUtil.unmodifiableListOf;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.hibernate.type.BasicType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.CompositeUserType;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion.PropColumnInfo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata.Builder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNode;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNodesGenerator;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformer;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.utils.EntityUtils;

public class EqlDomainMetadata {

    private static final TypeConfiguration typeConfiguration = new TypeConfiguration();
    public static final TypeResolver typeResolver = new TypeResolver(typeConfiguration, new TypeFactory(typeConfiguration));
    private static final Type H_LONG = typeResolver.basic("long");
    private static final Type H_STRING = typeResolver.basic("string");
    private static final Type H_BOOLEAN = typeResolver.basic("yes_no");
    public static final String Y = "Y";
    public static final String N = "N";

    public static final List<String> specialProps = unmodifiableListOf(ID, KEY, VERSION);

    private final PropColumn id;
    private final PropColumn version;
    private static final PropColumn key = new PropColumn("KEY_");

    public final DbVersion dbVersion;
    /**
     * Map between java type and hibernate persistence type (implementers of Type, IUserTypeInstantiate, ICompositeUserTypeInstantiate).
     */
    private final ConcurrentMap<Class<?>, Object> hibTypesDefaults;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata;
    private final ConcurrentMap<String, Table> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final EntQueryGenerator gen;

    private final Injector hibTypesInjector;

    public EqlDomainMetadata(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;

        this.hibTypesDefaults = new ConcurrentHashMap<>(entityTypes.size());
        this.entityPropsMetadata = new ConcurrentHashMap<>(entityTypes.size());

        // initialise meta-data for basic entity properties, which is RDBMS dependent
        id = new PropColumn(dbVersion.idColumnName());
        version = new PropColumn(dbVersion.versionColumnName());

        // carry on with other stuff
        if (hibTypesDefaults != null) {
            for (final Entry<Class<?>, Class<?>> entry : hibTypesDefaults.entrySet()) {
                try {
                    this.hibTypesDefaults.put(entry.getKey(), entry.getValue().getDeclaredField("INSTANCE").get(null));//.newInstance());
                } catch (final Exception e) {
                    throw new EqlMetadataGenerationException("Couldn't generate instantiate hibernate type [" + entry.getValue() + "] due to: " + e);
                }
            }
        }
        this.hibTypesDefaults.put(Boolean.class, H_BOOLEAN);
        this.hibTypesDefaults.put(boolean.class, H_BOOLEAN);

        this.hibTypesInjector = hibTypesInjector;
        this.gen = new EntQueryGenerator(null, null, null, emptyMap());

        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = getEntityTypeInfo(entityType);
                if (parentInfo.category != PURE) {
                    final List<EqlPropertyMetadata> propsMetadata = generatePropertyMetadatasForEntity(parentInfo, entityType);
                    entityPropsMetadata.put(entityType, new EqlEntityMetadata(parentInfo, propsMetadata));
                    if (parentInfo.category == PERSISTENT) {
                        tables.put(entityType.getName(), generateTable(parentInfo.tableName, propsMetadata));
                        tableStructsForBatchInsertion.put(entityType.getName(), generateTableWithPropColumnInfo(parentInfo.tableName, propsMetadata));
                    }
                }
            } catch (final Exception ex) {
                throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "].", ex);
            }
        });

        domainInfo = entityPropsMetadata.entrySet().stream().collect(Collectors.toConcurrentMap(k -> k.getKey(), k -> new EntityInfo<>(k.getKey(), k.getValue().typeInfo.category)));
        domainInfo.values().stream().forEach(ei -> addProps(ei, domainInfo, entityPropsMetadata.get(ei.javaType()).props()));

        for (final EqlEntityMetadata el : entityPropsMetadata.values()) {
            if (el.typeInfo.category == QUERY_BASED) {
                try {
                    final EntityInfo<? extends AbstractEntity<?>> enhancedEntityInfo = generateEnhancedEntityInfoForSyntheticType(el.typeInfo, el.typeInfo.entityType);
                    domainInfo.put(enhancedEntityInfo.javaType(), enhancedEntityInfo);
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new EqlMetadataGenerationException("Couldn't generate enhanced entity info for synthetic entity [" + el.typeInfo.entityType + "] due to: " + e);
                }
            }
        }

        validateCalcProps();
    }

    /**
     * Only properties that are present in SE yields are preserved.
     *
     * @param parentInfo
     * @return
     */
    private <T extends AbstractEntity<?>> EntityInfo<?> generateEnhancedEntityInfoForSyntheticType(final EntityTypeInfo<T> parentInfo, final Class<? extends AbstractEntity<?>> actualType) {
        final TransformationContext1 context = new TransformationContext1(this);
        final Yields2 yields = gen.generateAsSyntheticEntityQuery(parentInfo.entityModels.get(0), parentInfo.entityType).transform(context).yields;
        final Map<String, YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(yields.getYields());
        return Source1BasedOnSubqueries.produceEntityInfoForDefinedEntityType(this, yieldInfoNodes, actualType/*parentInfo.entityType*/);
    }

    private void validateCalcProps() {
        final PathsToTreeTransformer p2tt = new PathsToTreeTransformer(this, gen);
        for (final EntityInfo<?> et : domainInfo.values()) {
            if (et.getCategory() != UNION) {
                final Source2BasedOnPersistentType source = new Source2BasedOnPersistentType(et.javaType(), et, gen.nextSourceId() /*"dummy_id"*/); //TODO analyze
                for (final AbstractPropInfo<?> prop : et.getProps().values()) {
                    if (prop.expression != null && !prop.name.equals(KEY)) {
                       try {
                            p2tt.transform(setOf(new Prop2(source, asList(prop))));
                        } catch (final Exception e) {
                            throw new EqlException("There is an error in expression of calculated property [" + et.javaType().getSimpleName() + ":" + prop.name + "]: " + e.getMessage(), e);
                        }
                    } else if (prop.hasExpression() && prop instanceof ComponentTypePropInfo) {
                        for (final AbstractPropInfo<?> subprop : ((ComponentTypePropInfo<?>) prop).getProps().values()) {
                            if (subprop.expression != null) {
                                try {
                                    p2tt.transform(setOf(new Prop2(source, asList(prop, subprop))));
                                } catch (final Exception e) {
                                    throw new EqlException("There is an error in expression of calculated property [" + et.javaType().getSimpleName() + ":" + prop.name  + "." + subprop.name + "]: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo, final Collection<EqlPropertyMetadata> entityPropsMetadatas) {
        for (final EqlPropertyMetadata el : entityPropsMetadatas) {
            if (!el.critOnly) {
                final String name = el.name;
                final Class<?> javaType = el.javaType;
                final Object hibType = el.hibType;
                final ExpressionModel expr = el.expressionModel;

                if (isUnionEntityType(javaType)) {
                    final EntityInfo<? extends AbstractUnionEntity> ef = new EntityInfo<>((Class<? extends AbstractUnionEntity>) javaType, UNION);
                    for (final EqlPropertyMetadata sub : el.subitems()) {
                        if (sub.expressionModel == null) {
                            ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, null, sub.implicit));
                        } else {
                            final ExpressionModel subExpr = sub.expressionModel;
                            if (EntityUtils.isEntityType(sub.javaType)) {
                                ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, subExpr, sub.implicit));
                            } else {
                                ef.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr, sub.implicit));
                            }

                        }
                    }
                    entityInfo.addProp(new UnionTypePropInfo(name, ef, hibType, false));
                } else if (isPersistedEntityType(javaType)) {
                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(javaType), hibType, el.required, expr, el.implicit));
                    //                } else if (ID.equals(name)){
                    //                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(entityInfo.javaType()), hibType, required, expr));
                } else {
                    if (el.subitems().isEmpty()) {
                        entityInfo.addProp(new PrimTypePropInfo(name, hibType, javaType, expr, el.implicit));
                    } else {
                        final ComponentTypePropInfo propTpi = new ComponentTypePropInfo(name, javaType, hibType);
                        for (final EqlPropertyMetadata sub : el.subitems()) {
                            final ExpressionModel subExpr = sub.expressionModel;
                            propTpi.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr, sub.implicit));
                        }
                        entityInfo.addProp(propTpi);
                    }
                }
            }
        }
    }

    /**
     * Determines hibernate type instance (Type/UserType/CustomUserType) for entity property based on provided property's meta information.
     *
     * @param propField
     * @return
     */
    private Object getHibernateType(final Field propField) {
        final String propName = propField.getName();
        final Class<?> propType = propField.getType();

        if (isPersistedEntityType(propType) || isUnionEntityType(propType) || isSyntheticEntityType(propType)) {
            return H_LONG;
        }

        final PersistentType persistentType = getAnnotation(propField, PersistentType.class);

        if (persistentType == null) {
            final Object defaultHibType = hibTypesDefaults.get(propType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                final BasicType result = typeResolver.basic(propType.getName());
                if (result == null) {
                    throw new EqlMetadataGenerationException(propName + " of type " + propType.getName() + " has no hibType (1)");
                }
                return result;
            }
        } else {
            final String hibernateTypeName = persistentType.value();
            final Class<?> hibernateUserTypeImplementor = persistentType.userType();
            if (isNotEmpty(hibernateTypeName)) {
                final BasicType result = typeResolver.basic(hibernateTypeName);
                if (result == null) {
                    throw new EqlMetadataGenerationException(propName + " of type " + propType.getName() + " has no hibType (2)");
                }
                return result;
            } else if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
                return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
            } else {
                throw new EqlMetadataGenerationException("Persistent annotation doesn't provide intended information.");
            }
        }
    }

    private Optional<EqlPropertyMetadata> generateIdPropertyMetadata(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo) {
    	final EqlPropertyMetadata idProperty = new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).required().column(id).build();
        final EqlPropertyMetadata idPropertyInOne2One = new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).required().column(id).build();
        switch (parentInfo.category) {
        case PERSISTENT:
            return isOneToOne(parentInfo.entityType) ? of(idPropertyInOne2One) : of(idProperty)/*(entityType)*/;
        case QUERY_BASED:
            if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                if (isEntityType(getKeyType(parentInfo.entityType))) {
                    throw new EntityDefinitionException(format("Entity [%s] is recognised as synthetic that is based on a persistent type with an entity-typed key. This is not supported.", parentInfo.entityType.getName()));
                }
                return of(idProperty);
            } else if (isEntityType(getKeyType(parentInfo.entityType))) {
                return of(new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).expression(expr().prop(KEY).model()).implicit().build());
            } else {
            	// FIXME reconsider this implementation taking into account its role combined with actual yields information in the process of getting final EntityPropInfo for Synthetic Entity
                return of(idProperty);
            }
        default:
            return empty();
        }
    }

    private EqlPropertyMetadata generateVersionPropertyMetadata(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo) {
        return new EqlPropertyMetadata.Builder(VERSION, Long.class, H_LONG).required().column(version).build();
    }

    private EqlPropertyMetadata generateKeyPropertyMetadata(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo) {
        final Class<? extends Comparable<?>> keyType = getKeyType(parentInfo.entityType);
        if (isOneToOne(parentInfo.entityType)) {
            switch (parentInfo.category) {
            case PERSISTENT:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_LONG).required().column(id).build();
            case QUERY_BASED:
                return new EqlPropertyMetadata.Builder(KEY, keyType, H_LONG).required().build();
            default:
                return null;
            }
        } else if (DynamicEntityKey.class.equals(keyType)) {
            return new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(generateCompositeKeyEqlExpression((Class<? extends AbstractEntity<DynamicEntityKey>>) parentInfo.entityType)).implicit().required().build();
        } else {
            final Object keyHibType = typeResolver.basic(keyType.getName());
            switch (parentInfo.category) {
            case PERSISTENT:
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
            case QUERY_BASED:
                if (isSyntheticBasedOnPersistentEntityType(parentInfo.entityType)) {
                    return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().column(key).build();
                }
                return new EqlPropertyMetadata.Builder(KEY, keyType, keyHibType).required().build();
            default:
                return null;
            }
        }
    }

    private ExpressionModel generateUnionCommonDescPropExpressionModel(final List<Field> unionMembers, final String contextPropName) {
        final List<String> unionMembersNames = unionMembers.stream().filter(et -> hasDescProperty((Class<? extends AbstractEntity<?>>) et.getType())).map(et -> et.getName()).collect(toList());
        return generateUnionEntityPropertyContextualExpression(unionMembersNames, DESC, contextPropName);
    }

    private List<EqlPropertyMetadata> generatePropertyMetadatasForEntity(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo, final Class<? extends AbstractEntity<?>> actualType) {
        final List<EqlPropertyMetadata> result = new ArrayList<>();
        if (UNION == parentInfo.category) {
            result.addAll(generateUnionImplicitCalcSubprops((Class<? extends AbstractUnionEntity>) parentInfo.entityType, null));
            unionProperties((Class<? extends AbstractUnionEntity>) parentInfo.entityType).stream().forEach(field -> result.add(getCommonPropInfo(field, parentInfo.entityType, null)));
        } else {
            generateIdPropertyMetadata(parentInfo).ifPresent(idPmd -> result.add(idPmd));

            result.add(generateKeyPropertyMetadata(parentInfo));

            final List<Field> restOfPropsFields = getRestOfProperties(parentInfo, actualType);
            final Set<String> addedProps = new HashSet<>();
            addedProps.add(DESC);
            parentInfo.compositeKeyMembers.stream().forEach(f -> addedProps.add(f._1));
            parentInfo.compositeKeyMembers.stream().forEach(f -> restOfPropsFields.stream().filter(p -> f._1.equals(p.getName())).findAny().ifPresent(km -> result.add(getCommonPropInfo(km, parentInfo.entityType, null))));

            restOfPropsFields.stream().filter(p -> DESC.equals(p.getName())).findAny().ifPresent(desc -> result.add(getCommonPropInfo(desc, parentInfo.entityType, null)));

            if (PERSISTENT == parentInfo.category || QUERY_BASED == parentInfo.category && EntityUtils.isPersistedEntityType(parentInfo.entityType.getSuperclass())) {
                result.add(generateVersionPropertyMetadata(parentInfo));
            }

            for (final Field field : restOfPropsFields.stream().filter(p -> !addedProps.contains(p.getName())).collect(toList())) {
                result.add(isOne2One_association(actualType/*parentInfo.entityType*/, field.getName()) ? getOneToOnePropInfo(field, parentInfo)
                        : getCommonPropInfo(field, actualType/*parentInfo.entityType*/, null));
            }
        }

        return result;
    }

    public static List<Field> getRestOfProperties(final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo, final Class<? extends AbstractEntity<?>> actualType) {
       return streamRealProperties(actualType/*parentInfo.entityType*/)
              .filter(propField -> (isAnnotationPresent(propField, Calculated.class) ||
                      isAnnotationPresent(propField, MapTo.class) ||
                      isAnnotationPresent(propField, CritOnly.class) ||
                      isOne2One_association(actualType/*parentInfo.entityType*/, propField.getName()) ||
                      parentInfo.category == QUERY_BASED) &&
                      !specialProps.contains(propField.getName()) &&
                      !(Collection.class.isAssignableFrom(propField.getType()) && hasLinkProperty(actualType/*parentInfo.entityType*/, propField.getName())))
             .collect(toList());
    }

    private static String getColumnName(final String propName, final MapTo mapTo, final String parentPrefix) {
        return (parentPrefix != null ? parentPrefix + "_" : "") + (isNotEmpty(mapTo.value()) ? mapTo.value() : propName.toUpperCase() + "_");
    }

    private static PropColumn generateColumn(final String columnName, final IsProperty isProperty) {
        final Integer length = isProperty.length() > 0 ? isProperty.length() : null;
        final Integer precision = isProperty.precision() >= 0 ? isProperty.precision() : null;
        final Integer scale = isProperty.scale() >= 0 ? isProperty.scale() : null;
        return new PropColumn(removeObsoleteUnderscore(columnName), length, precision, scale);
    }

    private static List<EqlPropertyMetadata> getCompositeUserTypeSubpropsMetadata(final ICompositeUserTypeInstantiate hibType, final String parentColumnPrefix, final ExpressionModel expr) {
        final String[] propNames = hibType.getPropertyNames();
        final Object[] propHibTypes = hibType.getPropertyTypes();
        final Class<?> headerPropType = hibType.returnedClass();
        final List<EqlPropertyMetadata> result = new ArrayList<>();
        for (int i = 0; i != propNames.length; i++) {
            final String propName = propNames[i];
            final Object subHibType = propHibTypes[i];
            final Class<?> subJavaType = determinePropertyType(headerPropType, propName);

            final EqlPropertyMetadata.Builder subLmdInProgress = new EqlPropertyMetadata.Builder(propName, subJavaType, subHibType);
            if (parentColumnPrefix != null) { //persisted
                final String mapToColumn = getPropertyAnnotation(MapTo.class, headerPropType, propName).value();
                final String columnName = propNames.length == 1 ? parentColumnPrefix
                        : (parentColumnPrefix + (parentColumnPrefix.endsWith("_") ? "" : "_") + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
                final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, headerPropType, propName);
                subLmdInProgress.column(generateColumn(columnName, isProperty));
            } else if (expr != null) { // calculated
                subLmdInProgress.expression(expr);
            }

            result.add(subLmdInProgress.build());
        }
        return result;
    }

    private List<EqlPropertyMetadata> generateUnionImplicitCalcSubprops(final Class<? extends AbstractUnionEntity> unionPropType, final String contextPropName) {
        final List<Field> unionMembers = unionProperties(unionPropType);
        final List<String> unionMembersNames = unionMembers.stream().map(up -> up.getName()).collect(toList());
        final List<EqlPropertyMetadata> subitems = new ArrayList<>();
        subitems.add(new EqlPropertyMetadata.Builder(KEY, String.class, H_STRING).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, KEY, contextPropName)).implicit().build());
        subitems.add(new EqlPropertyMetadata.Builder(ID, Long.class, H_LONG).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, ID, contextPropName)).implicit().build());
        subitems.add(new EqlPropertyMetadata.Builder(DESC, String.class, H_STRING).expression(generateUnionCommonDescPropExpressionModel(unionMembers, contextPropName)).implicit().build());

        final List<String> commonProps = commonProperties(unionPropType).stream().filter(n -> !DESC.equals(n) && !KEY.equals(n)).collect(toList());
        final Class<?> firstUnionEntityPropType = unionMembers.get(0).getType(); // e.g. WagonSlot in TgBogieLocation
        for (final String commonProp : commonProps) {
            if (unionMembersNames.contains(commonProp)) {
                throw new EntityDefinitionException(format("The name of common prop [%s] conflicts with union prop [%s] in union entity [%s].", commonProp, commonProp, unionPropType.getSimpleName()));
            }
            final Class<?> javaType = determinePropertyType(firstUnionEntityPropType, commonProp);
            final Object subHibType = getHibernateType(findFieldByName(firstUnionEntityPropType, commonProp));

            subitems.add(new EqlPropertyMetadata.Builder(commonProp, javaType, subHibType).expression(generateUnionEntityPropertyContextualExpression(unionMembersNames, commonProp, contextPropName)).implicit().build());
        }

        return subitems;
    }

    private EqlPropertyMetadata getCommonPropInfo(final Field propField, final Class<? extends AbstractEntity<?>> entityType, final String parentPrefix) {
        if (isAnnotationPresent(propField, CritOnly.class)) {
            return new EqlPropertyMetadata.Builder(propField.getName(), propField.getType(), null).critOnly().required(isRequiredByDefinition(propField, entityType)).build();
        }

        final String propName = propField.getName();
        final Class<?> propType = propField.getType();
        final Object hibType = getHibernateType(propField);
        final MapTo mapTo = getAnnotation(propField, MapTo.class);
        final IsProperty isProperty = getAnnotation(propField, IsProperty.class);
        final Calculated calculated = getAnnotation(propField, Calculated.class);

        final Builder resultInProgress = new EqlPropertyMetadata.Builder(propName, propType, hibType);

        resultInProgress.required(isRequiredByDefinition(propField, entityType));

        if (mapTo != null && !isSyntheticEntityType(entityType) && calculated == null /* 2 last conditions are to overcome incorrect metadata combinations*/) {
            final String columnName = getColumnName(propName, mapTo, parentPrefix);
            if (isUnionEntityType(propType)) {
                final List<EqlPropertyMetadata> subitems = new ArrayList<>();
                final Class<? extends AbstractUnionEntity> unionPropType = (Class<? extends AbstractUnionEntity>) propType;
                subitems.addAll(generateUnionImplicitCalcSubprops(unionPropType, propName));
                for (final Field subpropField : unionProperties(unionPropType)) {
                    subitems.add(getCommonPropInfo(subpropField, (Class<? extends AbstractEntity<?>>) propType, columnName));
                }

                return resultInProgress.subitems(subitems).build();
            } else {
                if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                    return resultInProgress.column(generateColumn(columnName, isProperty)).build();
                } else {
                    return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, columnName, null)).build();
                }
            }
        } else if (calculated != null) {
            if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                return resultInProgress.expression(extractExpressionModelFromCalculatedProperty(entityType, propField)).build();
            } else {
                return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, extractExpressionModelFromCalculatedProperty(entityType, propField))).build();
            }
        } else { // synthetic entity
            if (isUnionEntityType(propType)) {
                final List<EqlPropertyMetadata> subitems = new ArrayList<>();
                final Class<? extends AbstractUnionEntity> unionPropType = (Class<? extends AbstractUnionEntity>) propType;
                subitems.addAll(generateUnionImplicitCalcSubprops(unionPropType, propName));
                for (final Field subpropField : unionProperties(unionPropType)) {
                    subitems.add(getCommonPropInfo(subpropField, (Class<? extends AbstractEntity<?>>) propType, null));
                }

                return resultInProgress.subitems(subitems).build();
            } else {
                if (!(hibType instanceof ICompositeUserTypeInstantiate)) {
                    return resultInProgress.build();
                } else {
                    return resultInProgress.subitems(getCompositeUserTypeSubpropsMetadata((ICompositeUserTypeInstantiate) hibType, null, null)).build();
                }

            }
        }
    }

    private EqlPropertyMetadata getOneToOnePropInfo(final Field propField, final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo) {
        final String propName = propField.getName();
        final Class<?> javaType = propField.getType();
        final Object hibType = getHibernateType(propField);

        // 1-2-1 is not required to exist -- that's why need longer formula -- that's why 1-2-1 is in fact implicitly calculated nullable prop
        final ExpressionModel expressionModel = expr().model(select((Class<? extends AbstractEntity<?>>) propField.getType()).where().prop(KEY).eq().extProp(ID).model()).model();
        return new EqlPropertyMetadata.Builder(propName, javaType, hibType).notRequired().expression(expressionModel).implicit().build();
    }

    private final static Table generateTable(final String tableName, final List<EqlPropertyMetadata> propsMetadatas) {
        final Map<String, String> columns = new HashMap<>();
        for (final EqlPropertyMetadata el : propsMetadatas) {

            if (el.column != null) {
                columns.put(el.name, el.column.name);
            } else if (!el.subitems().isEmpty()) {
                for (final EqlPropertyMetadata subitem : el.subitems()) {
                    if (subitem.expressionModel == null) {
                        columns.put(el.name + "." + subitem.name, subitem.column.name);
                    }
                }
            }
        }

        return new Table(tableName, columns);
    }

    /**
     * Generates a DB table representation, an entity is mapped to, specific to batch insertion.
     *
     * @param tableName
     * @param propsMetadata
     * @return
     */
    private final static TableStructForBatchInsertion generateTableWithPropColumnInfo(final String tableName, final List<EqlPropertyMetadata> propsMetadata) {
        final List<PropColumnInfo> columns = new ArrayList<>();
        for (final EqlPropertyMetadata el : propsMetadata) {
            if (!el.name.equals(ID) && !el.name.equals(VERSION)) {
                if (el.column != null) {
                    columns.add(new PropColumnInfo(isPersistedEntityType(el.javaType) ? el.name + "." + ID : el.name, el.column.name, el.hibType));
                } else if (!el.subitems().isEmpty()) {
                    if (el.hibType instanceof CompositeUserType) {
                        final List<String> columnNames = el.subitems().stream().filter(s -> s.column != null /* only relevant for persistent props */).map(s -> s.column.name).collect(toList());
                        if (!columnNames.isEmpty()) { // there was at least 1 persistent property
                            columns.add(new PropColumnInfo(el.name, columnNames, el.hibType));
                        }
                    } else {
                        for (final EqlPropertyMetadata subitem : el.subitems()) {
                            if (subitem.column != null) {
                                columns.add(new PropColumnInfo((el.name + "." + subitem.name + (isPersistedEntityType(subitem.javaType) ? "." + ID : "")), subitem.column.name, subitem.hibType));
                            }
                        }
                    }
                }
            }
        }

        return new TableStructForBatchInsertion(tableName, columns);
    }

    public static String removeObsoleteUnderscore(final String name) {
        return name.endsWith("_") && name.substring(0, name.length() - 1).contains("_")
                ? name.substring(0, name.length() - 1)
                : name;
    }

    public Map<String, Table> getTables() {
        return unmodifiableMap(tables);
    }

    public TableStructForBatchInsertion getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return tableStructsForBatchInsertion.get(entityType.getName());
    }

    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata() {
        return unmodifiableMap(entityPropsMetadata);
    }

    public EntityInfo<?> getEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }
        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        final List<EqlPropertyMetadata> propsMetadatas = generatePropertyMetadatasForEntity(eti, type);
        //entityPropsMetadata.put(type, t2(eti.category, propsMetadatas));
        final EntityInfo<?> created = new EntityInfo<>(type, eti.category);
        //domainInfo.put(type, created);
        addProps(created, domainInfo, propsMetadatas);

        return created;
    }

    public EntityInfo<?> getEnhancedEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }

        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        if (eti.category == QUERY_BASED) {
            return generateEnhancedEntityInfoForSyntheticType(eti, type);
        } else {
            return getEntityInfo(type);
        }
    }

}