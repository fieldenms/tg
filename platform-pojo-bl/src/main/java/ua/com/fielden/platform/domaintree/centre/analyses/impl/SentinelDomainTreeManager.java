package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * A domain tree manager for sentinel analyses.
 * 
 * @author TG Team
 * 
 */
public class SentinelDomainTreeManager extends AnalysisDomainTreeManager implements ISentinelDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     * 
     * @param entityFactory
     * @param rootTypes
     */
    public SentinelDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new SentinelDomainTreeRepresentation(entityFactory, rootTypes), null, new SentinelAddToDistributionTickManager(), new SentinelAddToAggregationTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     * 
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected SentinelDomainTreeManager(final EntityFactory entityFactory, final AnalysisDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick, final Integer visibleDistributedValuesNumber) {
        super(entityFactory, dtr, visible, firstTick, secondTick, visibleDistributedValuesNumber);
    }

    @Override
    public ISentinelAddToDistributionTickManager getFirstTick() {
        return (ISentinelAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public ISentinelAddToAggregationTickManager getSecondTick() {
        return (ISentinelAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public ISentinelDomainTreeRepresentation getRepresentation() {
        return (ISentinelDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * Makes CountOfSelfDashboard property immutably 'checked' and 'used' (second tick) and also disabled for both ticks.
     */
    public void provideMetaStateForCountOfSelfDashboardProperty() {
        for (final Class<?> rootType : getRepresentation().rootTypes()) {
            getSecondTick().check(rootType, SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD, true);
        }
        ((SentinelDomainTreeRepresentation) getRepresentation()).provideMetaStateForCountOfSelfDashboardProperty();
    }

    public static class SentinelAddToDistributionTickManager extends AnalysisAddToDistributionTickManager implements ISentinelAddToDistributionTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public SentinelAddToDistributionTickManager() {
        }

        @Override
        public ITickManager check(final Class<?> root, final String property, final boolean check) {
            if (check) {
                // remove previously checked property(ies). It should be the only one, but remove them all to be sure:
                while (!checkedPropertiesMutable(root).isEmpty()) {
                    final String propertyToUncheck = checkedPropertiesMutable(root).get(0);
                    if (isUsed(root, propertyToUncheck)) {
                        useInternally(root, propertyToUncheck, false);
                    }
                    check(root, propertyToUncheck, false);
                }
            } else {
                if (isUsed(root, property)) {
                    useInternally(root, property, false);
                }
            }
            super.check(root, property, check);
            if (check) {
                useInternally(root, property, true); // automatic usage of the property
            }
            return this;
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            throw new UnsupportedOperationException("Usage operation is prohibited due to automatic management 'used' properties by 'check' operation. It was tried to '[un]use' property ["
                    + property + "] in type [" + root.getSimpleName() + "].");
        }

        private void useInternally(final Class<?> root, final String property, final boolean check) {
            super.use(root, property, check);
        }
    }

    public static class SentinelAddToAggregationTickManager extends AnalysisAddToAggregationTickManager implements ISentinelAddToAggregationTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public SentinelAddToAggregationTickManager() {
        }

        @Override
        public ITickManager check(final Class<?> root, final String property, final boolean check) {
            if (!SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD.equals(property)) {
                throw new DomainTreeException("It was tried to 'check' property [" + property + "] in type [" + root.getSimpleName() + "]. But only ["
                        + SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD + "] is permitted for checking.");
            }
            if (!check) {
                throw new DomainTreeException("It was tried to 'UNcheck' property [" + SentinelDomainTreeRepresentation.COUNT_OF_SELF_DASHBOARD + "] property in type ["
                        + root.getSimpleName() + "]. But it should remain immutable checked forever.");
            }
            super.check(root, property, check);
            useInternally(root, property, true); // automatic usage of the property
            return this;
        }

        @Override
        public IUsageManager use(final Class<?> root, final String property, final boolean check) {
            throw new UnsupportedOperationException("Usage operation is prohibited due to automatic management 'used' properties by 'check' operation. It was tried to '[un]use' property ["
                    + property + "] in type [" + root.getSimpleName() + "].");
        }

        private void useInternally(final Class<?> root, final String property, final boolean check) {
            super.use(root, property, check);
        }
    }

}
