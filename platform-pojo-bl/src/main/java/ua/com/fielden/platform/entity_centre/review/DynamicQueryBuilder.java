package ua.com.fielden.platform.entity_centre.review;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.*;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangePrefixEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangeSelectorEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.MnemonicEnum;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

import java.lang.reflect.Field;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.mnemonics.DateMnemonicUtils.dateOfRangeThatIncludes;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.isPropertyAuthorised;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.paramValue;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.utils.CollectionUtil.partitionBy;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.utils.MiscUtilities.prepare;
import static ua.com.fielden.platform.utils.Pair.pair;

/// A utility class that is responsible for building query implementation of [EntityQueryCriteria].
///
public class DynamicQueryBuilder {
    private static final Logger logger = getLogger(DynamicQueryBuilder.class);

    private DynamicQueryBuilder() {}

    /// This is a class that represents high-level abstraction for a crit-only property in a dynamic criteria entity.
    ///
    /// Consists of one or possibly two (for "from"/"to" or "is"/"is not") values / exclusiveness-flags,
    /// and strictly single datePrefix/Mnemonic/AndBefore triplet, "orNull" and "not" flags, and other stuff, which are necessary for query composition.
    ///
    public static class QueryProperty {

        private static final String ERR_CRITONLY_SUBMODEL_INACCESSIBLE = "@CritOnly property [%s] in [%s] has inaccessible submodel with name [%s_].";
        private static final String ERR_INCOMPATIBLE_CRITONLY_SUBMODEL_TYPE = "Submodel [%s_] for @CritOnly property [%s] in [%s] should have type ICompoundCondition0.";
        private static final String QP_PREFIX = "QP_";

        private Object value = null;
        private Object value2 = null;
        private Boolean exclusive = null;
        private Boolean exclusive2 = null;
        private DateRangePrefixEnum datePrefix = null;
        private MnemonicEnum dateMnemonic = null;
        private Boolean andBefore = null;
        private Boolean orNull = null;
        private Boolean not = null;
        private Integer orGroup = null;
        private boolean matchAnywhere = true; // if QueryProperty represents a string criterion without wildcards, then match anywhere is the default behaviour.

        private final Class<?> entityClass;
        private final String propertyName;
        private final String conditionBuildingName;
        private final boolean critOnly;
        private final boolean critOnlyWithModel;
        private final String propertyUnderCondition;
        private final ICompoundCondition0<?> critOnlyModel;
        private boolean single;
        private final boolean aECritOnlyChild;
        private final Class<?> type;
        /** The type of collection which contain this property. If this property is not in collection hierarchy it should be null. */
        private final Class<? extends AbstractEntity<?>> collectionContainerType;
        private final Class<? extends AbstractEntity<?>> collectionContainerParentType;
        private final String propertyNameOfCollectionParent;
        private final String collectionNameInItsParentTypeContext;
        /** Union entity related properties */
        private final boolean inUnionHierarchy;
        private final String unionParent;
        private final String unionGroup;
        /** Determines the union and collection nested properties */
        private final Boolean inNestedUnionAndCollections;

        /// Creates parameter name for [QueryProperty] instance (should be used to expand mnemonics value into conditions from EQL critCondition operator).
        ///
        public static String queryPropertyParamName(final CharSequence propertyName) {
            return QP_PREFIX + propertyName;
        }

        public QueryProperty(final Class<?> entityClass, final String propertyName) {
            this.entityClass = entityClass;
            final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(entityClass, propertyName);
            this.propertyName = propertyName;
            if (!isSupported(analyser.getPropertyType())) {
                throw new UnsupportedTypeException(analyser.getPropertyType());
            }
            this.inNestedUnionAndCollections = analyser.isUnionCollectionIntersects();
            if (this.inNestedUnionAndCollections) {
                throw new IllegalArgumentException("The nested collection or union properties are not supported yet! Please remove property [" + this.propertyName
                        + "] from criteria.");
            }
            this.type = analyser.getPropertyType();
            final Pair<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> collectionalTypes = analyser.getCollectionContainerAndItsParentType();
            final String propertyNameWithinCollectionalHierarchy;

            // Turned off the recognition of union members in order to treat them as any other property.
            // This results in combining conditions for union members with logical AND instead of OR.
            // TODO: Once it is proven in practice that AND is indeed more practical, the code needs to be cleaned up by removing the union-specific logic.
            this.inUnionHierarchy = false; // analyser.isInUnionHierarchy();
            if (this.inUnionHierarchy) {
                this.collectionContainerType = null;
                this.collectionContainerParentType = null;
                propertyNameWithinCollectionalHierarchy = null;
                this.propertyNameOfCollectionParent = null;
                this.collectionNameInItsParentTypeContext = null;
                this.unionParent = analyser.getUnionParent();
                this.unionGroup = analyser.getUnionGroup();
            } else if (collectionalTypes != null) {
                this.collectionContainerType = collectionalTypes.getKey();
                this.collectionContainerParentType = collectionalTypes.getValue();
                propertyNameWithinCollectionalHierarchy = analyser.getNamesWithinCollectionalHierarchy().getKey();
                this.propertyNameOfCollectionParent = analyser.getNamesWithinCollectionalHierarchy().getValue();
                this.collectionNameInItsParentTypeContext = analyser.getCollectionNameInItsParentTypeContext();
                this.unionParent = null;
                this.unionGroup = null;
            } else {
                this.collectionContainerType = null;
                this.collectionContainerParentType = null;
                propertyNameWithinCollectionalHierarchy = null;
                this.propertyNameOfCollectionParent = null;
                this.collectionNameInItsParentTypeContext = null;
                this.unionParent = null;
                this.unionGroup = null;
            }
            this.conditionBuildingName = isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL() ? propertyNameWithinCollectionalHierarchy : ALIAS + "."
                    + analyser.getCriteriaFullName();

            final CritOnly critAnnotation = analyser.getPropertyFieldAnnotation(CritOnly.class);
            this.critOnly = critAnnotation != null;
            final Optional<Field> critOnlySubmodelFieldOpt = critOnly ? findCritOnlySubmodelField(critAnnotation, analyser.propertyTypes[analyser.propertyTypes.length - 2], analyser.propertyNames[analyser.propertyNames.length - 1]) : empty();
            this.critOnlyWithModel = critOnlySubmodelFieldOpt.isPresent();
            if (this.critOnlyWithModel) {
                this.propertyUnderCondition = critAnnotation.propUnderCondition();
                this.critOnlyModel = getCritOnlySubmodel(critOnlySubmodelFieldOpt.get(), analyser.propertyTypes[analyser.propertyTypes.length - 2], analyser.propertyNames[analyser.propertyNames.length - 1]);
            } else {
                this.propertyUnderCondition = "";
                this.critOnlyModel = null;
            }

            final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
            final String penultPropertyName = PropertyTypeDeterminator.isDotExpression(propertyName) ? PropertyTypeDeterminator.penultAndLast(propertyName).getKey() : null;
            this.aECritOnlyChild = !isEntityItself && PropertyTypeDeterminator.isDotExpression(propertyName) && AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, this.entityClass, penultPropertyName);
            this.single = isCritOnly() && Type.SINGLE.equals(critAnnotation.value());
        }

