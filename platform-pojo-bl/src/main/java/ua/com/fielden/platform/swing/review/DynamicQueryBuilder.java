package ua.com.fielden.platform.swing.review;

import static java.lang.Boolean.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.DateRangeSelectorEnum;
import ua.com.fielden.snappy.DateUtilities;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * An utility class that is responsible for building query implementation of {@link DynamicEntityQueryCriteria}.
 *
 * @author TG Team
 *
 */
public class DynamicQueryBuilder {
    private final static Logger logger = Logger.getLogger(DynamicQueryBuilder.class);

    /**
     * This is a class which represents high-level abstraction for criterion in dynamic criteria. <br>
     * <br>
     * Consists of one or possibly two (for "from"/"to" or "is"/"is not") values / exclusiveness-flags, <br>
     * and strictly single datePrefix/Mnemonic pair, "orNull", "not" and "all" flags, and other stuff, which are necessary for query composition.
     *
     * @author TG Team
     *
     */
    public static class QueryProperty {
        private Object value = null, value2 = null;
        private Boolean exclusive = null, exclusive2 = null;
        private DateRangePrefixEnum datePrefix = null;
        private MnemonicEnum dateMnemonic = null;
        private Boolean andBefore = null;
        private Boolean orNull = null;
        private Boolean not = null;

        private final Class<?> entityClass;
        private final String propertyName, conditionBuildingName;
        private final boolean critOnly, single;
        private final Class<?> type;
        /** The type of collection which contain this property. If this property is not in collection hierarchy it should be null. */
        private final Class<? extends AbstractEntity<?>> collectionContainerType, collectionContainerParentType;
        private final String propertyNameOfCollectionParent, collectionNameInItsParentTypeContext;
        /** Union entity related properties */
        private final boolean inUnionHierarchy;
        private final String unionParent;
        private final String unionGroup;
        /** Determines the union and collection nested properties */
        private final Boolean inNestedUnionAndCollections;

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
            this.inUnionHierarchy = analyser.isInUnionHierarchy();
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
            this.single = isCritOnly() && Type.SINGLE.equals(critAnnotation.value());
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

        /**
         * Determines whether property have empty values.
         *
         * @return
         */
        protected boolean hasEmptyValue() {
            if (EntityUtils.isBoolean(type)) {
                final boolean is = (Boolean) value;
                final boolean isNot = (Boolean) value2;
                return is && isNot || !is && !isNot; // both true and both false will be indicated as default
            } else if (EntityUtils.isRangeType(type)) { // both values should be "empty" to be indicated as default
                return valueEqualsToEmpty(value, type, single) && valueEqualsToEmpty(value2, type, single);
            } else {
                return valueEqualsToEmpty(value, type, single);
            }
        }

        private static boolean valueEqualsToEmpty(final Object value, final Class<?> type, final boolean single) {
            // due to Web UI changes were empty value for String is always null, need to treat string nulls as empty
            // for Swing UI this was different, whereby value "" was treated at an empty string while null was NOT treated as an empty value
            return (String.class == type && value == null) || EntityUtils.equalsEx(value, getEmptyValue(type, single));
        }

        /**
         * No values have been assigned and date mnemonics have not been used.
         *
         * @return
         */
        public boolean isEmpty() {
            return hasEmptyValue() && datePrefix == null && dateMnemonic == null;
        }

        /**
         * Determines whether property should be ignored during query composition, which means that 1) it is crit-only property; 2) it is empty and has not "orNull" condition
         * assigned.
         *
         * @return
         */
        public boolean shouldBeIgnored() {
            return isCritOnly() || isEmpty() && !TRUE.equals(orNull);
        }

        /**
         * Returns <code>true</code> if this property belongs to some collection hierarchy. Method {@link #getCollectionContainerType()} should return the high level collection
         * type.
         *
         * @return
         */
        public boolean isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL() {
            // TODO implement the logic depicted in name
            return getCollectionContainerType() != null;
        }

        /**
         * The type of collection which contain this property. If this property is not in collection hierarchy it should be <code>null</code>.
         *
         * @return
         */
        public Class<? extends AbstractEntity<?>> getCollectionContainerType() {
            return collectionContainerType;
        }

        /**
         * The type of the parent of collection which contain this property. If this property is not in collection hierarchy it should be <code>null</code>.
         *
         * @return
         */
        public Class<? extends AbstractEntity<?>> getCollectionContainerParentType() {
            return collectionContainerParentType;
        }

