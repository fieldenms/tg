package ua.com.fielden.platform.swing.review;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

public class DynamicProperty<T extends AbstractEntity<?>> {
    //Properties that can be modified dynamically.
    private Object criteriaValue;
    private String title;
    private String desc;
    private Boolean not;
    private Boolean exclusive, all, orNull;
    private DateRangePrefixEnum datePrefix;
    private MnemonicEnum dateMnemonic;
    private Boolean andBefore;

    private final DynamicEntityQueryCriteria<T, ? extends IEntityDao<T>> criteria;
    private final Logger logger;

    //Those properties are defined by dynamic criteria or according to the property's annotations.
    private final boolean isUpperCase;
    private final boolean critOnly;
    private final boolean single;
    private final String fullName;
    private final String actualPropertyName;
    private final Class<?> type;
    private final Boolean isWithinCollectionalHierarchy;

    /**
     * Copy constructor.
     *
     * @param dynamicProperty
     */
    public DynamicProperty(final DynamicEntityQueryCriteria<T, ? extends IEntityDao<T>> criteria, final DynamicProperty<T> dynamicProperty) {
	this(criteria, dynamicProperty.getActualPropertyName());

	setTitle(dynamicProperty.getTitle());
	setDesc(dynamicProperty.getDesc());
	setValue(dynamicProperty.getValue());
	setNot(dynamicProperty.getNot());
	setExclusive(dynamicProperty.getExclusive());
	setDatePrefix(dynamicProperty.getDatePrefix());
	setDateMnemonic(dynamicProperty.getDateMnemonic());
	setAndBefore(dynamicProperty.getAndBefore());
	setAll(dynamicProperty.getAll());
	setOrNull(dynamicProperty.getOrNull());
    }

    /**
     * Returns the name of "keyMember" which defines "collectivity" for "collectionElementType".
     *
     * @param collectionElementType
     * @param collectionOwnerType
     * @return
     */
    public static String getNameOfCollectionController(final Class collectionElementType, final Class collectionOwnerType) {
	final List<Field> keyFields = Finder.getKeyMembers(collectionElementType);
	for (final Field field : keyFields) {
	    if (field.getType().equals(collectionOwnerType)) {
		return field.getName();
	    }
	}
	throw new RuntimeException("There is no key member of type [" + collectionOwnerType + "] in type [" + collectionElementType + "].");
    }

    public DynamicProperty(final DynamicEntityQueryCriteria<T, ? extends IEntityDao<T>> criteria, final String propertyName) {
	this.criteria = criteria;

	//Initialising the property analyser and determining whether it is crit-only and whether property name is correct.
	final DynamicPropertyAnalyser analyser = new DynamicPropertyAnalyser(criteria.getEntityClass(), propertyName, criteria.getCriteriaFilter());

	if (analyser.getPropertyFieldAnnotation(CritOnly.class) != null && propertyName.contains(".")) {
	    throw new IllegalStateException("The crit only " + propertyName + " property from " + criteria.getEntityClass() + " class must be first level property");
	}

	//initiating logger
	this.logger = Logger.getLogger(this.getClass());

	actualPropertyName = propertyName;
	// initial value for new property must be null.
	criteriaValue = null;
	// determining full name (i. e. dot notation of the property.

	fullName = analyser.getCriteriaFullName();

	type = analyser.getPropertyType();

	final Pair<Class<? extends AbstractEntity>, Class<? extends AbstractEntity>> collectionalTypes = analyser.getCollectionContainerAndItsParentType();
	isWithinCollectionalHierarchy = collectionalTypes != null;

	// determining the title and description of the new property.
	final Pair<String, String> titleAndDesc = analyser.getTitleAndDesc();
	this.title = TitlesDescsGetter.removeHtmlTag(titleAndDesc.getKey());
	this.desc = TitlesDescsGetter.removeHtmlTag(titleAndDesc.getValue());

	// Initializing boolean properties needed for creating property editors or query.
	final CritOnly critAnnotation = analyser.getPropertyFieldAnnotation(CritOnly.class);
	this.critOnly = critAnnotation != null;

	if (isCritOnly() && critAnnotation.value() == Type.SINGLE) {
	    this.single = true;
	    this.criteriaValue = criteria.getDummyEntity();
	} else {
	    this.single = false;
	}

	this.isUpperCase = analyser.getCriteriaAnnotation(UpperCase.class) != null;
    }

    public Boolean getNot() {
	return not;
    }

    public void setNot(final Boolean not) {
	this.not = not;
    }

    public Boolean getExclusive() {
	return exclusive;
    }