        /// Creates [QueryProperty] ensuring that all its values (including [#value] and [#value2]) are empty.
        /// All other state (e.g. [#datePrefix]) is empty (aka `null`) by default -- please enhance this method
        /// if this will change in future.
        ///
        public static QueryProperty createEmptyQueryProperty(final Class<?> entityClass, final String propertyName) {
            final QueryProperty queryProperty = new QueryProperty(entityClass, propertyName);
            queryProperty.setValue(getEmptyValue(queryProperty.getType(), queryProperty.isSingle()));
            queryProperty.setValue2(getEmptyValue(queryProperty.getType(), queryProperty.isSingle()));
            return queryProperty;
        }

        public static boolean critOnlyWithMnemonics(final CritOnly critAnnotation) {
            final CritOnly.Mnemonics mnemonics = critAnnotation.mnemonics() == CritOnly.Mnemonics.DEFAULT ? critAnnotation.value().defaultMnemonics : critAnnotation.mnemonics();
            return mnemonics == CritOnly.Mnemonics.WITH;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(final Object value) {
            this.value = value;
        }

        public Object getValue2() {
            return value2;
        }

        public void setValue2(final Object value2) {
            this.value2 = value2;
        }

        public Boolean getExclusive() {
            return exclusive;
        }

        public void setExclusive(final Boolean exclusive) {
            this.exclusive = exclusive;
        }

        public Boolean getExclusive2() {
            return exclusive2;
        }

        public void setExclusive2(final Boolean exclusive2) {
            this.exclusive2 = exclusive2;
        }

        public DateRangePrefixEnum getDatePrefix() {
            return datePrefix;
        }

        public void setDatePrefix(final DateRangePrefixEnum datePrefix) {
            this.datePrefix = datePrefix;
        }

        public MnemonicEnum getDateMnemonic() {
            return dateMnemonic;
        }

        public void setDateMnemonic(final MnemonicEnum dateMnemonic) {
            this.dateMnemonic = dateMnemonic;
        }

        public Boolean getAndBefore() {
            return andBefore;
        }

        public void setAndBefore(final Boolean andBefore) {
            this.andBefore = andBefore;
        }

        public Boolean getOrNull() {
            return orNull;
        }

        public void setOrNull(final Boolean orNull) {
            this.orNull = orNull;
        }

        public Boolean getNot() {
            return not;
        }

        public void setNot(final Boolean not) {
            this.not = not;
        }

        public Integer getOrGroup() {
            return orGroup;
        }

        public void setOrGroup(final Integer orGroup) {
            this.orGroup = orGroup;
        }

        public boolean isMatchAnywhere() {
            return matchAnywhere;
        }

        public void setMatchAnywhere(final boolean matchAnywhere) {
            this.matchAnywhere = matchAnywhere;
        }

        /// Determines whether property have empty values.
        ///
        protected boolean hasEmptyValue() {
            if (isBoolean(type)) {
                if (single) { // boolean single cannot have an empty value, therefore return false
                    return false;
                }
                final boolean is = (Boolean) value;
                final boolean isNot = (Boolean) value2;
                return is && isNot || !is && !isNot; // both true and both false will be indicated as default
            } else if (isRangeType(type)) { // both values should be "empty" to be indicated as default
                return valueEqualsToEmpty(value, type, single) && valueEqualsToEmpty(value2, type, single);
            } else {
                return valueEqualsToEmpty(value, type, single);
            }
        }

        private static boolean valueEqualsToEmpty(final Object value, final Class<?> type, final boolean single) {
            // due to Web UI changes were empty value for String is always null, need to treat string nulls as empty
            // for Swing UI this was different, whereby value "" was treated at an empty string while null was NOT treated as an empty value
            return ((String.class == type || RichText.class == type) && value == null) || equalsEx(value, getEmptyValue(type, single));
        }

        /// Finds a submodel for a crit-only property, if present.
        /// It is important to traverse the type hierarchy. This is important for the Entity Centre generated types, because they extend the original entity type, where crit-only properties are defined.
        ///
        private static Optional<Field> findCritOnlySubmodelField(final CritOnly critAnnotation, final Class<?> entityType, final String propertyName) {
            final var canGetFieldInfo = !StringUtils.isEmpty(critAnnotation.propUnderCondition()) && /* a property name is specified */
                                        !AbstractEntity.class.equals(critAnnotation.entityUnderCondition()) && /* a domain entity is specified */
                                        isPropertyPresent(critAnnotation.entityUnderCondition(), critAnnotation.propUnderCondition()); /* a specified property belongs to a domain entity */
            return canGetFieldInfo
                   ? getFields(entityType, false).stream().filter(field -> (propertyName + "_").equals(field.getName())).findFirst() /* there may be a sub-model in type hierarchy */
                   : empty();
        }

        private static ICompoundCondition0<?> getCritOnlySubmodel(final Field exprField, final Class<?> type, final String propertyName) {
            try {
                exprField.setAccessible(true);
                final Object value = exprField.get(null);
                if (value instanceof ICompoundCondition0) {
                    return (ICompoundCondition0<?>) value;
                } else {
                    throw new EntityCentreExecutionException(format(ERR_INCOMPATIBLE_CRITONLY_SUBMODEL_TYPE, propertyName, propertyName, type.getSimpleName()));
                }
            } catch (final IllegalAccessException ex) {
                throw new EntityCentreExecutionException(format(ERR_CRITONLY_SUBMODEL_INACCESSIBLE, propertyName, type.getSimpleName(), propertyName));
            }
        }

        /// No values have been assigned and date mnemonics have not been used.
        ///
        public boolean isEmpty() {
            return hasEmptyValue() && datePrefix == null && dateMnemonic == null;
        }

        /// Determines whether property should be ignored during query composition, which means that 1) it is crit-only without model property; 2) it is empty and has not "orNull" condition
        /// assigned.
        ///
        public boolean shouldBeIgnored() {
            return isCritOnlyWithoutModel() || isAECritOnlyChild() || isEmptyWithoutMnemonics();
        }

        /// No values have been assigned and no mnemonics have been used.
        ///
        public boolean isEmptyWithoutMnemonics() {
            return isEmpty() && !TRUE.equals(orNull);
        }

        /// Returns `true` if this property belongs to some collection hierarchy. Method [#getCollectionContainerType()] should return the high level collection
        /// type.
        ///
        public boolean isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL() {
            // TODO implement the logic depicted in name
            return getCollectionContainerType() != null;
        }

        /// The type of collection which contain this property. If this property is not in collection hierarchy it should be `null`.
        ///
        public Class<? extends AbstractEntity<?>> getCollectionContainerType() {
            return collectionContainerType;
        }

        /// The type of the parent of collection which contain this property. If this property is not in collection hierarchy it should be `null`.
        ///
        public Class<? extends AbstractEntity<?>> getCollectionContainerParentType() {
            return collectionContainerParentType;
        }

        /// The condition building name that is related to parent collection or root entity type (if no collection exists on top of property).
        ///
        public String getConditionBuildingName() {
            return conditionBuildingName;
        }

        /// The name of collection, which contains this query property, in context of root entity type.
        ///
        public String getPropertyNameOfCollectionParent() {
            return propertyNameOfCollectionParent;
        }

        /// The name of collection, which contains this query property, in context of collection parent type.
        ///
        public String getCollectionNameInItsParentTypeContext() {
            return collectionNameInItsParentTypeContext;
        }

        /// Returns `true` if query property is inside nested (at least two) collections.
        ///
        public Boolean isInNestedUnionAndCollections() {
            return inNestedUnionAndCollections;
        }

        /// Returns value that indicates whether property is in union hierarchy or not.
        ///
        public boolean isInUnionHierarchy() {
            return inUnionHierarchy;
        }

        /// Returns union property parent name that is instance of [AbstractUnionEntity] class.
        ///
        public String getUnionParent() {
            return unionParent;
        }

        /// Returns the [AbstractEntity] property name that is in union.
        ///
        public String getUnionGroup() {
            return unionGroup;
        }

        /// Returns `true` if property is crit-only, `false` otherwise.
        ///
        public boolean isCritOnly() {
            return critOnly;
        }

        /// Returns `true` if this property is crit only and has associated query model, otherwise returns `false`
        ///
        public boolean isCritOnlyWithModel() {
            return critOnlyWithModel;
        }

        /// Returns `true` if property is crit-only without model, `false` otherwise.
        ///
        public boolean isCritOnlyWithoutModel() {
            return critOnly && !critOnlyWithModel;
        }

        /// Returns `true` if property is a child of crit-only AE property (dot-notated), `false` otherwise.
        ///
        public boolean isAECritOnlyChild() {
            return aECritOnlyChild;
        }

        /// The property name in dot-notation.
        ///
        public String getPropertyName() {
            return propertyName;
        }

        /// The type of property.
        ///
        public Class<?> getType() {
            return type;
        }

        /// Returns `true` if property is crit-only and single, `false` otherwise.
        ///
        public boolean isSingle() {
            return single;
        }
        public void setSingle(final boolean single) {
            this.single = single;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }
    }

