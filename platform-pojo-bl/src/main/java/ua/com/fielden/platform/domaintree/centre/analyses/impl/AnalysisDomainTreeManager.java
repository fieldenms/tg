package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IUsageManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToDistributionTickRepresentation;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * A domain tree manager for analyses.
 *
 * @author TG Team
 *
 */
public class AnalysisDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IAnalysisDomainTreeManager {
    private Integer visibleDistributedValuesNumber;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param entityFactory
     * @param rootTypes
     */
    public AnalysisDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new AnalysisDomainTreeRepresentation(entityFactory, rootTypes), null, new AnalysisAddToDistributionTickManager(), new AnalysisAddToAggregationTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AnalysisDomainTreeManager(final EntityFactory entityFactory, final AnalysisDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick, final Integer visibleDistributedValuesNumber) {
        super(entityFactory, dtr, visible, firstTick, secondTick);

        this.visibleDistributedValuesNumber = visibleDistributedValuesNumber;
    }

    @Override
    public IAnalysisAddToDistributionTickManager getFirstTick() {
        return (IAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickManager getSecondTick() {
        return (IAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IAnalysisDomainTreeRepresentation getRepresentation() {
        return (IAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    public static class AnalysisAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IAnalysisAddToDistributionTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AnalysisAddToDistributionTickManager() {
        }

        @Override
        protected AnalysisAddToDistributionTickRepresentation tr() {
            return (AnalysisAddToDistributionTickRepresentation) super.tr();
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

    public static class AnalysisAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IAnalysisAddToAggregationTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AnalysisAddToAggregationTickManager() {
        }

        @Override
        protected AnalysisAddToAggregationTickRepresentation tr() {
            return (AnalysisAddToAggregationTickRepresentation) super.tr();
        }
    }

    @Override
    public int getVisibleDistributedValuesNumber() {
        return visibleDistributedValuesNumber == null ? 0 : visibleDistributedValuesNumber;
    }

    @Override
    public IAnalysisDomainTreeManager setVisibleDistributedValuesNumber(final int visibleDistributedValuesNumber) {
        this.visibleDistributedValuesNumber = Integer.valueOf(visibleDistributedValuesNumber);
        return this;
    }

    protected Integer visibleDistributedValuesNumber() {
        return visibleDistributedValuesNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((visibleDistributedValuesNumber == null) ? 0 : visibleDistributedValuesNumber.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this != obj) {
            if (super.equals(obj) && getClass() == obj.getClass()) {
                final AnalysisDomainTreeManager other = (AnalysisDomainTreeManager) obj;
                return Objects.equals(visibleDistributedValuesNumber, other.visibleDistributedValuesNumber);
            }
            return false;
        }
        return true;
    }
}