        /**
         * The condition building name that is related to parent collection or root entity type (if no collection exists on top of property).
         *
         * @return
         */
        public String getConditionBuildingName() {
            return conditionBuildingName;
        }

        /**
         * The name of collection, which contains this query property, in context of root entity type.
         *
         * @return
         */
        public String getPropertyNameOfCollectionParent() {
            return propertyNameOfCollectionParent;
        }

        /**
         * The name of collection, which contains this query property, in context of collection parent type.
         *
         * @return
         */
        public String getCollectionNameInItsParentTypeContext() {
            return collectionNameInItsParentTypeContext;
        }

        /**
         * Returns <code>true</code> if query property is inside nested (at least two) collections.
         *
         * @return
         */
        public Boolean isInNestedUnionAndCollections() {
            return inNestedUnionAndCollections;
        }

        /**
         * Returns value that indicates whether property is in union hierarchy or not.
         *
         * @return
         */
        public boolean isInUnionHierarchy() {
            return inUnionHierarchy;
        }

        /**
         * Returns union property parent name that is instance of {@link AbstractUnionEntity} class.
         *
         * @return
         */
        public String getUnionParent() {
            return unionParent;
        }

        /**
         * Returns the {@link AbstractEntity} property name that is in union.
         *
         * @return
         */
        public String getUnionGroup() {
            return unionGroup;
        }

        /**
         * Returns <code>true</code> if property is crit-only, <code>false</code> otherwise.
         *
         * @return
         */
        public boolean isCritOnly() {
            return critOnly;
        }

        /**
         * The property name in dot-notation.
         *
         * @return
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * The type of property.
         *
         * @return
         */
        public Class<?> getType() {
            return type;
        }

        /**
         * Returns <code>true</code> if property is crit-only and single, <code>false</code> otherwise.
         *
         * @return
         */
        public boolean isSingle() {
            return single;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }
    }

    /**
     * A bunch of properties relevant to single collection. Contains <b>collection filtering</b> properties and <b>ANY</b> / <b>ALL</b> properties.
     *
     * @author TG Team
     */
    private static class CollectionProperties {
        private final Class<? extends AbstractEntity<?>> collectionContatinerType;
        private String nameOfCollectionController, propertyNameOfCollectionParent;
        private final List<QueryProperty> anyProperties, allProperties, filteringProperties;

        public CollectionProperties(final Class<? extends AbstractEntity<?>> collectionContatinerType) {
            this.collectionContatinerType = collectionContatinerType;
            anyProperties = new ArrayList<QueryProperty>();
            allProperties = new ArrayList<QueryProperty>();
            filteringProperties = new ArrayList<QueryProperty>();
        }

        /**
         * Adds a property to a relevant sub-collection (FILTERING, ALL, ANY).
         *
         * @param all
         *            -- <code>true</code> to add to ALL properties, <code>false</code> -- to add to ANY properties, <code>null</code> to add to FILTERING properties.
         */
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

        /**
         * Returns <code>true</code> if collection has at least one aggregated (at this stage only ANY or ALL) condition, which means that sub-model generation will be performed.
         * Filtering conditions will be irrelevant in case when no aggregated conditions appear.
         *
         * @return
         */
        public boolean hasAggregatedCondition() {
            return getAnyProperties().size() + getAllProperties().size() > 0;
        }

        /**
         * The name of collection, which contains this query property, in context of root entity type.
         *
         * @return
         */
        public String getPropertyNameOfCollectionParent() {
            return propertyNameOfCollectionParent;
        }

        public String getNameOfCollectionController() {
            return nameOfCollectionController;
        }

        /**
         * Returns the name of "keyMember" which defines "collectivity" for "collectionElementType".
         *
         * @param collectionOwnerType
         * @param collectionName
         * @return
         */
        private static String getNameOfCollectionController(final Class<? extends AbstractEntity<?>> collectionOwnerType, final String collectionName) {
            return Finder.findLinkProperty(collectionOwnerType, collectionName);
        }
    }

    /**
     * Enhances current query by property conditions (property could form part of "exists"/"not_exists" statements for collections or part of simple "where" statement).
     *
     * @return
     */
    private static <ET extends AbstractEntity<?>> ICompleted<ET> buildConditions(final IJoin<ET> query, final List<QueryProperty> properties, final Optional<Pair<IQueryEnhancer<ET>, Optional<CentreContext<ET, ?>>>> queryEnhancerAndContext) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;

