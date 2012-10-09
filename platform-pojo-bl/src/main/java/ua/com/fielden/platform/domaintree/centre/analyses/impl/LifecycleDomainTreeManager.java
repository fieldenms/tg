package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToCategoriesTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToDistributionTickRepresentation;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree manager for lifecycle analyses.
 *
 * @author TG Team
 *
 */
public class LifecycleDomainTreeManager extends AbstractAnalysisDomainTreeManager implements ILifecycleDomainTreeManager {
    private Date from, to;
    private Boolean total;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LifecycleDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new LifecycleDomainTreeRepresentation(serialiser, rootTypes), null, new LifecycleAddToDistributionTickManager(), new LifecycleAddToCategoriesTickManager(), null, null, null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LifecycleDomainTreeManager(final ISerialiser serialiser, final LifecycleDomainTreeRepresentation dtr, final Boolean visible, final LifecycleAddToDistributionTickManager firstTick, final LifecycleAddToCategoriesTickManager secondTick, final Date from, final Date to, final Boolean total) {
	super(serialiser, dtr, visible, firstTick, secondTick);

	this.from = from;
	this.to = to;
	this.total = total;
    }

    @Override
    public ILifecycleAddToDistributionTickManager getFirstTick() {
	return (ILifecycleAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public ILifecycleAddToCategoriesTickManager getSecondTick() {
	return (ILifecycleAddToCategoriesTickManager) super.getSecondTick();
    }

    @Override
    public ILifecycleDomainTreeRepresentation getRepresentation() {
	return (ILifecycleDomainTreeRepresentation) super.getRepresentation();
    }

    public static class LifecycleAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements ILifecycleAddToDistributionTickManager {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public LifecycleAddToDistributionTickManager() {
	}

	@Override
	protected LifecycleAddToDistributionTickRepresentation tr() {
	    return (LifecycleAddToDistributionTickRepresentation) super.tr();
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    final List<String> listOfUsedProperties = getAndInitUsedProperties(root, property);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.clear();
		listOfUsedProperties.add(property);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	}
    }

    public static class LifecycleAddToCategoriesTickManager extends AbstractAnalysisAddToAggregationTickManager implements ILifecycleAddToCategoriesTickManager {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public LifecycleAddToCategoriesTickManager() {
	}

	@Override
	protected LifecycleAddToCategoriesTickRepresentation tr() {
	    return (LifecycleAddToCategoriesTickRepresentation) super.tr();
	}

	@Override
	protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
	    if (isLifecycle(root, property)) {
		removeCheckingAndUsageAndDestroyCategories(root);
		super.insertCheckedProperty(root, property, index);
		addAndCheckAllCategoriesAndUseMainCategories(root, property);
	    } else {
		super.insertCheckedProperty(root, property, index);
	    }
	}

	protected void addAndCheckAllCategoriesAndUseMainCategories(final Class<?> root, final String lifecycleProperty) {
	    final ICategorizer categorizer = categorizer(root, lifecycleProperty);
//	    for (final ICategory category : categorizer.getAllCategories()) {
//		final ICalculatedProperty calcProp = tr().getDtr().parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(DynamicEntityClassLoader.getOriginalType(root), "", LifecycleDomainTreeRepresentation.CATEGORY_PROPERTY_MARKER, category.getName(), category.getDesc(), CalculatedPropertyAttribute.NO_ATTR, "SELF");
//		final String categoryPropertyName = calcProp.pathAndName();
//		tr().getDtr().parentCentreDomainTreeManager().getEnhancer().apply();
//
//
//		tr().getDtr().parentCentreDomainTreeManager().getEnhancer().getCalculatedProperty(root, categoryPropertyName);
//
//
//		check(root, categoryPropertyName, true); // all categories should be checked by default
//		if (categorizer.getMainCategories().contains(category)) {
//		    use(root, categoryPropertyName, true); // only main categories should be used by default
//		}
//	    }
	    for (final ICategory category : categorizer.getAllCategories()) {
		final ICalculatedProperty calcProp = tr().getDtr().parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(DynamicEntityClassLoader.getOriginalType(root), "", LifecycleDomainTreeRepresentation.CATEGORY_PROPERTY_MARKER, category.getName(), category.getDesc(), CalculatedPropertyAttribute.NO_ATTR, "SELF");
		final String categoryPropertyName = calcProp.pathAndName();
		tr().getDtr().parentCentreDomainTreeManager().getEnhancer().getCalculatedProperty(root, categoryPropertyName);
	    }
	    tr().getDtr().parentCentreDomainTreeManager().getEnhancer().apply();
	    final List<String> includedProperties = tr().getDtr().includedProperties(root);
	    final Class<?> managedType = managedType(root);
	    for (final String property : includedProperties) {
		if (!PropertyTypeDeterminator.isDotNotation(property) && LifecycleAddToCategoriesTickRepresentation.isCategoryProperty(managedType, property)) { // categories at this stage are located in root type
		    check(root, property, true); // all categories should be checked by default
//		    if (categorizer.getMainCategories().contains(category)) {
//			use(root, property, true); // only main categories should be used by default
//		    }
		}
	    }
	}

	/**
	 * Removes 'checking' and 'usage' of all properties for this tick. This action includes
	 * unchecking of 'lifecycle' property and all of its category markers and the destroying of all
	 * category markers.
	 *
	 * @param root
	 */
	protected void removeCheckingAndUsageAndDestroyCategories(final Class<?> root) {
	    // uncheck all previously checked property(ies) (and unuse them first).
	    // Note: there should be the only one or zero checked Lifecycle property, and, perhaps, some category markers.
	    while (!checkedPropertiesMutable(root).isEmpty()) {
		final String propertyToUncheck = checkedPropertiesMutable(root).get(0);
		if (isUsed(root, propertyToUncheck)) {
		    use(root, propertyToUncheck, false);
		}
		check(root, propertyToUncheck, false);
	    }
	    removeAllCategories(root);
	}

	protected void removeAllCategories(final Class<?> root) {
	    final List<String> includedProperties = tr().getDtr().includedProperties(root);
	    final Class<?> managedType = managedType(root);
	    for (final String property : includedProperties) {
		if (!PropertyTypeDeterminator.isDotNotation(property) && LifecycleAddToCategoriesTickRepresentation.isCategoryProperty(managedType, property)) { // categories at this stage are located in root type
		    // tr().getDtr().parentCentreDomainTreeManager().getEnhancer().addCalculatedProperty(root, "", LifecycleDomainTreeRepresentation.CATEGORY_PROPERTY_MARKER, "", desc, attribute, originationProperty);
		    tr().getDtr().parentCentreDomainTreeManager().getEnhancer().removeCalculatedProperty(root, property);
		    tr().getDtr().parentCentreDomainTreeManager().getEnhancer().apply();
		}
	    }
	}

	@Override
	protected void removeCheckedProperty(final Class<?> root, final String property) {
	    if (isLifecycle(root, property)) {
		removeCheckingAndUsageAndDestroyCategories(root);
	    } else {
		super.removeCheckedProperty(root, property);
	    }
	}

	@Override
	public List<ICategory> allCategories(final Class<?> root) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public List<ICategory> currentCategories(final Class<?> root) {
	    // TODO Auto-generated method stub
	    return null;
	}
    }

    /**
     * A specific Kryo serialiser for {@link LifecycleDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class LifecycleDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<LifecycleDomainTreeManager> {
	public LifecycleDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LifecycleDomainTreeManager read(final ByteBuffer buffer) {
	    final LifecycleDomainTreeRepresentation dtr = readValue(buffer, LifecycleDomainTreeRepresentation.class);
	    final LifecycleAddToDistributionTickManager firstTick = readValue(buffer, LifecycleAddToDistributionTickManager.class);
	    final LifecycleAddToCategoriesTickManager secondTick = readValue(buffer, LifecycleAddToCategoriesTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    final Date from = readValue(buffer, Date.class);
	    final Date to = readValue(buffer, Date.class);
	    final Boolean total = readValue(buffer, Boolean.class);
	    return new LifecycleDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick, from, to, total);
	}

	@Override
	public void write(final ByteBuffer buffer, final LifecycleDomainTreeManager manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.from);
	    writeValue(buffer, manager.to);
	    writeValue(buffer, manager.total);
	}
    }

    @Override
    public Pair<Class<?>, String> getLifecycleProperty() {
	// here return the first (which should be the only one) lifecycle property in the list of checked properties
	for (final Class<?> root : getRepresentation().rootTypes()) {
	    final List<String> checkedProperties = getSecondTick().checkedProperties(root);
	    for (final String checkedProperty : checkedProperties) {
		if (isLifecycle(root, checkedProperty)) {
		    return key(root, checkedProperty);
		}
	    }
	}
	return null;
    }

    /**
     * Determines whether a property represents 'lifecycle' property.
     *
     * @param root
     * @param checkedProperty
     * @return
     */
    private static boolean isLifecycle(final Class<?> root, final String checkedProperty) {
	return categorizer(root, checkedProperty) != null;
    }

    /**
     * Returns a categorizer associated with this property (if it is not @Monitoring at all -- returns <code>null</code>).
     *
     * @param root
     * @param checkedProperty
     * @return
     */
    private static ICategorizer categorizer(final Class<?> root, final String checkedProperty) {
	final Monitoring annotation = AnnotationReflector.getPropertyAnnotation(Monitoring.class, root, checkedProperty);
	try {
	    return annotation != null ? annotation.value().newInstance() : null;
	} catch (final InstantiationException e) {
	    e.printStackTrace();
	    logger().error(e.getMessage());
	    throw new RuntimeException(e);
	} catch (final IllegalAccessException e) {
	    e.printStackTrace();
	    logger().error(e.getMessage());
	    throw new RuntimeException(e);
	}
    }

    @Override
    public Date getFrom() {
	return from;
    }

    @Override
    public ILifecycleDomainTreeManager setFrom(final Date from) {
	this.from = from;
	return this;
    }

    @Override
    public Date getTo() {
	return to;
    }

    @Override
    public ILifecycleDomainTreeManager setTo(final Date to) {
	this.to = to;
	return this;
    }

    @Override
    public boolean isTotal() {
	return total != null ? total : false; // should be FALSE by default;
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setTotal(final boolean total) {
	this.total = total;
	return this;
    }

    /**
     * Makes CountOfSelfDashboard property immutably 'checked' and 'used' (second tick) and also disabled for both ticks.
     */
    public void provideMetaStateForLifecycleAnalysesDatePeriodProperties() {
	for (final Class<?> rootType : getRepresentation().rootTypes()) {
	    for (final GroupingPeriods period : GroupingPeriods.values()) {
		getFirstTick().check(rootType, period.getPropertyName(), true);
	    }
	}
	((LifecycleDomainTreeRepresentation) getRepresentation()).provideMetaStateForLifecycleAnalysesDatePeriodProperties();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((from == null) ? 0 : from.hashCode());
	result = prime * result + ((to == null) ? 0 : to.hashCode());
	result = prime * result + ((total == null) ? 0 : total.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final LifecycleDomainTreeManager other = (LifecycleDomainTreeManager) obj;
	if (from == null) {
	    if (other.from != null)
		return false;
	} else if (!from.equals(other.from))
	    return false;
	if (to == null) {
	    if (other.to != null)
		return false;
	} else if (!to.equals(other.to))
	    return false;
	if (total == null) {
	    if (other.total != null)
		return false;
	} else if (!total.equals(other.total))
	    return false;
	return true;
    }

    /**
     * TODO implement
     *
     * @param root
     * @param categoryMarker
     * @return
     */
    public static ICategory getCategory(final Class<?> root, final String categoryMarker) {
	return null;
    }
}
