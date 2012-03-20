package ua.com.fielden.platform.swing.review;

import static ua.com.fielden.platform.equery.equery.select;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;
import ua.com.fielden.platform.equery.interfaces.IOthers;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundConditionAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup1;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.utils.EntityUtils;
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
	private Boolean all = null;

	private final String propertyName, conditionBuildingName;
	private final boolean critOnly, single;
	private final Class<?> type;
	/** The type of collection which contain this property. If this property is not in collection hierarchy it should be null. */
	private final Class<? extends AbstractEntity> collectionContainerType, collectionContainerParentType;
	private final String propertyNameOfCollectionParent;
	private final Boolean inNestedCollections;

	public QueryProperty(final Class<?> entityClass, final String propertyName, final String alias) {
	    if (StringUtils.isEmpty(alias)) {
		throw new IllegalArgumentException("The alias for dynamic query should not be empty.");
	    }
	    final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(entityClass, propertyName, null);
	    this.propertyName = propertyName;
	    if (!isSupported(analyser.getPropertyType())) {
		throw new UnsupportedTypeException(analyser.getPropertyType());
	    }
	    this.type = analyser.getPropertyType();

	    final Pair<Class<? extends AbstractEntity>, Class<? extends AbstractEntity>> collectionalTypes = analyser.getCollectionContainerAndItsParentType();
	    final String propertyNameWithinCollectionalHierarchy;
	    if (collectionalTypes != null) {
		this.collectionContainerType = collectionalTypes.getKey();
		this.collectionContainerParentType = collectionalTypes.getValue();
		propertyNameWithinCollectionalHierarchy = analyser.getNamesWithinCollectionalHierarchy().getKey();
		if (StringUtils.isEmpty(propertyNameWithinCollectionalHierarchy)) {
		    throw new IllegalArgumentException("The property [" + this.propertyName + "] is a collection itself. It could not be used for quering.");
		}
		this.propertyNameOfCollectionParent = analyser.getNamesWithinCollectionalHierarchy().getValue();
		this.inNestedCollections = analyser.isInNestedCollections();
		if (this.isInNestedCollections()) {
		    throw new IllegalArgumentException("Properties in nested collections are not supported yet. Please remove property [" + this.propertyName + "] from criteria.");
		}
	    } else {
		this.collectionContainerType = null;
		this.collectionContainerParentType = null;
		propertyNameWithinCollectionalHierarchy = null;
		this.propertyNameOfCollectionParent = null;
		this.inNestedCollections = null;
	    }
	    this.conditionBuildingName = isWithinCollectionalHierarchy() ? propertyNameWithinCollectionalHierarchy : (alias + "." + analyser.getCriteriaFullName());

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

	public Boolean getAll() {
	    return all;
	}

	public void setAll(final Boolean all) {
	    this.all = all;
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
		return is && isNot || (!is && !isNot); // both true and both false will be indicated as default
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
	    return isCritOnly() || (isEmpty() && !Boolean.TRUE.equals(orNull));
	}

	/**
	 * Returns <code>true</code> if this property belongs to some collection hierarchy. Method {@link #getCollectionContainerType()} should return the high level collection type.
	 *
	 * @return
	 */
	public boolean isWithinCollectionalHierarchy() {
	    return getCollectionContainerType() != null;
	}

	/**
	 * The type of collection which contain this property. If this property is not in collection hierarchy it should be <code>null</code>.
	 *
	 * @return
	 */
	public Class<? extends AbstractEntity> getCollectionContainerType() {
	    return collectionContainerType;
	}

	/**
	 * The type of the parent of collection which contain this property. If this property is not in collection hierarchy it should be <code>null</code>.
	 *
	 * @return
	 */
	public Class<? extends AbstractEntity> getCollectionContainerParentType() {
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
    }

    /**
     * Enhances current query by property conditions (property could form part of "exists"/"not_exists" statements for collections or part of simple "where" statement).
     *
     * @return
     */
    public static ICompleted buildConditions(final IJoin query, final List<QueryProperty> properties, final String alias) {
	final IWhereAtGroup1 whereAtGroup1 = query.where().begin();
	ICompoundConditionAtGroup1 compoundConditionAtGroup1 = null;

	// create empty map consisting of [collectionType => (ANY properties, ALL properties)] entries, which forms exactly one entry for one collectional hierarchy
	final Map<Class<? extends AbstractEntity>, Pair<List<QueryProperty>, List<QueryProperty>>> collectionalProperties = new LinkedHashMap<Class<? extends AbstractEntity>, Pair<List<QueryProperty>, List<QueryProperty>>>();

	// traverse all properties to enhance resulting query
	for (final QueryProperty property : properties) {
	    if (!property.shouldBeIgnored()) {
		if (property.isWithinCollectionalHierarchy()) { // the property is in collection hierarchy. So, separate collection sub-model (EXISTS or NOT_EXISTS) should be enhanced by this property's criteria.
		    final Class<? extends AbstractEntity> ccType = property.getCollectionContainerType();
		    if (!collectionalProperties.containsKey(ccType)) {
			collectionalProperties.put(ccType, new Pair<List<QueryProperty>, List<QueryProperty>>(new ArrayList<QueryProperty>(), new ArrayList<QueryProperty>()));
		    }
		    if (Boolean.TRUE.equals(property.getAll())) {
			collectionalProperties.get(ccType).getValue().add(property); // ALL properties list
		    } else {
			collectionalProperties.get(ccType).getKey().add(property); // ANY properties list
		    }
		} else { // main query should be enhanced in case of simple property
		    compoundConditionAtGroup1 = buildCondition(getWhereAtGroup1(compoundConditionAtGroup1, whereAtGroup1), property, false);
		}
	    }
	}
	// enhance main model with collectional hierarchies models
	for (final Entry<Class<? extends AbstractEntity>, Pair<List<QueryProperty>, List<QueryProperty>>> entry : collectionalProperties.entrySet()) {
	    compoundConditionAtGroup1 = buildCollection(getWhereAtGroup1(compoundConditionAtGroup1, whereAtGroup1), entry, alias);
	}
	return compoundConditionAtGroup1 == null ? query : compoundConditionAtGroup1.end();
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
    private static ICompoundConditionAtGroup1 buildCollection(final IWhereAtGroup1 whereAtGroup1, final Entry<Class<? extends AbstractEntity>, Pair<List<QueryProperty>, List<QueryProperty>>> entry, final String alias) {
	// e.g. : "WorkOrder.vehicle.statusChanges.[vehicleKey/status.active]". Then:
	// property.getCollectionContainerType() == VehicleStatusChange.class
	// property.getCollectionContainerParentType() == Vehicle.class
	// nameOfCollectionController == "vehicleKey"
	// property.getPropertyNameOfCollectionParent() == "vehicle"
	final Class<? extends AbstractEntity> collectionContainerType = entry.getKey();
	final List<QueryProperty> anyProperties = entry.getValue().getKey();
	final List<QueryProperty> allProperties = entry.getValue().getValue();

	final List<QueryProperty> any_and_all_properties = new ArrayList<QueryProperty>(anyProperties);
	any_and_all_properties.addAll(allProperties);
	if (any_and_all_properties.isEmpty()) {
	    throw new RuntimeException("Collection of type " + collectionContainerType + " ANY+ALL properties should not be empty.");
	}
	final QueryProperty p = any_and_all_properties.get(0);
	final String nameOfCollectionController = DynamicProperty.getNameOfCollectionController(collectionContainerType, p.getCollectionContainerParentType());
	final String mainModelProperty = p.getPropertyNameOfCollectionParent().isEmpty() ? alias : (alias + "." + p.getPropertyNameOfCollectionParent());

	final IOthers.IWhereAtGroup2 collectionBegin = whereAtGroup1.begin();
	IOthers.ICompoundConditionAtGroup2 compoundConditionAtGroup2 = null;
	// enhance collection by ANY part
	if (!anyProperties.isEmpty()) {
	    final Iterator<QueryProperty> anyIter = anyProperties.iterator();
	    ICompoundConditionAtGroup1 anyExists_withDirectConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty).and().begin(), anyIter.next(), false);
	    while (anyIter.hasNext()) { // enhance EXISTS model with appropriate condition
		anyExists_withDirectConditions = buildCondition(anyExists_withDirectConditions.and(), anyIter.next(), false);
	    }
	    // enhance main model by EXISTS model relevant to ANY properties in collectional hierarchy
	    compoundConditionAtGroup2 = getWhereAtGroup2(compoundConditionAtGroup2, collectionBegin).begin()//
	    	.exists(anyExists_withDirectConditions.end().model())//
	    	.end();
	}
	// enhance collection by ALL part
	if (!allProperties.isEmpty()) {
	    final Iterator<QueryProperty> allIter = allProperties.iterator();
	    final IQueryModel<?> allNotExists_withNoConditions = createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty).model();
	    final QueryProperty firstProperty = allIter.next();
	    ICompoundConditionAtGroup1 allExists_withDirectConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty).and().begin(), firstProperty, false);
	    ICompoundConditionAtGroup1 allNotExists_withNegatedConditions = buildCondition(createSubmodel(collectionContainerType, nameOfCollectionController, mainModelProperty).and().begin(), firstProperty, true);
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
     * Returns <code>true</code> if the <code>type</code> is supported in dynamic criteria, <code>false</code> otherwise.
     *
     * @param type
     * @return
     */
    private static boolean isSupported(final Class<?> type) {
	return EntityUtils.isEntityType(type) || EntityUtils.isString(type) || EntityUtils.isBoolean(type) || EntityUtils.isRangeType(type);
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
     * Creates submodel for collection.
     *
     * @param collectionContainerType
     * @param nameOfCollectionController
     * @param mainModelProperty
     * @return
     */
    private static ICompoundCondition createSubmodel(final Class<? extends AbstractEntity> collectionContainerType, final String nameOfCollectionController, final String mainModelProperty) {
	return select(collectionContainerType).where().prop(nameOfCollectionController).eq().prop(mainModelProperty);
    }

    /**
     * Helper method to form IWhereAtGroup1 instance from "compoundConditionAtGroup1" and "whereAtGroup1".
     *
     * @param compoundConditionAtGroup1
     * @param Equery
     * @return
     */
    private static IWhereAtGroup1 getWhereAtGroup1(final ICompoundConditionAtGroup1 compoundConditionAtGroup1, final IWhereAtGroup1 whereAtGroup1) {
	return compoundConditionAtGroup1 == null ? whereAtGroup1 : compoundConditionAtGroup1.and();
    }

    /**
     * Helper method to form IWhereAtGroup2 instance from "compoundConditionAtGroup2" and "whereAtGroup2".
     *
     * @param compoundConditionAtGroup1
     * @param Equery
     * @return
     */
    private static IOthers.IWhereAtGroup2 getWhereAtGroup2(final IOthers.ICompoundConditionAtGroup2 compoundConditionAtGroup2, final IOthers.IWhereAtGroup2 whereAtGroup2) {
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
    private static ICompoundConditionAtGroup1 buildCondition(final IWhereAtGroup1 where, final QueryProperty property, final boolean isNegated) {
	final boolean orNull = Boolean.TRUE.equals(property.getOrNull());
	final boolean not = Boolean.TRUE.equals(property.getNot());
	final String propertyName = property.getConditionBuildingName();
	// IMPORTANT : in order not to make extra joins properties like "alias.key", "alias.property1.key" and so on will be enhanced by
	// conditions like "alias is [not] null", "alias.property1 is [not] null" and so on (respectively).
	final IOthers.ISearchConditionAtGroup2 sc = where.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(propertyName));
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
	    final IOthers.IWhereAtGroup2 whereAtGroup2 = considerNulls ? sc.isNull().or() : sc.isNotNull().and();
	    return buildAtomicCondition(property, negate ? whereAtGroup2.notBegin() : whereAtGroup2.begin()).end();
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
	final Date from = (Boolean.TRUE.equals(andBefore)) ? null : du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, datePrefix, dateMnemonic), //
	/*         */to = (Boolean.FALSE.equals(andBefore)) ? null : du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, datePrefix, dateMnemonic);
	// left boundary should be inclusive and right -- exclusive!
	return new Pair<Date, Date>(from, to);
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
    private static IOthers.ICompoundConditionAtGroup2 buildAtomicCondition(final QueryProperty property, final IOthers.IWhereAtGroup3 conditionGroup) {
	final String propertyName = property.getConditionBuildingName();

	if (EntityUtils.isRangeType(property.getType())) {
	    if (EntityUtils.isDate(property.getType()) && property.getDatePrefix() != null && property.getDateMnemonic() != null) {
		// left boundary should be inclusive and right -- exclusive!
		final Pair<Date, Date> fromAndTo = getDateValuesFrom(property.getDatePrefix(), property.getDateMnemonic(), property.getAndBefore());
		return conditionGroup.prop(propertyName).ge().val(fromAndTo.getKey()).and().prop(propertyName).lt().val(fromAndTo.getValue()).end();
	    } else {
		final IOthers.ISearchConditionAtGroup3 scag = conditionGroup.prop(propertyName);
		final IOthers.ISearchConditionAtGroup3 scag2 = (Boolean.TRUE.equals(property.getExclusive())) ? //
		/*      */scag.gt().val(property.getValue()).and().prop(propertyName) // exclusive
			: scag.ge().val(property.getValue()).and().prop(propertyName); // inclusive
		return (Boolean.TRUE.equals(property.getExclusive2())) ? //
		/*      */scag2.lt().val(property.getValue2()).end() // exclusive
			: scag2.le().val(property.getValue2()).end(); // inclusive
	    }
	} else if (EntityUtils.isBoolean(property.getType())) {
	    final boolean is = (Boolean) property.getValue();
	    final boolean isNot = (Boolean) property.getValue2();
	    return (is && !isNot) ? conditionGroup.prop(propertyName).isTrue().end() : (!is && isNot ? conditionGroup.prop(propertyName).isFalse().end() : null);
	} else if (EntityUtils.isString(property.getType())) {
	    return conditionGroup.prop(propertyName).like().val(DynamicEntityQueryCriteria.prepare((String) property.getValue())).end();
	} else if (EntityUtils.isEntityType(property.getType())) {
	    return conditionGroup.prop(propertyName).like().val(DynamicEntityQueryCriteria.prepare((List<String>) property.getValue())).end();
	} else {
	    throw new UnsupportedTypeException(property.getType());
	}
    }
}
