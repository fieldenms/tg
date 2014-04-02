package ua.com.fielden.platform.swing.egi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;

public class TotalBuilder<E extends AbstractEntity<?>> {

    private final Map<String, List<ITotalCalculator<?, E>>> totals;

    public TotalBuilder() {
        totals = new HashMap<>();
    }

    public TotalBuilder<E> addTotal(final String propertyName, final ITotalCalculator<?, E> calculator) {
        List<ITotalCalculator<?, E>> calculators = totals.get(propertyName);
        if (calculators == null) {
            calculators = new ArrayList<>();
            totals.put(propertyName, calculators);
        }
        calculators.add(calculator);
        return this;
    }

    public boolean removeTotal(final String propertyName, final ITotalCalculator<?, E> calculator) {
        if (totals.containsKey(propertyName)) {
            final List<ITotalCalculator<?, E>> calculators = totals.get(propertyName);
            final boolean res = calculators.remove(calculator);
            if (calculators.isEmpty()) {
                totals.remove(propertyName);
            }
            return res;
        }
        return false;
    }

    public boolean isEmpty() {
        return totals.isEmpty();
    }

    public boolean containsTotalsFor(final String propertyName) {
        return totals.containsKey(propertyName);
    }

    public List<ITotalCalculator<?, E>> getCalculators(final String propertyName) {
        return totals.get(propertyName);
    }

    public int getMaxNumberOfTotals() {
        int rowNumber = 0;
        for (final String key : totals.keySet()) {
            final int nextRowNumber = totals.get(key).size();
            if (rowNumber < nextRowNumber) {
                rowNumber = nextRowNumber;
            }
        }
        return rowNumber;
    }

    public Set<String> getTotalPRopertyNames() {
        return totals.keySet();
    }
}
