package ua.com.fielden.snappy;

import java.util.ArrayList;
import java.util.List;

public class RsAggrResult extends Result {
    private final List<Object> aggregatedValues;
    private final List<String> aggrAccessors;

    public RsAggrResult(final Object failedAggrValues, final List<String> aggrAccessors) {
        super(getState(failedAggrValues));
        this.aggregatedValues = createAggregatedValues(failedAggrValues);
        this.aggrAccessors = aggrAccessors;
    }

    public RsAggrResult(final List<Object> aggregatedValues, final List<String> aggrAccessors) {
        super(getState(aggregatedValues));
        this.aggregatedValues = aggregatedValues;
        this.aggrAccessors = aggrAccessors;
    }

    private static ResultState getState(final Object failedAggrValues) {
        return (failedAggrValues != null) ? ResultState.FAILED : ResultState.SUCCESSED;
    }

    private static ResultState getState(final List<Object> aggregatedValues) {
        return (!aggregatedValues.isEmpty()) ? ResultState.FAILED : ResultState.SUCCESSED;
    }

    public List<Object> aggregatedValues() {
        return aggregatedValues;
    }

    private List<Object> createAggregatedValues(final Object failedAggrValues) {
        final List<Object> actualObjects = new ArrayList<Object>();
        final Object aggrResult = failedAggrValues;
        if (aggrResult == null) { // empty result!
            return actualObjects;
        }
        try {
            final Object[] resultTuple = (Object[]) aggrResult;
            for (int j = 0; j < resultTuple.length; j++) {
                actualObjects.add(resultTuple[j]);
            }
        } catch (final ClassCastException ex) { // single aggregated value!
            actualObjects.add(aggrResult);
        }
        return actualObjects;
    }

    public List<String> getAggrAccessors() {
        return aggrAccessors;
    }
}
