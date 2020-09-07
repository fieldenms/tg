package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.centre.IWidthManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotAddToDistributionTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * A domain tree manager for pivot analyses.
 *
 * @author TG Team
 *
 */
public class PivotDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IPivotDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param entityFactory
     * @param rootTypes
     */
    public PivotDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new PivotDomainTreeRepresentation(entityFactory, rootTypes), null, new PivotAddToDistributionTickManager(), new PivotAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected PivotDomainTreeManager(final EntityFactory entityFactory, final PivotDomainTreeRepresentation dtr, final Boolean visible, final PivotAddToDistributionTickManager firstTick, final PivotAddToAggregationTickManager secondTick) {
        super(entityFactory, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
        return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
        return (IPivotAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
        return (IPivotDomainTreeRepresentation) super.getRepresentation();
    }

    public static class PivotAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IPivotAddToDistributionTickManager {
        private final EnhancementPropertiesMap<Integer> propertiesWidths;

        private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;
        private final transient IUsageManager columnUsageManager;

        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public PivotAddToDistributionTickManager() {
            propertiesWidths = createPropertiesMap();
            rootsListsOfUsedProperties = createRootsMap();
            columnUsageManager = new ColumnUsageManager();
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            // inject an enhanced type into method implementation
            final Class<?> managedType = managedType(root);

            final List<String> listOfUsedProperties = getAndInitUsedProperties(managedType, property);
            if (check && !listOfUsedProperties.contains(property)) {
                listOfUsedProperties.add(property);
                getSecondUsageManager().use(root, property, false);
            } else if (!check) {
                listOfUsedProperties.remove(property);
            }
            return this;
        }

        @Override
        public int getWidth(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            return PivotDomainTreeRepresentation.getWidth(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
        }

        @Override
        public IPivotAddToDistributionTickManager setWidth(final Class<?> root, final String property, final int width) {
            AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            // update ALL existing widths for used properties with the same value (single column "treatment")
            for (final String usedProperty : usedProperties(root)) {
                PivotDomainTreeRepresentation.setWidth(tr().getDtr(), root, usedProperty, width, propertiesWidths);
            }
            return this;
        }

        @Override
        protected PivotAddToDistributionTickRepresentation tr() {
            return (PivotAddToDistributionTickRepresentation) super.tr();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
            result = prime * result + (rootsListsOfUsedProperties == null ? 0 : rootsListsOfUsedProperties.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this != obj) {
                if (super.equals(obj) && getClass() == obj.getClass()) {
                    final PivotAddToDistributionTickManager other = (PivotAddToDistributionTickManager) obj;
                    return Objects.equals(propertiesWidths, other.propertiesWidths) &&
                            Objects.equals(rootsListsOfUsedProperties, other.rootsListsOfUsedProperties);
                }
                return false;
            }
            return true;
        }

        @Override
        public IUsageManager getSecondUsageManager() {
            return columnUsageManager;
        }

        private class ColumnUsageManager implements IUsageManager {
            public ColumnUsageManager() {
            }

            private List<String> getAndInitSecondUsedProperties(final Class<?> root, final String property) {
                illegalUncheckedProperties(PivotAddToDistributionTickManager.this, root, property, "It's illegal to use/unuse the specified property [" + property
                        + "] if it is not 'checked' in type [" + root.getSimpleName() + "].");
                if (!rootsListsOfUsedProperties.containsKey(root)) {
                    rootsListsOfUsedProperties.put(root, new ArrayList<String>());
                }
                return rootsListsOfUsedProperties.get(root);
            }

            @Override
            public boolean isUsed(final Class<?> root, final String property) {
                // inject an enhanced type into method implementation
                final Class<?> managedType = managedType(root);

                illegalUncheckedProperties(PivotAddToDistributionTickManager.this, managedType, property, "It's illegal to ask whether the specified property [" + property
                        + "] is 'used' if it is not 'checked' in type [" + managedType.getSimpleName() + "].");
                return rootsListsOfUsedProperties.containsKey(managedType) && rootsListsOfUsedProperties.get(managedType).contains(property);
            }

            @Override
            public IUsageManager use(final Class<?> root, final String property, final boolean check) {
                // inject an enhanced type into method implementation
                final Class<?> managedType = managedType(root);

                final List<String> listOfUsedProperties = getAndInitSecondUsedProperties(managedType, property);
                if (check && !listOfUsedProperties.contains(property)) {
                    listOfUsedProperties.clear();
                    listOfUsedProperties.add(property);
                    PivotAddToDistributionTickManager.this.use(root, property, false);
                } else if (!check) {
                    listOfUsedProperties.remove(property);
                }
                return this;
            }

            @Override
            public List<String> usedProperties(final Class<?> root) {
                // inject an enhanced type into method implementation
                final Class<?> managedType = managedType(root);

                final List<String> checkedProperties = checkedProperties(managedType);
                final List<String> usedProperties = new ArrayList<>();
                for (final String property : checkedProperties) {
                    if (isUsed(managedType, property)) {
                        usedProperties.add(property);
                    }
                }
                return usedProperties;
            }
        }
    }

    public static class PivotAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IPivotAddToAggregationTickManager {
        private final EnhancementPropertiesMap<Integer> propertiesWidths;

        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public PivotAddToAggregationTickManager() {
            propertiesWidths = createPropertiesMap();
        }

        @Override
        public int getWidth(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not get a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            return PivotDomainTreeRepresentation.getWidth(tr().getDtr(), root, property, propertiesWidths, tr().getWidthByDefault(root, property));
        }

        @Override
        public IWidthManager setWidth(final Class<?> root, final String property, final int width) {
            AbstractDomainTree.illegalUnusedProperties(this, root, property, "Could not set a 'width' for 'unused' property [" + property + "] in type [" + root.getSimpleName()
                    + "].");
            PivotDomainTreeRepresentation.setWidth(tr().getDtr(), root, property, width, propertiesWidths);
            return this;
        }

        @Override
        protected PivotAddToAggregationTickRepresentation tr() {
            return (PivotAddToAggregationTickRepresentation) super.tr();
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            // inject an enhanced type into method implementation
            final Class<?> managedType = managedType(root);

            final List<String> listOfUsedProperties = getAndInitUsedProperties(managedType, property);
            if (check && !listOfUsedProperties.contains(property)) {
                listOfUsedProperties.add(property);
            } else if (!check && listOfUsedProperties.contains(property)) {
                // before successful removal of the Usage -- the Ordering should be removed
                while (isOrdered(property, orderedProperties(managedType))) {
                    toggleOrdering(managedType, property);
                }
                // perform actual removal
                listOfUsedProperties.remove(property);
            }
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this != obj) {
                if (super.equals(obj) && getClass() == obj.getClass()) {
                    final PivotAddToAggregationTickManager other = (PivotAddToAggregationTickManager) obj;
                    return Objects.equals(propertiesWidths, other.propertiesWidths);
                }
            }
            return true;
        }
    }

}