    /// A bunch of properties relevant to single collection. Contains **collection filtering** properties and **ANY** / **ALL** properties.
    ///
    private static class CollectionProperties {
        private final Class<? extends AbstractEntity<?>> collectionContatinerType;
        private String nameOfCollectionController, propertyNameOfCollectionParent;
        private final List<QueryProperty> anyProperties, allProperties, filteringProperties;

        public CollectionProperties(final Class<? extends AbstractEntity<?>> collectionContatinerType) {
            this.collectionContatinerType = collectionContatinerType;
            anyProperties = new ArrayList<>();
            allProperties = new ArrayList<>();
            filteringProperties = new ArrayList<>();
        }

        /// Adds a property to a relevant sub-collection (FILTERING, ALL, ANY).
        ///
        public void add(final QueryProperty property) {
            if (nameOfCollectionController == null) {
                nameOfCollectionController = getNameOfCollectionController(property.getCollectionContainerParentType(), property.getCollectionNameInItsParentTypeContext());
            }
            if (propertyNameOfCollectionParent == null) {
                propertyNameOfCollectionParent = property.getPropertyNameOfCollectionParent();
            }
            final Calculated calcAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, property.getEntityClass(), property.getPropertyName());
            final Boolean all = calcAnnotation == null ? null : calcAnnotation.attribute().equals(CalculatedPropertyAttribute.ALL) ? Boolean.TRUE
                    : calcAnnotation.attribute().equals(CalculatedPropertyAttribute.ANY) ? Boolean.FALSE : null;
            if (Boolean.TRUE.equals(all)) {
                allProperties.add(property); // ALL properties list
            } else if (Boolean.FALSE.equals(all)) {
                anyProperties.add(property); // ANY properties list
            } else {
                filteringProperties.add(property); // FILTERING properties list
            }
        }

        public Class<? extends AbstractEntity<?>> getCollectionContatinerType() {
            return collectionContatinerType;
        }

        public List<QueryProperty> getAnyProperties() {
            return anyProperties;
        }

        public List<QueryProperty> getAllProperties() {
            return allProperties;
        }

        public List<QueryProperty> getFilteringProperties() {
            return filteringProperties;
        }