        // create empty map consisting of [collectionType => (ANY properties, ALL properties)] entries, which forms exactly one entry for one collectional hierarchy
        final Map<Class<? extends AbstractEntity<?>>, CollectionProperties> collectionalProperties = new LinkedHashMap<Class<? extends AbstractEntity<?>>, CollectionProperties>();
        // map for union properties.
        final Map<String, Map<String, List<QueryProperty>>> unionProperties = new LinkedHashMap<>();
        // traverse all properties to enhance resulting query
        for (final QueryProperty property : properties) {
            if (!property.shouldBeIgnored()) {
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
                } else { // main query should be enhanced in case of simple property
                    compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCondition(property, false));
                }
            }
        }
        //enhances query with union property condition
        for (final Map<String, List<QueryProperty>> unionGroup : unionProperties.values()) {
            compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildUnion(unionGroup));
        }
        // enhance main model with collectional hierarchies models
        for (final CollectionProperties collectionProperties : collectionalProperties.values()) {
            if (collectionProperties.hasAggregatedCondition()) {
                compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCollection(collectionProperties, ALIAS));
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

    /**
     * Creates condition model for union group.
     *
     * @param unionGroup
     * @return
     */
    private static <ET extends AbstractEntity<?>> ConditionModel buildUnion(final Map<String, List<QueryProperty>> unionGroup) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;
        for (final List<QueryProperty> properties : unionGroup.values()) {
            compoundCondition = getConditionOperatorOr(condOperand, compoundCondition).condition(buildUnionGroup(properties));
        }
        return compoundCondition.model();
    }

    /**
     * Creates condition model for union sub group.
     *
     * @param properties
     * @return
     */
    private static <ET extends AbstractEntity<?>> ConditionModel buildUnionGroup(final List<QueryProperty> properties) {
        final IStandAloneConditionOperand<ET> condOperand = EntityQueryUtils.<ET> cond();
        IStandAloneConditionCompoundCondition<ET> compoundCondition = null;
        for (final QueryProperty qp : properties) {
            compoundCondition = getConditionOperator(condOperand, compoundCondition).condition(buildCondition(qp, false));
        }
        return compoundCondition.model();
    }

    /**
     * Defines a logic that determines an empty value according to <code>type</code> and <code>single</code> flag.
     *
     * @param type
     * @param single
     * @return
     */
    public static Object getEmptyValue(final Class<?> type, final boolean single) {
        if (EntityUtils.isEntityType(type)) {
            if (single) {
                return null;
            } else {
                return new ArrayList<String>();
            }
        } else if (EntityUtils.isString(type)) {
            return "";
        } else if (EntityUtils.isBoolean(type)) {
            return true;
        } else if (EntityUtils.isRangeType(type)) {
            return null;
        } else {
            throw new UnsupportedTypeException(type);
        }
    }

    /**
     * Creates a date period [from; to] from a period defined by (datePrefix; dateMnemonic).
     *
     * IMPORTANT : please consider that left boundary should be inclusive and right -- exclusive! E.g. CURR YEAR converts to (01.01.2011 00:00; 01.01.2012 00:00) and need to be
     * used as <i>prop(propertyName).<b>ge()</b>.val(from).and().prop(propertyName).<b>lt()</b>.val(to)</i> in terms of Entity Query.
     *
     * @param datePrefix
     * @param dateMnemonic
     * @return
     */
    public static Pair<Date, Date> getDateValuesFrom(final DateRangePrefixEnum datePrefix, final MnemonicEnum dateMnemonic, final Boolean andBefore) {
        final DateUtilities du = new DateUtilities();
        final Date currentDate = new Date();
        final Date from = Boolean.TRUE.equals(andBefore) ? null : du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, datePrefix, dateMnemonic), //
        /*         */to = Boolean.FALSE.equals(andBefore) ? null : du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, datePrefix, dateMnemonic);
        // left boundary should be inclusive and right -- exclusive!
        return new Pair<Date, Date>(from, to);
    }

    /**
     * Creates a new array of values based on the passed string by splitting criteria using comma and by changing * to %.
     *
     * @param criteria
     * @return
     */
    public static String[] prepare(final String criteria) {
        if (StringUtils.isEmpty(criteria)) {
            return new String[] {};
        }

        final List<String> result = new ArrayList<String>();
        for (final String crit : criteria.split(",")) {
            result.add(PojoValueMatcher.prepare(crit));
        }
        return result.toArray(new String[] {});
    }

    /**
     * Creates new array based on the passed list of string. This method also changes * to % for every element of the passed list.
     *
     * @param criteria
     * @return
     */
    public static String[] prepare(final List<String> criteria) {
        return MiscUtilities.prepare(criteria);
    }

    /**
     * Returns <code>true</code> if the <code>type</code> is supported in dynamic criteria, <code>false</code> otherwise.
     *
     * @param type
     * @return
     */
    private static boolean isSupported(final Class<?> type) {
        return EntityUtils.isEntityType(type) || EntityUtils.isString(type) || EntityUtils.isBoolean(type) || EntityUtils.isRangeType(type) || EntityUtils.isDynamicEntityKey(type);
    }

    /**
     * Builds collection condition including exists / not exists inner models based on ALL/ANY properties inside collection.
     * <p>
     * Both ALL and ANY conditions are allowed for property.
     * <p>
     * If <b>ANY</b> condition has been applied -- appropriate <b>EXISTS</b> model for collection will be enhanced (will be concatenated with previous conditions using
     * <b>AND</b>!).<br>
     * If <b>ALL</b> condition has been applied -- appropriate <b>EXISTS</b> (will be concatenated with previous conditions using <b>AND</b>!) and appropriate <b>NOT_EXISTS</b>
     * (<b>NEGATED</b> condition will be concatenated with previous conditions using <b>OR</b>!) models for collection will be enhanced (+<b>NOT_EXISTS</b> without conditions will
     * be created and concatenated using <b>OR</b>!).<br>
     *
     * @param entry
     *            -- an entry consisting of [collectionType => (anyProperties, allProperties)] which forms exactly one collectional hierarchy
     * @return
     */
    private static <ET extends AbstractEntity<?>> ConditionModel buildCollection(final CollectionProperties collectionProperties, final String alias) {
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
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> anyExists_withDirectConditions = cond().condition(buildCondition(anyIter.next(), false));
            while (anyIter.hasNext()) { // enhance EXISTS model with appropriate condition
                anyExists_withDirectConditions = anyExists_withDirectConditions.and().condition(buildCondition(anyIter.next(), false));
            }

            final ICompoundCondition0<? extends AbstractEntity<?>> subModel = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().condition(anyExists_withDirectConditions.model());

            compoundCondition = getConditionOperator(collectionBegin, compoundCondition).condition(cond().exists(subModel.model()).model());
        }
        // enhance collection by ALL part
        if (!collectionProperties.getAllProperties().isEmpty()) {
            final Iterator<QueryProperty> allIter = collectionProperties.getAllProperties().iterator();
            final EntityResultQueryModel<?> allNotExists_withNoConditions = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).model();
            final QueryProperty firstProperty = allIter.next();
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> allExists_withDirectConditions = cond().condition(buildCondition(firstProperty, false));
            //createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin();
            IStandAloneConditionCompoundCondition<AbstractEntity<?>> allNotExists_withNegatedConditions = cond().condition(buildCondition(firstProperty, true));
            //createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin();
            while (allIter.hasNext()) { // enhance EXISTS / NOT_EXISTS model with appropriate direct / negated condition
                final QueryProperty nextProperty = allIter.next();
                allExists_withDirectConditions = allExists_withDirectConditions.and().condition(buildCondition(nextProperty, false));
                allNotExists_withNegatedConditions = allNotExists_withNegatedConditions.or().condition(buildCondition(nextProperty, true));
            }

            final ICompoundCondition0<? extends AbstractEntity<?>> subModel_allExists = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().condition(allExists_withDirectConditions.model());
            final ICompoundCondition0<? extends AbstractEntity<?>> subModel_allNotExists = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().condition(allNotExists_withNegatedConditions.model());

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

    /**
     * Creates sub-model for collection, enhanced with FILTERING properties.
     *
     * @param collectionContainerType
     * @param nameOfCollectionController
     * @param mainModelProperty
     * @param filteringProperties
     * @return
     */
    private static <ET extends AbstractEntity<?>> ICompoundCondition0<ET> createSubmodel(final Class<ET> collectionContainerType, final String nameOfCollectionController, final String mainModelProperty, final List<QueryProperty> filteringProperties) {
        final ICompoundCondition0<ET> submodelThroghLinkProperty = select(collectionContainerType).where().prop(nameOfCollectionController).eq().prop(mainModelProperty);
        if (filteringProperties.isEmpty()) {
            return submodelThroghLinkProperty;
        }
        IStandAloneConditionCompoundCondition<ET> aloneCompCond = EntityQueryUtils.<ET> cond().condition(buildCondition(filteringProperties.get(0), false)); // enhance sub-model with first FILTERING property
        for (int i = 1; i < filteringProperties.size(); i++) {
            aloneCompCond = aloneCompCond.and().condition(buildCondition(filteringProperties.get(i), false)); // enhance sub-model with rest FILTERING properties
        }
        return submodelThroghLinkProperty.and().condition(aloneCompCond.model());
    }

    private static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> getConditionOperator(final IStandAloneConditionOperand<ET> collectionBegin, final IStandAloneConditionCompoundCondition<ET> compoundCondition) {
        return compoundCondition == null ? collectionBegin : compoundCondition.and();
    }

    private static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> getConditionOperatorOr(final IStandAloneConditionOperand<ET> collectionBegin, final IStandAloneConditionCompoundCondition<ET> compoundCondition) {
        return compoundCondition == null ? collectionBegin : compoundCondition.or();
    }

    /**
     * Enhances "where" with concrete property condition defined by "key" parameter taking into account condition negation and <b>null</b> values treatment.
     *
     * @param where
     * @param key
     * @param isNegated
     *            -- indicates whether appropriate condition should be negated
     * @return
     */
    private static <ET extends AbstractEntity<?>> ConditionModel buildCondition(final QueryProperty property, final boolean isNegated) {
        final boolean orNull = Boolean.TRUE.equals(property.getOrNull());
        final boolean not = Boolean.TRUE.equals(property.getNot());
        final String propertyName = property.getConditionBuildingName();
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
            final ConditionModel subModel = buildAtomicCondition(property);
            return negate ? whereAtGroup2.negatedCondition(subModel).model() : whereAtGroup2.condition(subModel).model();
        }
    }

    /**
     * Builds atomic condition for some property like "is True", ">= and <", "like" etc. based on property type and assigned parameters.
     *
     * @param key
     * @param mainProperty
     * @param conditionGroup
     * @param propertyName
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <ET extends AbstractEntity<?>> ConditionModel buildAtomicCondition(final QueryProperty property) {
        final String propertyName = property.getConditionBuildingName();

        if (EntityUtils.isRangeType(property.getType())) {
            if (EntityUtils.isDate(property.getType()) && property.getDatePrefix() != null && property.getDateMnemonic() != null) {
                // left boundary should be inclusive and right -- exclusive!
                final Pair<Date, Date> fromAndTo = getDateValuesFrom(property.getDatePrefix(), property.getDateMnemonic(), property.getAndBefore());
                return cond().prop(propertyName).ge().iVal(fromAndTo.getKey()).and().prop(propertyName).lt().iVal(fromAndTo.getValue()).model();
            } else {
                final IStandAloneConditionComparisonOperator<ET> scag = EntityQueryUtils.<ET> cond().prop(propertyName);
                final IStandAloneConditionComparisonOperator<ET> scag2 = Boolean.TRUE.equals(property.getExclusive()) ? //
                /*      */scag.gt().iVal(property.getValue()).and().prop(propertyName) // exclusive
                : scag.ge().iVal(property.getValue()).and().prop(propertyName); // inclusive
                return Boolean.TRUE.equals(property.getExclusive2()) ? //
                /*      */scag2.lt().iVal(property.getValue2()).model() // exclusive
                : scag2.le().iVal(property.getValue2()).model(); // inclusive
            }
        } else if (EntityUtils.isBoolean(property.getType())) {
            final boolean is = (Boolean) property.getValue();
            final boolean isNot = (Boolean) property.getValue2();
            return is && !isNot ? cond().prop(propertyName).eq().val(true).model() : !is && isNot ? cond().prop(propertyName).eq().val(false).model() : null;
        } else if (EntityUtils.isString(property.getType())) {
            return cond().prop(propertyName).iLike().anyOfValues((Object[]) prepare((String) property.getValue())).model();
        } else if (EntityUtils.isEntityType(property.getType())) {
            return cond().prop(propertyName).iLike().anyOfValues((Object[]) prepare((List<String>) property.getValue())).model();
        } else {
            throw new UnsupportedTypeException(property.getType());
        }
    }

    /**
     * Indicates the unsupported type exception for dynamic criteria.
     *
     * @author TG Team
     *
     */
    protected static class UnsupportedTypeException extends RuntimeException {
        private static final long serialVersionUID = 8310488278117580979L;

        /**
         * Creates the unsupported type exception for dynamic criteria.
         *
         * @param type
         */
        public UnsupportedTypeException(final Class<?> type) {
            super("The [" + type + "] type is not supported for dynamic criteria.");
        }
    }

    /**
     * Starts query building with appropriate join condition.
     *
     * @return
     */
    private static <E extends AbstractEntity<?>> IJoin<E> createJoinCondition(final Class<E> managedType) {
        return select(managedType).as(ALIAS);
    }

    private static final String ALIAS = "alias_for_main_criteria_type";

    /**
     * Creates the property name that might be used in query. This condition property is aliased.
     *
     * @param property
     * @return
     */
    public static String createConditionProperty(final String property) {
        return property.isEmpty() ? ALIAS : ALIAS + "." + property;
    }

    /**
     * Creates the query with configured conditions and enhances it using optional {@link IQueryEnhancer}.
     *
     * @return
     */
    public static <E extends AbstractEntity<?>> ICompleted<E> createQuery(final Class<E> managedType, final List<QueryProperty> queryProperties, final Optional<Pair<IQueryEnhancer<E>, Optional<CentreContext<E, ?>>>> queryEnhancerAndContext) {
        return buildConditions(createJoinCondition(managedType), queryProperties, queryEnhancerAndContext);
    }

    /**
     * Creates the query with configured conditions.
     *
     * @return
     */
    public static <E extends AbstractEntity<?>> ICompleted<E> createQuery(final Class<E> managedType, final List<QueryProperty> queryProperties) {
        return buildConditions(createJoinCondition(managedType), queryProperties, Optional.empty());
    }

    /**
     * Creates the aggregation query that groups by distribution properties and aggregates by aggregation properties.
     *
     * @param managedType
     * @param queryProperties
     * @param distributionProperties
     * @param aggregationProperties
     * @return
     */
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
            yieldedQuery = yieldedQuery == null ? yield(genClass, yieldProperty, baseQuery) : yield(genClass, yieldProperty, yieldedQuery);
        }
        if (yieldedQuery == null) {
            throw new IllegalStateException("The query was compound incorrectly!");
        }

        return yieldedQuery;
    }

    /**
     * Groups the given query by specified property.
     *
     * @param proeprtyName
     * @param query
     * @return
     */
    private static <E extends AbstractEntity<?>> ICompleted<E> groupBy(final String distribution, final ICompleted<E> query) {
        return query.groupBy().prop(distribution.isEmpty() ? ALIAS : ALIAS + "." + distribution);
    }

    /**
     * Groups the given query by specified property.
     *
     * @param proeprtyName
     * @param query
     * @return
     */
    //TODO Later the genClass property must be removed. This is an interim solution that allows to add .amount prefix to the money properties.
    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> yield(final Class<E> genClass, final Map.Entry<String, String> yield, final ICompleted<E> query) {
        //TODO this code must be removed as a interim solution
        String aliasValue = yield.getValue();
        if (!aliasValue.isEmpty() && Money.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(genClass, aliasValue))) {
            aliasValue += ".amount";
        }
        //remove code above
        return query.yield().prop(yield.getKey().isEmpty() ? ALIAS : ALIAS + "." + yield.getKey()).as(aliasValue);
    }

    /**
     * Groups the given query by specified property.
     *
     * @param proeprtyName
     * @param query
     * @return
     */
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

    /**
     * Removes ".key" part from propertyName.
     *
     * @param propertyName
     * @return
     */
    public static String getPropertyNameWithoutKeyPart(final String propertyName) {
        return replaceLast(propertyName, ".key", "");
    }

    private static String replaceLast(final String s, final String what, final String byWhat) {
        return s.endsWith(what) ? s.substring(0, s.lastIndexOf(what)) : s;
    }
}