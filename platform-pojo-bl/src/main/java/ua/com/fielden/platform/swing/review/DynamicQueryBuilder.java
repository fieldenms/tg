package ua.com.fielden.platform.swing.review;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.MiscUtilities;
import ua.com.fielden.platform.utils.Pair;
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
     * This is a class which represents high-level abstraction for criterion in dynamic criteria.
     * <br><br>
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
	private final Boolean inNestedCollections;

	public QueryProperty(final Class<?> entityClass, final String propertyName) {
	    this.entityClass = entityClass;
	    final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(entityClass, propertyName);
	    this.propertyName = propertyName;
	    if (!isSupported(analyser.getPropertyType())) {
		throw new UnsupportedTypeException(analyser.getPropertyType());
	    }
	    this.type = analyser.getPropertyType();

	    final Pair<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> collectionalTypes = analyser.getCollectionContainerAndItsParentType();
	    final String propertyNameWithinCollectionalHierarchy;
	    if (collectionalTypes != null) {
		this.collectionContainerType = collectionalTypes.getKey();
		this.collectionContainerParentType = collectionalTypes.getValue();
		propertyNameWithinCollectionalHierarchy = analyser.getNamesWithinCollectionalHierarchy().getKey();
		if (StringUtils.isEmpty(propertyNameWithinCollectionalHierarchy)) {
		    throw new IllegalArgumentException("The property [" + this.propertyName + "] is a collection itself. It could not be used for quering.");
		}
		this.propertyNameOfCollectionParent = analyser.getNamesWithinCollectionalHierarchy().getValue();
		this.collectionNameInItsParentTypeContext = analyser.getCollectionNameInItsParentTypeContext();
		this.inNestedCollections = analyser.isInNestedCollections();
		if (this.isInNestedCollections()) {
		    throw new IllegalArgumentException("Properties in nested collections are not supported yet. Please remove property [" + this.propertyName + "] from criteria.");
		}
	    } else {
		this.collectionContainerType = null;
		this.collectionContainerParentType = null;
		propertyNameWithinCollectionalHierarchy = null;
		this.propertyNameOfCollectionParent = null;
		this.collectionNameInItsParentTypeContext = null;
		this.inNestedCollections = null;
	    }
	    this.conditionBuildingName = isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL() ? propertyNameWithinCollectionalHierarchy : ALIAS + "." + analyser.getCriteriaFullName();

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
	    return EntityUtils.equalsEx(value, getEmptyValue(type, single));
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
	 * Determines whether property should be ignored during query composition,
	 * which means that 1) it is crit-only property; 2) it is empty and has not "orNull" condition assigned.
	 *
	 * @return
	 */
	public boolean shouldBeIgnored() {
	    return isCritOnly() || isEmpty() && !Boolean.TRUE.equals(orNull);
	}

	/**
	 * Returns <code>true</code> if this property belongs to some collection hierarchy. Method {@link #getCollectionContainerType()} should return the high level collection type.
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
	public Boolean isInNestedCollections() {
	    return inNestedCollections;
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
	 * @param all -- <code>true</code> to add to ALL properties, <code>false</code> -- to add to ANY properties, <code>null</code> to add to FILTERING properties.
	 */
	public void add(final QueryProperty property) {
	    if (nameOfCollectionController == null) {
		nameOfCollectionController = getNameOfCollectionController(property.getCollectionContainerParentType(), property.getCollectionNameInItsParentTypeContext());
	    }
	    if (propertyNameOfCollectionParent == null) {
		propertyNameOfCollectionParent = property.getPropertyNameOfCollectionParent();
	    }
	    final Calculated calcAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, property.getEntityClass(), property.getPropertyName());
	    final Boolean all = calcAnnotation == null ? null : calcAnnotation.attribute().equals(CalculatedPropertyAttribute.ALL) ? Boolean.TRUE : calcAnnotation.attribute().equals(CalculatedPropertyAttribute.ANY) ? Boolean.FALSE : null;
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
    private static <ET extends AbstractEntity<?>> ICompleted<ET> buildConditions(final IJoin<ET> query, final List<QueryProperty> properties) {
	final IWhere1<ET> whereAtGroup1 = query.where().begin();
	ICompoundCondition1<ET> compoundConditionAtGroup1 = null;

	// create empty map consisting of [collectionType => (ANY properties, ALL properties)] entries, which forms exactly one entry for one collectional hierarchy
	final Map<Class<? extends AbstractEntity<?>>, CollectionProperties> collectionalProperties = new LinkedHashMap<Class<? extends AbstractEntity<?>>, CollectionProperties>();

	// traverse all properties to enhance resulting query
	for (final QueryProperty property : properties) {
	    if (!property.shouldBeIgnored()) {
		if (property.isWithinCollectionalHierarchyOrOutsideCollectionWithANYorALL()) { // the property is in collection hierarchy. So, separate collection sub-model (EXISTS or NOT_EXISTS) should be enhanced by this property's criteria.
		    final Class<? extends AbstractEntity<?>> ccType = property.getCollectionContainerType();
		    if (!collectionalProperties.containsKey(ccType)) {
			collectionalProperties.put(ccType, new CollectionProperties(ccType));
		    }
		    collectionalProperties.get(ccType).add(property);
		} else { // main query should be enhanced in case of simple property
		    compoundConditionAtGroup1 = buildCondition(getWhereAtGroup1(compoundConditionAtGroup1, whereAtGroup1), property, false);
		}
	    }
	}
	// enhance main model with collectional hierarchies models
	for (final CollectionProperties collectionProperties : collectionalProperties.values()) {
	    if (collectionProperties.hasAggregatedCondition()) {
		compoundConditionAtGroup1 = buildCollection(getWhereAtGroup1(compoundConditionAtGroup1, whereAtGroup1), collectionProperties, ALIAS);
	    } else {
		// TODO
		logger.warn("There are no aggregated conditions for collection [" + collectionProperties + "] in type " + collectionProperties + ". All FILTERING conditions (if any) will be disregarded.");
	    }
	}
	return compoundConditionAtGroup1 == null ? query : compoundConditionAtGroup1.end();
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
     * IMPORTANT : please consider that left boundary should be inclusive and right -- exclusive! E.g. CURR YEAR converts to (01.01.2011 00:00; 01.01.2012 00:00) and need
     * to be used as <i>prop(propertyName).<b>ge()</b>.val(from).and().prop(propertyName).<b>lt()</b>.val(to)</i> in terms of Entity Query.
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
     * If <b>ANY</b> condition has been applied -- appropriate <b>EXISTS</b> model for collection will be enhanced (will be concatenated with previous conditions using <b>AND</b>!).<br>
     * If <b>ALL</b> condition has been applied -- appropriate <b>EXISTS</b> (will be concatenated with previous conditions using <b>AND</b>!)
     * and appropriate <b>NOT_EXISTS</b> (<b>NEGATED</b> condition will be concatenated with previous conditions using <b>OR</b>!) models for collection will be enhanced (+<b>NOT_EXISTS</b> without conditions will be created and concatenated using <b>OR</b>!).<br>
     *
     * @param entry -- an entry consisting of [collectionType => (anyProperties, allProperties)] which forms exactly one collectional hierarchy
     * @return
     */
    private static <ET extends AbstractEntity<?>> ICompoundCondition1<ET> buildCollection(final IWhere1<ET> whereAtGroup1, final CollectionProperties collectionProperties, final String alias) {
	// e.g. : "WorkOrder.vehicle.statusChanges.[vehicleKey/status.active]". Then:
	// property.getCollectionContainerType() == VehicleStatusChange.class
	// property.getCollectionContainerParentType() == Vehicle.class
	// property.getCollectionNameInItsParentTypeContext() == statusChanges
	// nameOfCollectionController == "vehicleKey"
	// property.getPropertyNameOfCollectionParent() == "vehicle"
	final Class<? extends AbstractEntity<?>> collectionContainerType = collectionProperties.getCollectionContatinerType();
	final String nameOfCollectionController = collectionProperties.getNameOfCollectionController();
	final String mainModelProperty = collectionProperties.getPropertyNameOfCollectionParent().isEmpty() ? alias : alias + "." + collectionProperties.getPropertyNameOfCollectionParent();

	final IWhere2<ET> collectionBegin = whereAtGroup1.begin();
	ICompoundCondition2<ET> compoundConditionAtGroup2 = null;
	// enhance collection by ANY part
	if (!collectionProperties.getAnyProperties().isEmpty()) {
	    final Iterator<QueryProperty> anyIter = collectionProperties.getAnyProperties().iterator();
	    ICompoundCondition1<? extends AbstractEntity<?>> anyExists_withDirectConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin(), anyIter.next(), false);
	    while (anyIter.hasNext()) { // enhance EXISTS model with appropriate condition
		anyExists_withDirectConditions = buildCondition(anyExists_withDirectConditions.and(), anyIter.next(), false);
	    }
	    // enhance main model by EXISTS model relevant to ANY properties in collectional hierarchy
	    compoundConditionAtGroup2 = getWhereAtGroup2(compoundConditionAtGroup2, collectionBegin).begin()//
		    .exists(anyExists_withDirectConditions.end().model())//
		    .end();
	}
	// enhance collection by ALL part
	if (!collectionProperties.getAllProperties().isEmpty()) {
	    final Iterator<QueryProperty> allIter = collectionProperties.getAllProperties().iterator();
	    final EntityResultQueryModel<?> allNotExists_withNoConditions = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).model();
	    final QueryProperty firstProperty = allIter.next();
	    ICompoundCondition1<? extends AbstractEntity<?>> allExists_withDirectConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin(), firstProperty, false);
	    ICompoundCondition1<? extends AbstractEntity<?>> allNotExists_withNegatedConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty, collectionProperties.getFilteringProperties()).and().begin(), firstProperty, true);
	    while (allIter.hasNext()) { // enhance EXISTS / NOT_EXISTS model with appropriate direct / negated condition
		final QueryProperty nextProperty = allIter.next();
		allExists_withDirectConditions = buildCondition(allExists_withDirectConditions.and(), nextProperty, false);
		allNotExists_withNegatedConditions = buildCondition(allNotExists_withNegatedConditions.or(), nextProperty, true);
	    }
	    // enhance main model by EXISTS / NOT_EXISTS models relevant to ALL properties in collectional hierarchy
	    compoundConditionAtGroup2 = getWhereAtGroup2(compoundConditionAtGroup2, collectionBegin).begin()//
		    .notExists(allNotExists_withNoConditions)// entities with empty collection should be included!
		    .or()//
		    .exists(allExists_withDirectConditions.end().model()).and().notExists(allNotExists_withNegatedConditions.end().model())//
		    .end();
	}
	if (compoundConditionAtGroup2 == null) {
	    throw new RuntimeException("Collection of type " + collectionContainerType + " did not alter query.");
	}
	return compoundConditionAtGroup2.end();
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
	ICompoundCondition1<ET> subModel = buildCondition(submodelThroghLinkProperty.and().begin(), filteringProperties.get(0), false); // enhance sub-model with first FILTERING property
	for (int i = 1; i < filteringProperties.size(); i++) {
	    subModel = buildCondition(subModel.and(), filteringProperties.get(i), false); // enhance sub-model with rest FILTERING properties
	}
	return subModel.end();
    }

    /**
     * Helper method to form IWhereAtGroup1 instance from "compoundConditionAtGroup1" and "whereAtGroup1".
     *
     * @param compoundConditionAtGroup1
     * @param Equery
     * @return
     */
    private static <ET extends AbstractEntity<?>> IWhere1<ET> getWhereAtGroup1(final ICompoundCondition1<ET> compoundConditionAtGroup1, final IWhere1<ET> whereAtGroup1) {
	return compoundConditionAtGroup1 == null ? whereAtGroup1 : compoundConditionAtGroup1.and();
    }

    /**
     * Helper method to form IWhereAtGroup2 instance from "compoundConditionAtGroup2" and "whereAtGroup2".
     *
     * @param compoundConditionAtGroup1
     * @param Equery
     * @return
     */
    private static <ET extends AbstractEntity<?>> IWhere2<ET> getWhereAtGroup2(final ICompoundCondition2<ET> compoundConditionAtGroup2, final IWhere2<ET> whereAtGroup2) {
	return compoundConditionAtGroup2 == null ? whereAtGroup2 : compoundConditionAtGroup2.and();
    }

    /**
     * Enhances "where" with concrete property condition defined by "key" parameter taking into account condition negation and <b>null</b> values treatment.
     *
     * @param where
     * @param key
     * @param isNegated -- indicates whether appropriate condition should be negated
     * @return
     */
    private static <ET extends AbstractEntity<?>> ICompoundCondition1<ET> buildCondition(final IWhere1<ET> where, final QueryProperty property, final boolean isNegated) {
	final boolean orNull = Boolean.TRUE.equals(property.getOrNull());
	final boolean not = Boolean.TRUE.equals(property.getNot());
	final String propertyName = property.getConditionBuildingName();
	// IMPORTANT : in order not to make extra joins properties like "alias.key", "alias.property1.key" and so on will be enhanced by
	// conditions like "alias is [not] null", "alias.property1 is [not] null" and so on (respectively).
	final IComparisonOperator2<ET> sc = where.begin().prop(getPropertyNameWithoutKeyPart(propertyName));
	// indicates whether a condition should be negated
	final boolean negate = not ^ isNegated;
	if (property.isEmpty()) {
	    if (!orNull) {
		throw new RuntimeException("Should have at least NULL condition.");
	    }
	    return negate ? sc.isNotNull().end() : sc.isNull().end();
	} else {
	    // indicates whether nulls should be considered in a query
	    final boolean considerNulls = negate ^ orNull;
	    final IWhere2<ET> whereAtGroup2 = considerNulls ? sc.isNull().or() : sc.isNotNull().and();
	    return buildAtomicCondition(property, negate ? whereAtGroup2.notBegin() : whereAtGroup2.begin()).end();
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
    private static <ET extends AbstractEntity<?>> ICompoundCondition2<ET> buildAtomicCondition(final QueryProperty property, final IWhere3<ET> conditionGroup) {
	final String propertyName = property.getConditionBuildingName();

	if (EntityUtils.isRangeType(property.getType())) {
	    if (EntityUtils.isDate(property.getType()) && property.getDatePrefix() != null && property.getDateMnemonic() != null) {
		// left boundary should be inclusive and right -- exclusive!
		final Pair<Date, Date> fromAndTo = getDateValuesFrom(property.getDatePrefix(), property.getDateMnemonic(), property.getAndBefore());
		return conditionGroup.prop(propertyName).ge().iVal(fromAndTo.getKey()).and().prop(propertyName).lt().iVal(fromAndTo.getValue()).end();
	    } else {
		final IComparisonOperator3<ET> scag = conditionGroup.prop(propertyName);
		final IComparisonOperator3<ET> scag2 = Boolean.TRUE.equals(property.getExclusive()) ? //
			/*      */scag.gt().iVal(property.getValue()).and().prop(propertyName) // exclusive
			: scag.ge().iVal(property.getValue()).and().prop(propertyName); // inclusive
			return Boolean.TRUE.equals(property.getExclusive2()) ? //
				/*      */scag2.lt().iVal(property.getValue2()).end() // exclusive
				: scag2.le().iVal(property.getValue2()).end(); // inclusive
	    }
	} else if (EntityUtils.isBoolean(property.getType())) {
	    final boolean is = (Boolean) property.getValue();
	    final boolean isNot = (Boolean) property.getValue2();
	    return is && !isNot ? conditionGroup.prop(propertyName).eq().val(true).end() : !is && isNot ? conditionGroup.prop(propertyName).eq().val(false).end() : null;
	} else if (EntityUtils.isString(property.getType())) {
	    return conditionGroup.prop(propertyName).iLike().anyOfValues(prepare((String) property.getValue())).end();
	} else if (EntityUtils.isEntityType(property.getType())) {
	    return conditionGroup.prop(propertyName).iLike().anyOfValues(prepare((List<String>) property.getValue())).end();
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
    public static String createConditionProperty(final String property){
	return property.isEmpty() ? ALIAS : ALIAS + "." + property;
    }

    /**
     * Creates the query with configured conditions.
     *
     * @return
     */
    public static <E extends AbstractEntity<?>> ICompleted<E> createQuery(final Class<E> managedType, final List<QueryProperty> queryProperties){
	return buildConditions(createJoinCondition(managedType), queryProperties);
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
    public static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> createAggregationQuery(final EntityResultQueryModel<E> sourceQueryModel, final List<String> distributionProperties, final Map<String, String> yieldProperties){

	ICompleted<E> baseQuery = select(sourceQueryModel).as(ALIAS);
	for (final String groupProperty : distributionProperties) {
	    baseQuery = groupBy(groupProperty, baseQuery);
	}
	ISubsequentCompletedAndYielded<E> yieldedQuery = null;
	for (final Map.Entry<String, String> yieldProperty : yieldProperties.entrySet()) {
	    yieldedQuery = yieldedQuery == null ? yield(yieldProperty, baseQuery) : yield(yieldProperty, yieldedQuery);
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
    private static <E extends AbstractEntity<?>> ICompleted<E> groupBy(final String distribution, final ICompleted<E> query){
	return query.groupBy().prop(distribution.isEmpty() ? ALIAS : ALIAS + "." + distribution);
    }

    /**
     * Groups the given query by specified property.
     *
     * @param proeprtyName
     * @param query
     * @return
     */
    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> yield(final Map.Entry<String, String> yield, final ICompleted<E> query){
	return query.yield().prop(yield.getKey().isEmpty() ? ALIAS : ALIAS + "." + yield.getKey()).as(yield.getValue());
    }

    /**
     * Groups the given query by specified property.
     *
     * @param proeprtyName
     * @param query
     * @return
     */
    private static <E extends AbstractEntity<?>> ISubsequentCompletedAndYielded<E> yield(final Map.Entry<String, String> yield, final ISubsequentCompletedAndYielded<E> query){
	return query.yield().prop(yield.getKey().isEmpty() ? ALIAS : ALIAS + "." + yield.getKey()).as(yield.getValue());
    }

    /**
     * Removes ".key" part from propertyName.
     *
     * @param propertyName
     * @return
     */
    private static String getPropertyNameWithoutKeyPart(final String propertyName) {
	return replaceLast(propertyName, ".key", "");
    }

    private static String replaceLast(final String s, final String what, final String byWhat) {
	final int i = s.lastIndexOf(what);
	return i >= 0 ? s.substring(0, i) : s;
    }
}