        /// Returns `true` if collection has at least one aggregated (at this stage only ANY or ALL) condition, which means that sub-model generation will be performed.
        /// Filtering conditions will be irrelevant in case when no aggregated conditions appear.
        ///
        public boolean hasAggregatedCondition() {
            return getAnyProperties().size() + getAllProperties().size() > 0;
        }

        /// The name of collection, which contains this query property, in context of root entity type.
        ///
        public String getPropertyNameOfCollectionParent() {
            return propertyNameOfCollectionParent;
        }

        public String getNameOfCollectionController() {
            return nameOfCollectionController;
        }

        /// Returns the name of "keyMember" which defines "collectivity" for "collectionElementType".
        ///
        private static String getNameOfCollectionController(final Class<? extends AbstractEntity<?>> collectionOwnerType, final String collectionName) {
            return Finder.findLinkProperty(collectionOwnerType, collectionName);
        }
    }

    /// Enhances current query by property conditions (property could form part of "exists"/"not_exists" statements for collections or part of simple "where" statement).
    ///
    private static <ET extends AbstractEntity<?>> ICompleted<ET> buildConditions(final IJoin<ET> query, final List<QueryProperty> properties, final Optional<Pair<IQueryEnhancer<ET>, Optional<CentreContext<ET, ?>>>> queryEnhancerAndContext, final IDates dates) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;

        // create empty map consisting of [collectionType => (ANY properties, ALL properties)] entries, which forms exactly one entry for one collectional hierarchy
        final Map<Class<? extends AbstractEntity<?>>, CollectionProperties> collectionalProperties = new LinkedHashMap<>();
        // map for union properties.
        final Map<String, Map<String, List<QueryProperty>>> unionProperties = new LinkedHashMap<>();
        // map for OR groups of properties.
        final Map<Integer, List<QueryProperty>> orGroups = new TreeMap<>(); // lets have it sorted by group number
        // traverse all properties to enhance resulting query
        for (final QueryProperty property : properties) {
            if (!property.shouldBeIgnored() && isPropertyAuthorised(property.getEntityClass(), property.getPropertyName())) {
                if (property.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL()) { // the property is in collection hierarchy. So, separate collection sub-model (EXISTS or NOT_EXISTS) should be enhanced by this property's criteria.
                    final Class<? extends AbstractEntity<?>> ccType = property.getCollectionContainerType();
                    if (!collectionalProperties.containsKey(ccType)) {
                        collectionalProperties.put(ccType, new CollectionProperties(ccType));
                    }
                    collectionalProperties.get(ccType).add(property);
                } else if (property.isInUnionHierarchy()) { // creates the union properties map
                    Map<String, List<QueryProperty>> unionSubGroup = unionProperties.get(property.getUnionParent());
                    if (unionSubGroup == null) {
                        unionSubGroup = new LinkedHashMap<>();
                        unionProperties.put(property.getUnionParent(), unionSubGroup);
                    }
                    List<QueryProperty> groupProps = unionSubGroup.get(property.getUnionGroup());
                    if (groupProps == null) {
                        groupProps = new ArrayList<>();
                        unionSubGroup.put(property.getUnionGroup(), groupProps);
                    }
                    groupProps.add(property);
                } else if (property.getOrGroup() != null && property.getOrGroup() >= 1 && property.getOrGroup() <= 9) {
                    List<QueryProperty> orGroupProps = orGroups.get(property.getOrGroup());
                    if (orGroupProps == null) {
                        orGroupProps = new ArrayList<>();
                        orGroups.put(property.getOrGroup(), orGroupProps);
                    }
                    orGroupProps.add(property);
                } else { // main query should be enhanced in case of simple property
                    compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCondition(property, false, dates));
                }
            }
        }
        // enhances query with OR groups
        for (final List<QueryProperty> orGroup : orGroups.values()) {
            compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildOrGroup(orGroup, dates)); // please note that '.condition(' construction adds parentheses itself when converting to SQL -- no need to provide explicit parentheses
        }

        //enhances query with union property condition
        for (final Map<String, List<QueryProperty>> unionGroup : unionProperties.values()) {
            compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildUnion(unionGroup, dates));
        }
        // enhance main model with collectional hierarchies models
        for (final CollectionProperties collectionProperties : collectionalProperties.values()) {
            if (collectionProperties.hasAggregatedCondition()) {
                compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCollection(collectionProperties, ALIAS, dates));
            } else {
                // TODO
                logger.warn("There are no aggregated conditions for collection [" + collectionProperties + "] in type " + collectionProperties
                        + ". All FILTERING conditions (if any) will be disregarded.");
            }
        }

        if (queryEnhancerAndContext.isPresent()) {
            final IWhere0<ET> where0 = compoundCondition == null ? query.where() : query.where().condition(compoundCondition.model()).and();
            return queryEnhancerAndContext.get().getKey().enhanceQuery(where0, queryEnhancerAndContext.get().getValue());
        } else {
            return compoundCondition == null ? query : query.where().condition(compoundCondition.model());
        }
    }

    /// Creates condition model for union group.
    ///
    private static <ET extends AbstractEntity<?>> ConditionModel buildUnion(final Map<String, List<QueryProperty>> unionGroup, final IDates dates) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;
        for (final List<QueryProperty> properties : unionGroup.values()) {
            compoundCondition = getConditionOperatorOr(condOperand, compoundCondition).condition(buildUnionGroup(properties, dates));
        }
        return compoundCondition.model();
    }

    /// Creates condition model for OR group.
    ///
    /// @param properties non-empty [QueryProperty] list depicting the group of OR-glued conditions
    ///
    private static <ET extends AbstractEntity<?>> ConditionModel buildOrGroup(final List<QueryProperty> properties, final IDates dates) {
        final IStandAloneConditionOperand<ET> cond = EntityQueryUtils.<ET> cond(); // to avoid creating it each time accumulator function is performed
        return properties.stream()
            .reduce((IStandAloneConditionCompoundCondition<ET>) null,
                    (partialCompoundCondition, queryProperty) -> getConditionOperatorOr(cond, partialCompoundCondition).condition(buildCondition(queryProperty, false, dates)),
                    (c1, c2) -> {throw new UnsupportedOperationException("Combining is not applicable here.");}
            ).model(); // 'properties' are never empty, so it is NPE-safe
    }

    /// Creates condition model for union subgroup.
    ///
    /// @param properties
    /// @return
    private static <ET extends AbstractEntity<?>> ConditionModel buildUnionGroup(final List<QueryProperty> properties, final IDates dates) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;
        for (final QueryProperty qp : properties) {
            compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCondition(qp, false, dates));
        }
        return compoundCondition.model();
    }

    /// Defines a logic that determines an empty value according to `type` and `single` flag.
    ///
    public static Object getEmptyValue(final Class<?> type, final boolean single) {
        if (isEntityType(type)) {
            if (single) {
                return null;
            } else {
                return new ArrayList<String>();
            }
        } else if (isString(type) || isRichText(type)) {
            return "";
        } else if (isBoolean(type)) {
            return true;
        } else if (isRangeType(type)) {
            return null;
        } else {
            throw new UnsupportedTypeException(type);
        }
    }

    /// Creates a date period \[from; to] from a period defined by (datePrefix; dateMnemonic).
    /// IMPORTANT : please consider that left boundary should be inclusive and right -- exclusive! E.g. CURR YEAR converts to (01.01.2011 00:00; 01.01.2012 00:00) and need to be
    /// used as _prop(propertyName).**ge()**.val(from).and().prop(propertyName).**lt()**.val(to)_ in terms of Entity Query.
    ///
    public static Pair<Date, Date> getDateValuesFrom(final DateRangePrefixEnum datePrefix, final MnemonicEnum dateMnemonic, final Boolean andBefore, final IDates dates) {
        final Date now = dates.now().toDate();
        final Date from = Boolean.TRUE.equals(andBefore) ? null : dateOfRangeThatIncludes(now, DateRangeSelectorEnum.BEGINNING, datePrefix, dateMnemonic, dates);
        final Date to = Boolean.FALSE.equals(andBefore) ? null : dateOfRangeThatIncludes(now, DateRangeSelectorEnum.ENDING, datePrefix, dateMnemonic, dates);
        // left boundary should be inclusive and right -- exclusive!
        return pair(from, to);
    }

    /// Creates a new array of values based on the passed string by splitting criteria using comma and by changing wildcards `*` to SQL wildcards `%`.
    /// Values that do not have any wildcards get them automatically injected at the beginning and end to ensure the match-anywhere strategy.
    ///
    /// @param criteria
    /// @return
    public static String[] prepCritValuesForStringTypedProp(final String criteria) {
        if (StringUtils.isEmpty(criteria)) {
            return new String[] {};
        }

        final String[] crits = criteria.split(",");
        for (int index = 0; index < crits.length; index++) {
            if (!crits[index].contains("*")) {
                crits[index] = "*" + crits[index] + "*";
            }
            crits[index] = prepare(crits[index]);
        }
        return crits;
    }

    /// Adjusts string criteria by changing wildcards `*` to SQL wildcards `%`, if they exist.
    /// Otherwise, if `property` requires "match anywhere", prepends and appends the wildcard to the criteria value to match anywhere.
    ///
    private static String prepCritValuesForSingleStringTypedProp(final QueryProperty property) {
        final String criteria = (String) property.getValue();
        if (property.isMatchAnywhere() && !criteria.contains("*")) {
            return prepare("*" + criteria + "*");
        }
        return prepare(criteria);
    }

    /// Creates new array based on the passed list of string. This method also changes * to % for every element of the passed list.
    ///
    public static String[] prepCritValuesForEntityTypedProp(final List<String> criteria) {
        return prepare(criteria);
    }

    /// Returns `true` if the `type` is supported in dynamic criteria, `false` otherwise.
    ///
    private static boolean isSupported(final Class<?> type) {
        return isEntityType(type) || isString(type) || isRichText(type) || isBoolean(type) || isRangeType(type) || isDynamicEntityKey(type);
    }

    /// Builds collection condition including exists / not exists inner models based on ALL/ANY properties inside collection.
    ///
    /// Both ALL and ANY conditions are allowed for property.
    ///
    /// If **ANY** condition has been applied -- appropriate **EXISTS** model for collection will be enhanced (will be concatenated with previous conditions using
    /// **AND**!).
    /// If **ALL** condition has been applied -- appropriate **EXISTS** (will be concatenated with previous conditions using **AND**!) and appropriate **NOT_EXISTS**
    /// (**NEGATED** condition will be concatenated with previous conditions using **OR**!) models for collection will be enhanced (+**NOT_EXISTS** without conditions will
    /// be created and concatenated using **OR**!).
    ///
    private static <ET extends AbstractEntity<?>> ConditionModel buildCollection(final CollectionProperties collectionProperties, final String alias, final IDates dates) {
        // e.g. : "WorkOrder.vehicle.statusChanges.[vehicleKey/status.active]". Then:
        // property.getCollectionContainerType() == VehicleStatusChange.class
        // property.getCollectionContainerParentType() == Vehicle.class
        // property.getCollectionNameInItsParentTypeContext() == statusChanges
        // nameOfCollectionController == "vehicleKey"
        // property.getPropertyNameOfCollectionParent() == "vehicle"
        final Class<? extends AbstractEntity<?>> collectionContainerType = collectionProperties.getCollectionContatinerType();
        final String nameOfCollectionController = collectionProperties.getNameOfCollectionController();
        final String mainModelProperty = collectionProperties.getPropertyNameOfCollectionParent().isEmpty() ? alias : alias + "."
                + collectionProperties.getPropertyNameOfCollectionParent();

        final IStandAloneConditionOperand<ET> collectionBegin = cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;
        // enhance collection by ANY part
        if (!collectionProperties.getAnyProperties().isEmpty()) {
            final Iterator<QueryProperty> anyIter = collectionProperties.getAnyProperties().iterator();
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> anyExists_withDirectConditions = cond().condition(buildCondition(anyIter.next(), false, dates));
            while (anyIter.hasNext()) { // enhance EXISTS model with appropriate condition
                anyExists_withDirectConditions = anyExists_withDirectConditions.and().condition(buildCondition(anyIter.next(), false, dates));
            }

            final ICompoundCondition0<? extends AbstractEntity<?>> subModel = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties(), dates).and().condition(anyExists_withDirectConditions.model());

            compoundCondition = getConditionOperator(collectionBegin, compoundCondition).condition(cond().exists(subModel.model()).model());
        }
        // enhance collection by ALL part
        if (!collectionProperties.getAllProperties().isEmpty()) {
            final Iterator<QueryProperty> allIter = collectionProperties.getAllProperties().iterator();
            final EntityResultQueryModel<?> allNotExists_withNoConditions = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties(), dates).model();
            final QueryProperty firstProperty = allIter.next();
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> allExists_withDirectConditions = cond().condition(buildCondition(firstProperty, false, dates));
            //createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin();
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> allNotExists_withNegatedConditions = cond().condition(buildCondition(firstProperty, true, dates));
            //createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin();
            while (allIter.hasNext()) { // enhance EXISTS / NOT_EXISTS model with appropriate direct / negated condition
                final QueryProperty nextProperty = allIter.next();
                allExists_withDirectConditions = allExists_withDirectConditions.and().condition(buildCondition(nextProperty, false, dates));
                allNotExists_withNegatedConditions = allNotExists_withNegatedConditions.or().condition(buildCondition(nextProperty, true, dates));
            }

            final ICompoundCondition0<? extends AbstractEntity<?>> subModel_allExists = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties(), dates).and().condition(allExists_withDirectConditions.model());
            final ICompoundCondition0<? extends AbstractEntity<?>> subModel_allNotExists = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties(), dates).and().condition(allNotExists_withNegatedConditions.model());

            // enhance main model by EXISTS / NOT_EXISTS models relevant to ALL properties in collectional hierarchy
            compoundCondition = getConditionOperator(collectionBegin, compoundCondition).condition(cond().notExists(allNotExists_withNoConditions)// entities with empty collection should be included!
            .or()//
            .exists(subModel_allExists.model()).and().notExists(subModel_allNotExists.model())//
            .model());
        }
        if (compoundCondition == null) {
            throw new RuntimeException("Collection of type " + collectionContainerType + " did not alter query.");
        }
        return compoundCondition.model();
    }

    /// Creates sub-model for collection, enhanced with FILTERING properties.
    ///
    private static <ET extends AbstractEntity<?>> ICompoundCondition0<ET> createSubmodel(final Class<ET> collectionContainerType, final String nameOfCollectionController, final String mainModelProperty, final List<QueryProperty> filteringProperties, final IDates dates) {
        final ICompoundCondition0<ET> submodelThroghLinkProperty = select(collectionContainerType).where().prop(nameOfCollectionController).eq().prop(mainModelProperty);
        if (filteringProperties.isEmpty()) {
            return submodelThroghLinkProperty;
        }
        IStandAloneConditionCompoundCondition<ET> aloneCompCond = EntityQueryUtils.<ET> cond().condition(buildCondition(filteringProperties.get(0), false, dates)); // enhance sub-model with first FILTERING property
        for (int i = 1; i < filteringProperties.size(); i++) {
            aloneCompCond = aloneCompCond.and().condition(buildCondition(filteringProperties.get(i), false, dates)); // enhance sub-model with rest FILTERING properties
        }
        return submodelThroghLinkProperty.and().condition(aloneCompCond.model());
    }

    private static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> getConditionOperator(final IStandAloneConditionOperand<ET> collectionBegin, final IStandAloneConditionCompoundCondition<ET> compoundCondition) {
        return compoundCondition == null ? collectionBegin : compoundCondition.and();
    }

    private static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> getConditionOperatorOr(final IStandAloneConditionOperand<ET> collectionBegin, final IStandAloneConditionCompoundCondition<ET> compoundCondition) {
        return compoundCondition == null ? collectionBegin : compoundCondition.or();
    }

    /// Enhances "where" with concrete property condition defined by "key" parameter taking into account condition negation and **null** values treatment.
    ///
    /// @param isNegated -- indicates whether appropriate condition should be negated
    ///
    public static <ET extends AbstractEntity<?>> ConditionModel buildCondition(final QueryProperty property, final String propertyName, final boolean isNegated, final IDates dates) {
        final boolean orNull = Boolean.TRUE.equals(property.getOrNull());
        final boolean not = Boolean.TRUE.equals(property.getNot());
        // IMPORTANT : in order not to make extra joins properties like "alias.key", "alias.property1.key" and so on will be enhanced by
        // conditions like "alias is [not] null", "alias.property1 is [not] null" and so on (respectively).
        final IStandAloneConditionComparisonOperator<ET> sc = EntityQueryUtils.<ET> cond().prop(getPropertyNameWithoutKeyPart(propertyName));
        // indicates whether a condition should be negated
        final boolean negate = not ^ isNegated;
        if (property.isEmpty()) {
            if (!orNull) {
                throw new IllegalStateException("Should have at least NULL condition.");
            }
            return negate ? sc.isNotNull().model() : sc.isNull().model();
        } else {
            // indicates whether nulls should be considered in a query
            final boolean considerNulls = negate ^ orNull;
            final IStandAloneConditionOperand<ET> whereAtGroup2 = considerNulls ? sc.isNull().or() : sc.isNotNull().and();
            final ConditionModel subModel = buildAtomicCondition(property, propertyName, dates);
            return negate ? whereAtGroup2.negatedCondition(subModel).model() : whereAtGroup2.condition(subModel).model();
        }
    }

    /// More specific use of the previous method [#buildCondition(QueryProperty,String,boolean,IDates)].
    ///
    /// @param property
    /// @param isNegated
    /// @param dates
    ///
    /// @return
    private static <ET extends AbstractEntity<?>> ConditionModel buildCondition(final QueryProperty property, final boolean isNegated, final IDates dates) {
        if (property.isCritOnlyWithModel()) {
            return cond().critCondition(property.critOnlyModel, property.propertyUnderCondition, property.propertyName).model();
        }
        return buildCondition(property, property.getConditionBuildingName(), isNegated, dates);
    }

    /// Builds an atomic condition for `propertyName` based on its definition `property`. This could be "is True", ">= and <", "like", etc.
    /// This method handles all three kinds of criteria – single, range and multi.
    ///
    @SuppressWarnings("unchecked")
    private static <ET extends AbstractEntity<?>> ConditionModel buildAtomicCondition(final QueryProperty property, final String propertyName, final IDates dates) {
        if (property.isSingle()) {
            if (isString(property.getType()) || isRichText(property.getType())) {
                return cond().prop(propertyName).iLike().val(prepCritValuesForSingleStringTypedProp(property)).model();
            }
            return propertyEquals(propertyName, property.getValue()); // this covers the PropertyDescriptor case too
        } else if (isRangeType(property.getType())) {
            final boolean isDate = isDate(property.getType());
            final IStandAloneConditionComparisonOperator<ET> scag1 = EntityQueryUtils.<ET> cond().prop(propertyName);
            if (isDate && property.getDatePrefix() != null && property.getDateMnemonic() != null) {
                // left boundary should be inclusive and right -- exclusive!
                final Pair<Date, Date> fromAndTo = getDateValuesFrom(property.getDatePrefix(), property.getDateMnemonic(), property.getAndBefore(), dates);
                return scag1.ge()
                        .iVal(paramValue(fromAndTo.getKey(), isDate, property)).and().prop(propertyName)
                        .lt()
                        .iVal(paramValue(fromAndTo.getValue(), isDate, property)).model();
            } else {
                final IStandAloneConditionComparisonOperator<ET> scag2 = (TRUE.equals(property.getExclusive ()) ? scag1.gt() : scag1.ge())
                        .iVal(paramValue(property.getValue (), isDate, property)).and().prop(propertyName);
                return (TRUE.equals(property.getExclusive2()) ? scag2.lt() : scag2.le())
                        .iVal(paramValue(property.getValue2(), isDate, property)).model();
            }
        }
        // all other cases are multi-valued criteria, which get processed based on a type of the underlying property
        else if (isBoolean(property.getType())) {
            final boolean is = (Boolean) property.getValue();
            final boolean isNot = (Boolean) property.getValue2();
            return is && !isNot ? cond().prop(propertyName).eq().val(true).model() : !is && isNot ? cond().prop(propertyName).eq().val(false).model() : null;
        } else if (isString(property.getType()) || isRichText(property.getType())) {
            return cond().prop(propertyName).iLike().anyOfValues((Object[]) prepCritValuesForStringTypedProp((String) property.getValue())).model();
        } else if (isEntityType(property.getType())) {
            return isPropertyDescriptor(property.getType())
                    ? propertyDescriptorLike(propertyName, (List<String>) property.getValue(), (Class<AbstractEntity<?>>) getPropertyAnnotation(IsProperty.class, property.getEntityClass(), property.getPropertyName()).value())
                    : propertyLike(propertyName, (List<String>) property.getValue(), baseEntityType((Class<AbstractEntity<?>>) property.getType()));
        } else {
            throw new UnsupportedTypeException(property.getType());
        }
    }

    /// Generates condition for crit-only single entity type property for property name and value.
    ///
    /// This method is used in critCondition cases only. In other cases crit-only single properties are ignored.
    ///
    private static ConditionModel propertyEquals(final String propertyName, final Object value) {
        // this condition covers the PropertyDescriptor case too due to its proper conversion .toString() at the EQL level
        return cond().prop(propertyName).eq().val(value).model();
    }

    /// Generates condition for [PropertyDescriptor]-typed property with `searchValues` criteria and `enclosingEntityType`.
    ///
    /// @param propertyNameWithKey the name of property concatenated with ".key"
    /// @param searchValues represent search strings for the titles of properties modeled by [PropertyDescriptor]
    /// @param enclosingEntityType the type parameter in _PropertyDescriptor<...>_ property definition, which is the type that holds "described" properties
    ///
    private static ConditionModel propertyDescriptorLike(final String propertyNameWithKey, final List<String> searchValues, final Class<AbstractEntity<?>> enclosingEntityType) {
        final List<PropertyDescriptor<AbstractEntity<?>>> allPropertyDescriptors = getPropertyDescriptors(enclosingEntityType);
        final Map<Boolean, List<String>> searchVals = searchValues.stream().collect(groupingBy(str -> str.contains("*")));
        final Set<PropertyDescriptor<AbstractEntity<?>>> matchedPropDescriptors = new LinkedHashSet<>();
        concat(
            searchVals.getOrDefault(false, emptyList()).stream(),
            stream(prepCritValuesForEntityTypedProp(searchVals.getOrDefault(true, emptyList())))
        ).forEach(val -> matchedPropDescriptors.addAll(new PojoValueMatcher<>(allPropertyDescriptors, KEY, allPropertyDescriptors.size()).findMatches(val)));
        return matchedPropDescriptors.isEmpty()
            ? cond().val(0).eq().val(1).model() // no matching values -- can not be passed into '.in().values(...)'
            : cond().prop(getPropertyNameWithoutKeyPart(propertyNameWithKey)).in().values(matchedPropDescriptors.toArray()).model(); // passing of PropertyDescriptor instances works due to their proper conversion .toString() at the EQL level
    }

    /// Generates a condition for an entity-typed property using values specified for a criterion.
    ///
    /// @param prop  property path that ends with an entity-typed property or with `key`
    /// @param propType  type of the criterion property
    ///
    private static ConditionModel propertyLike(final String prop, final List<String> searchValues, final Class<? extends AbstractEntity<?>> propType) {
        return partitionBy(searchValues, str -> str.contains("*"))
                .map((wildVals, exactVals) -> {
                    final var propWithoutKey = getPropertyNameWithoutKeyPart(prop);
                    // TODO After #2452, adding ".id" for union-typed properties will no longer be necessary.
                    final var propId = propWithoutKey + (isUnionEntityType(propType) ? ".id" : "");
                    // Exact and wilcard search values.
                    if (!exactVals.isEmpty() && !wildVals.isEmpty()) {
                        return cond()
                                // Condition for exact search values.
                                .prop(propId).in().model(select(propType).where().prop(KEY).in().values(exactVals).model())
                                .or()
                                // Condition for wildcard search values.
                                .prop(propId).in().model(select(propType).where().prop(KEY).iLike().anyOfValues(prepCritValuesForEntityTypedProp(wildVals)).model())
                                .model();
                    }
                    // Only exact search values.
                    else if (!exactVals.isEmpty()) {
                        return cond()
                                .prop(propId).in().model(select(propType).where().prop(KEY).in().values(exactVals).model())
                                .model();
                    }
                    // Only wildcards.
                    else {
                        return cond()
                                .prop(propId)
                                .in()
                                .model(select(propType).where().prop(KEY).iLike().anyOfValues(prepCritValuesForEntityTypedProp(wildVals)).model())
                                .model();
                    }
                });
    }

    /// Indicates the unsupported type exception for dynamic criteria.
    ///
    protected static class UnsupportedTypeException extends AbstractPlatformRuntimeException {
        private static final long serialVersionUID = 8310488278117580979L;

        /// Creates the unsupported type exception for dynamic criteria.
        ///
        public UnsupportedTypeException(final Class<?> type) {
            super("The [" + type + "] type is not supported for dynamic criteria.");
        }
    }

    /// Starts query building with appropriate join condition.
    ///
    private static <E extends AbstractEntity<?>> IJoin<E> createJoinCondition(final Class<E> managedType) {
        // Wrapping into additional query with all calculated properties materialised into columns is needed to handle SQL Server limitation of aggregation on sub-queries.
        return select(select(managedType).model().setShouldMaterialiseCalcPropsAsColumnsInSqlQuery(true)).as(ALIAS);
    }

    private static final String ALIAS = "alias_for_main_criteria_type";

    /// Creates the property name that might be used in query. This condition property is aliased.
    ///
    public static String createConditionProperty(final String property) {
        return property.isEmpty() ? ALIAS : ALIAS + "." + property;
    }

    /// The same as [#createConditionProperty(String)], but with parameter of type [IConvertableToPath].
    ///
    /// **IMPORTANT:** At this stage there no way to differentiate between property meta-models coming from aliased and non-aliased instance of entity meta-models.
    /// It is important to use with method only in application to properties from non-aliased meta-models. Otherwise, a runtime error would occur at the EQL/SQL level due to incorrect aliases.
    ///
    /// @param property a property meta-model coming from a non-aliased instance of an entity meta-model.
    ///
    public static String createConditionProperty(final IConvertableToPath property) {
        return createConditionProperty(property.toPath());
    }

    /// Creates the query with configured conditions and enhances it using optional [IQueryEnhancer].
    ///
    public static <E extends AbstractEntity<?>> ICompleted<E> createQuery(final Class<E> managedType, final List<QueryProperty> queryProperties, final Optional<Pair<IQueryEnhancer<E>, Optional<CentreContext<E, ?>>>> queryEnhancerAndContext, final IDates dates) {
        return buildConditions(createJoinCondition(managedType), queryProperties, queryEnhancerAndContext, dates);
    }

    /// Creates the query with configured conditions.
    ///
    public static <E extends AbstractEntity<?>> ICompleted<E> createQuery(final Class<E> managedType, final List<QueryProperty> queryProperties, final IDates dates) {
        return buildConditions(createJoinCondition(managedType), queryProperties, Optional.empty(), dates);
    }

    /// Creates the aggregation query that groups by distribution properties and aggregates by aggregation properties.
    ///
    @SuppressWarnings("unchecked")
    //TODO Later the genClass property must be removed. This is an interim solution that allows to add .amount prefix to the money properties.
    public static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> createAggregationQuery(final EntityResultQueryModel<E> sourceQueryModel, final List<String> distributionProperties, final Class<E> genClass, final Map<String, String> yieldProperties) {

        sourceQueryModel.setFilterable(true);
        ICompleted<E> baseQuery = select(sourceQueryModel).as(ALIAS);
        for (final String groupProperty : distributionProperties) {
            baseQuery = groupBy(groupProperty, baseQuery);
        }
        ISubsequentCompletedAndYielded<E> yieldedQuery = null;
        for (final Map.Entry<String, String> yieldProperty : yieldProperties.entrySet()) {
            yieldedQuery = yieldedQuery == null ? DynamicQueryBuilder.yield(genClass, yieldProperty, baseQuery) : DynamicQueryBuilder.yield(genClass, yieldProperty, yieldedQuery);
        }
        if (yieldedQuery == null) {
            throw new IllegalStateException("The query was compound incorrectly!");
        }

        return yieldedQuery;
    }

    /// Groups the given query by specified property.
    ///
    private static <E extends AbstractEntity<?>> ICompleted<E> groupBy(final String distribution, final ICompleted<E> query) {
        return query.groupBy().prop(distribution.isEmpty() ? ALIAS : ALIAS + "." + distribution);
    }

    /// Groups the given query by specified property.
    ///
    //TODO Later the genClass property must be removed. This is an interim solution that allows to add .amount prefix to the money properties.
    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> yield(final Class<E> genClass, final Map.Entry<String, String> toYield, final ICompleted<E> query) {
        //TODO this code must be removed as a interim solution
        String aliasValue = toYield.getValue();
        if (!aliasValue.isEmpty() && Money.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(genClass, aliasValue))) {
            aliasValue += ".amount";
        }
        //remove code above
        return query.yield().prop(toYield.getKey().isEmpty() ? ALIAS : ALIAS + "." + toYield.getKey()).as(aliasValue);
    }

    /// Groups the given query by specified property.
    ///
    //TODO Later the genClass property must be removed. This is an interim solution that allows to add .amount prefix to the money properties.
    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> yield(final Class<E> genClass, final Map.Entry<String, String> yield, final ISubsequentCompletedAndYielded<E> query) {
        //TODO this code must be removed as a interim solution
        String aliasValue = yield.getValue();
        if (!aliasValue.isEmpty() && Money.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(genClass, aliasValue))) {
            aliasValue += ".amount";
        }
        //remove code above
        return query.yield().prop(yield.getKey().isEmpty() ? ALIAS : ALIAS + "." + yield.getKey()).as(aliasValue);
    }

    /// Removes ".key" part from propertyName.
    ///
    public static String getPropertyNameWithoutKeyPart(final String propertyName) {
        return KEY.equals(propertyName) ? ID : replaceLast(propertyName, ".key", "");
    }

    private static String replaceLast(final String s, final String what, final String byWhat) {
        return s.endsWith(what) ? s.substring(0, s.lastIndexOf(what)) : s;
    }
}
