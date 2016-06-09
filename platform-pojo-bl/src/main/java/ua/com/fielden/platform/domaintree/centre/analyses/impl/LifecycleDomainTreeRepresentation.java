package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel.GroupingPeriods;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for lifecycle analyses.
 * 
 * @author TG Team
 * 
 */
public class LifecycleDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements ILifecycleDomainTreeRepresentation {
    public static final String CATEGORY_PROPERTY_MARKER = "\"This is a marker expression, that indicates that this is a 'category' property to be used in lifecycle analyses.\"";

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param rootTypes
     */
    public LifecycleDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, rootTypes, createSet(), new LifecycleAddToDistributionTickRepresentation(), new LifecycleAddToCategoriesTickRepresentation());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected LifecycleDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final LifecycleAddToDistributionTickRepresentation firstTick, final LifecycleAddToCategoriesTickRepresentation secondTick) {
        super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    @Override
    public ILifecycleAddToDistributionTickRepresentation getFirstTick() {
        return (ILifecycleAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public ILifecycleAddToCategoriesTickRepresentation getSecondTick() {
        return (ILifecycleAddToCategoriesTickRepresentation) super.getSecondTick();
    }

    public static class LifecycleAddToDistributionTickRepresentation extends AbstractAnalysisAddToDistributionTickRepresentation implements ILifecycleAddToDistributionTickRepresentation {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public LifecycleAddToDistributionTickRepresentation() {
        }

        @Override
        public boolean isDisabledImmutablyLightweight(final Class<?> root, final String property) {
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            if (isEntityItself) { // "entities itself" should be enabled for lifecycle distribution
                return false;
            }
            return super.isDisabledImmutablyLightweight(root, property);
        }
    }

    public static class LifecycleAddToCategoriesTickRepresentation extends AbstractAnalysisAddToAggregationTickRepresentation implements ILifecycleAddToCategoriesTickRepresentation {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
         * field.
         */
        public LifecycleAddToCategoriesTickRepresentation() {
        }

        @Override
        public LifecycleDomainTreeRepresentation getDtr() {
            return (LifecycleDomainTreeRepresentation) super.getDtr();
        }

        /**
         * A contract for reverse "enabling" of the properties. For e.g. in base analyses second tick we should enable only AGGREGATED_EXPRESSION properties. But here the following
         * properties should be enabled:
         * <p>
         * 1) Lifecycle properties (with @Monitoring) <br>
         * 2) "category" properties (they will appear after appropriate Lifecycle property will be chosen)
         * 
         * @param root
         * @param property
         * @return
         */
        @Override
        protected boolean isEnabledImmutably(final Class<?> root, final String property) {
            // inject an enhanced type into method implementation
            final Class<?> managedType = managedType(root);
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"

            final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(managedType /* root */, property);
            final Class<?> penultType = transformed.getKey();
            final String lastPropertyName = transformed.getValue();

            return !isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(Monitoring.class, penultType, lastPropertyName) || // enable "lifecycle" properties (with @Monitoring)
                    !isEntityItself && isCategoryProperty(managedType, property); // enable "category" properties
        }

        /**
         * Indicates whether the property represents so called "category" property.
         * 
         * @param managedType
         * @param property
         * @return
         */
        protected static boolean isCategoryProperty(final Class<?> managedType, final String property) {
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, property);
            final Calculated calculatedAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(Calculated.class, managedType, property);
            final String expr = calculatedAnnotation != null ? calculatedAnnotation.value() : null;
            return expr != null && EntityUtils.isString(propertyType) && //
                    expr.equals(CATEGORY_PROPERTY_MARKER);
        }

        /**
         * Indicates whether the property represents so called "date period" marker.
         * 
         * @param property
         * @return
         */
        public static boolean isDatePeriodProperty(final Class<?> managedType, final String property) {
            final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
            if (isEntityItself) {
                return false;
            }
            for (final GroupingPeriods period : GroupingPeriods.values()) {
                final String title = TitlesDescsGetter.getTitleAndDesc(property, managedType).getKey();
                if (period.getTitle().equals(title)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Provides a meta-state for Date Period distribution properties.
     */
    public void provideMetaStateForLifecycleAnalysesDatePeriodProperties() {
    }

    /**
     * A specific Kryo serialiser for {@link LifecycleDomainTreeRepresentation}.
     * 
     * @author TG Team
     * 
     */
    public static class LifecycleDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<LifecycleDomainTreeRepresentation> {
        public LifecycleDomainTreeRepresentationSerialiser(final ISerialiser serialiser) {
            super(serialiser);
        }

        @Override
        public LifecycleDomainTreeRepresentation read(final ByteBuffer buffer) {
            final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
            final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
            final LifecycleAddToDistributionTickRepresentation firstTick = readValue(buffer, LifecycleAddToDistributionTickRepresentation.class);
            final LifecycleAddToCategoriesTickRepresentation secondTick = readValue(buffer, LifecycleAddToCategoriesTickRepresentation.class);
            return new LifecycleDomainTreeRepresentation(serialiser(), rootTypes, excludedProperties, firstTick, secondTick);
        }
    }
}
