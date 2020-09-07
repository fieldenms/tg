package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToCategoriesTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleAddToDistributionTickRepresentation;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
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
     * @param entityFactory
     * @param rootTypes
     */
    public LifecycleDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new LifecycleDomainTreeRepresentation(entityFactory, rootTypes), null, new LifecycleAddToDistributionTickManager(), new LifecycleAddToCategoriesTickManager(), null, null, null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LifecycleDomainTreeManager(final EntityFactory entityFactory, final LifecycleDomainTreeRepresentation dtr, final Boolean visible, final LifecycleAddToDistributionTickManager firstTick, final LifecycleAddToCategoriesTickManager secondTick, final Date from, final Date to, final Boolean total) {
        super(entityFactory, dtr, visible, firstTick, secondTick);

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
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            final List<String> listOfUsedProperties = getAndInitUsedProperties(root, property);
            if (check && !listOfUsedProperties.contains(property)) {
                listOfUsedProperties.clear();
                listOfUsedProperties.add(property);
            } else if (!check) {
                listOfUsedProperties.remove(property);
            }
            return this;
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

        private String lifecycleProperty(final Class<?> root) {
            final List<String> checkedProperties = checkedProperties(root);
            for (final String checkedProperty : checkedProperties) {
                if (isLifecycle(root, checkedProperty)) {
                    return checkedProperty;
                }
            }
            return null;
        }

        @Override
        protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
            if (isLifecycle(root, property)) {

                // remove previous lifecycle property if exists
                final String lifecycleProperty = lifecycleProperty(root);
                if (lifecycleProperty != null) {
                    removeCheckedProperty(root, lifecycleProperty);
                }

                super.insertCheckedProperty(root, property, 0); // no properties are checked after previous lifecycle property removal -- insert into beginning!
                addAndCheckAllCategoriesAndUseMainCategories(root, property);
            } else {
                super.insertCheckedProperty(root, property, index);
            }
        }

        protected void addAndCheckAllCategoriesAndUseMainCategories(final Class<?> root, final String lifecycleProperty) {
            final ICategorizer categorizer = categorizer(root, lifecycleProperty);
            for (final ICategory category : categorizer.getAllCategories()) {
                final Class<?> originalType = DynamicEntityClassLoader.getOriginalType(root);
                final ICalculatedProperty calcProp = getEnhancerFromCentre().addCalculatedProperty(originalType, "", LifecycleDomainTreeRepresentation.CATEGORY_PROPERTY_MARKER, category.getName(), category.getDesc(), CalculatedPropertyAttribute.NO_ATTR, "SELF", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
                getEnhancerFromCentre().apply();
                final String categoryPropertyName = calcProp.pathAndName();
                check(root, categoryPropertyName, true); // all categories should be checked by default
                if (categorizer.getMainCategories().contains(category)) {
                    use(root, categoryPropertyName, true); // only main categories should be used by default
                }
            }
        }

        /**
         * Removes 'checking' and 'usage' of all properties for this tick. This action includes unchecking of 'lifecycle' property and all of its category markers and the
         * destroying of all category markers.
         *
         * @param root
         */
        protected void removeCheckingAndUsageAndDestroyCategories(final Class<?> root, final String exceptProperty) {
            // uncheck all previously checked property(ies) (and unuse them first).
            // Note: there should be the only one or zero checked Lifecycle property, and, perhaps, some category markers.
            final List<String> checkedPropertiesMutable = checkedPropertiesMutable(root);
            checkedPropertiesMutable.remove(exceptProperty);
            while (!checkedPropertiesMutable.isEmpty()) {
                final String propertyToUncheck = checkedPropertiesMutable(root).get(0);
                if (isUsed(root, propertyToUncheck)) {
                    use(root, propertyToUncheck, false);
                }
                check(root, propertyToUncheck, false);
            }
            removeAllCategories(root);
        }

        protected void removeAllCategories(final Class<?> root) {
            final Class<?> originalType = DynamicEntityClassLoader.getOriginalType(root);
            final List<String> allCategories = allCats(root);
            for (final String categoryMarker : allCategories) {
                getEnhancerFromCentre().removeCalculatedProperty(originalType, categoryMarker);
                getEnhancerFromCentre().apply();
            }
        }

        protected List<String> allCats(final Class<?> root) {
            final Class<?> originalType = DynamicEntityClassLoader.getOriginalType(root);
            final List<String> includedProperties = new ArrayList<>(tr().getDtr().includedProperties(originalType));
            final Class<?> managedType = managedType(root);
            final List<String> allCats = new ArrayList<>();
            for (final String property : includedProperties) {
                if (!PropertyTypeDeterminator.isDotNotation(property) && LifecycleAddToCategoriesTickRepresentation.isCategoryProperty(managedType, property)) { // categories at this stage are located in root type
                    allCats.add(property);
                }
            }
            return allCats;
        }

        public IDomainTreeEnhancer getEnhancerFromCentre() {
            return tr().getDtr().parentCentreDomainTreeManager().getEnhancer();
        }

        @Override
        protected void removeCheckedProperty(final Class<?> root, final String property) {
            if (isLifecycle(root, property)) {
                removeCheckingAndUsageAndDestroyCategories(root, property);
                super.removeCheckedProperty(root, property);
            } else {
                super.removeCheckedProperty(root, property);
            }
        }

        @Override
        public List<? extends ICategory> allCategories(final Class<?> root) {
            final String lifecycleProperty = lifecycleProperty(root);
            return lifecycleProperty == null ? new ArrayList<>() : categorizer(root, lifecycleProperty(root)).getAllCategories();
        }

        @Override
        public List<? extends ICategory> currentCategories(final Class<?> root) {
            final List<? extends ICategory> allCategories = allCategories(root);
            final List<ICategory> res = new ArrayList<>();
            final List<String> usedProperties = usedProperties(root);
            for (final String property : usedProperties) {
                if (LifecycleDomainTreeRepresentation.LifecycleAddToCategoriesTickRepresentation.isCategoryProperty(managedType(root), property)) {
                    res.add(getCategory(managedType(root), property, allCategories));
                }
            }
            return res;
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
    public static boolean isLifecycle(final Class<?> root, final String checkedProperty) {
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
            getFirstTick().use(rootType, GroupingPeriods.values()[0].getPropertyName(), true);
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
        if (this != obj) {
            if (super.equals(obj) && getClass() == obj.getClass()) {
                final LifecycleDomainTreeManager other = (LifecycleDomainTreeManager) obj;
                return Objects.equals(from, other.from) && Objects.equals(to, other.to) && Objects.equals(total, other.total);
            }
            return false;
        }
        return true;
    }

    /**
     * Returns a category for a category marker.
     *
     * @param root
     * @param categoryMarker
     * @return
     */
    public static ICategory getCategory(final Class<?> managedType, final String categoryMarker, final List<? extends ICategory> allCategories) {
        for (final ICategory cat : allCategories) {
            if (cat.getName().equals(TitlesDescsGetter.getTitleAndDesc(categoryMarker, managedType).getKey())) {
                return cat;
            }
        }
        throw new DomainTreeException("Trying to determine a category from a property, which is not 'category marker' -- " + categoryMarker + ", from type "
                + managedType.getSimpleName() + ".");
    }

    /**
     * Returns a {@link GroupingPeriods} for a period marker.
     *
     * @param root
     * @param periodMarker
     * @return
     */
    public static GroupingPeriods getGroupingPeriod(final String periodMarker) {
        for (final GroupingPeriods period : GroupingPeriods.values()) {
            if (period.getPropertyName().equals(periodMarker)) {
                return period;
            }
        }
        return null;
    }
}