    public void setExclusive(final Boolean exclusive) {
	this.exclusive = exclusive;
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

    public Boolean getAll() {
	return all;
    }

    public void setAll(final Boolean all) {
	this.all = all;
    }

    public Boolean getOrNull() {
	return orNull;
    }

    public void setOrNull(final Boolean orNull) {
	this.orNull = orNull;
    }

    /**
     * Returns true if this property belongs to some collection hierarchy. Method {@link #getCollectionContainerType()} should return the high level collection type.
     *
     * @return
     */
    public boolean isWithinCollectionalHierarchy() {
	return isWithinCollectionalHierarchy;
    }

    public String getActualPropertyName() {
	return actualPropertyName;
    }

    public String getFullName() {
	return fullName;
    }

    public Class<?> getType() {
	return type;
    }

    public Object getCriteriaValue() {
	return criteriaValue;
    }

    public Object getValue() {
	if (isSingle()) {
	    try {
		return Finder.findFieldValueByName(criteriaValue, getActualPropertyName() + (isEntityProperty() ? ".id" : ""));
	    } catch (final Exception e) {
		e.printStackTrace();
		return null;
	    }
	}
	if (String.class.isAssignableFrom(getType()) && isCritOnly()) {
	    throw new IllegalStateException("String " + getActualPropertyName() + " preoprty from " + criteria.getEntityClass()
		    + " can not be annotated with CritOnly(Type.SINGLE) annotation");
	}
	return criteriaValue;
    }

    public void setValue(final Object value) {
	final Class<Boolean> boolType = boolean.class;

	if (isEntityProperty()) {
	    if (isSingle()) {
		if (value != null) {
		    setSingleEntityProperty((Long) value);
		}
	    } else if (value != null && Collection.class.isAssignableFrom(value.getClass())) {
		this.criteriaValue = value;
	    } else if (value != null) {
		new IllegalArgumentException("The value has " + value == null ? "null" : value.getClass().getSimpleName() + " type and the property type is "
			+ getType().getSimpleName() + ". Please correct type of the property and it's value");
	    }
	} else if ((value == null && isRangeProperty()) || (value != null && EntityUtils.isRangeType(value.getClass()))) {
	    if (isSingle()) {
		((AbstractEntity) criteriaValue).set(getActualPropertyName(), value);
	    } else {
		this.criteriaValue = value;
	    }
	} else if (String.class.isAssignableFrom(getType()) && value != null && String.class.isAssignableFrom(value.getClass())) {
	    if (isSingle()) {
		((AbstractEntity) criteriaValue).set(getActualPropertyName(), value);
	    } else if (isCritOnly()) {
		throw new IllegalStateException("String " + getActualPropertyName() + " preoprty from " + criteria.getEntityClass()
			+ " can not be annotated with CritOnly(Type.RANGE) annotation");
	    } else {
		criteriaValue = value;
	    }

	} else if (boolType.isAssignableFrom(getType()) && value != null && (boolType.isAssignableFrom(value.getClass()) || Boolean.class.isAssignableFrom(value.getClass()))) {
	    if (isSingle()) {
		((AbstractEntity) criteriaValue).set(getActualPropertyName(), value);
	    } else {
		criteriaValue = value;
	    }
	} else if (value != null) {
	    throw new IllegalArgumentException("The value has " + value.getClass().getSimpleName() + " type and the property type is " + getType().getSimpleName()
		    + ". Please correct type of the property and it's value");
	}

    }

    /**
     * Set the single entity property to the entity specified with id.
     *
     * @param id
     */
    private void setSingleEntityProperty(final Long id) {
	new SwingWorker<AbstractEntity<?>, Void>() {

	    @Override
	    protected AbstractEntity<?> doInBackground() throws Exception {
		return criteria.getDaoFactory().newDao((Class<AbstractEntity<?>>) getType()).findById(id);
	    }

	    @Override
	    protected void done() {
		try {
		    ((AbstractEntity<?>) criteriaValue).set(getActualPropertyName(), get());
		} catch (final InterruptedException e) {
		    logger.error("The thread was interrupted", e);
		    e.printStackTrace();
		} catch (final ExecutionException e) {
		    logger.error("Execution error", e);
		    e.printStackTrace();
		} catch (final Exception e) {
		    logger.error("couldn't set property " + getActualPropertyName() + " for " + getType() + " insance. Value: " + id.toString(), e);
		    e.printStackTrace();
		}

	    }

	}.execute();
    }

    public void setTitle(final String title) {
	this.title = title;
    }

    public void setDesc(final String desc) {
	this.desc = desc;
    }

    public String getDesc() {
	return desc;
    }

    public String getTitle() {
	return title;
    }

    public boolean isUpperCase() {
	return isUpperCase;
    }

    public boolean isCritOnly() {
	return critOnly;
    }

    public boolean isSingle() {
	return single;
    }

    /**
     * Returns true if either {@link Number}, {@link Date} or {@link Money} is assignable from type of this property.
     *
     * @return
     */
    public boolean isRangeProperty() {
	return EntityUtils.isRangeType(getType());
    }

    /**
     * Returns true if boolean is assignable from type of this property.
     *
     * @return
     */
    public boolean isBoolProperty() {
	return EntityUtils.isBoolean(getType());
    }

    public boolean isDateProperty() {
	return EntityUtils.isDate(getType());
    }

    /**
     * Returns true if {@link String} is assignable from type of this property.
     *
     * @return
     */
    public boolean isStringProperty() {
	return EntityUtils.isString(getType());
    }

    /**
     * Returns true if {@link AbstractEntity} is assignable from type of this property.
     *
     * @return
     */
    public boolean isEntityProperty() {
	return EntityUtils.isEntityType(getType());
    }

    @Override
    public String toString() {
	return actualPropertyName.toString() + " of " + type.toString() + " type";
    }

    public DynamicProperty<T> copy(final DynamicEntityQueryCriteria<T, ? extends IEntityDao<T>> criteria, final DynamicProperty<T> property) {
	return new DynamicProperty<T>(criteria, property);
    }

    public Boolean getAndBefore() {
	return andBefore;
    }

    public void setAndBefore(final Boolean andBefore) {
	this.andBefore = andBefore;
    }
}