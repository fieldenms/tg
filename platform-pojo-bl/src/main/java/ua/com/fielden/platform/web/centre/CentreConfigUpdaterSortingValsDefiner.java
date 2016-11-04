package ua.com.fielden.platform.web.centre;

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.streaming.ValueCollectors;

/**
 * During validation cycles for sorting dialog there is a need to migrate sorting information from 'sortingVals' (serialisable form of information
 * from the client side) to fully-fledged 'sortingProperties'.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterSortingValsDefiner implements IAfterChangeEventHandler<Set<String>> {

    @Inject
    public CentreConfigUpdaterSortingValsDefiner() {
        super();
    }

    @Override
    public void handle(final MetaProperty<Set<String>> property, final Set<String> sortingVals) {
        final CentreConfigUpdater updater = (CentreConfigUpdater) property.getEntity();
        
        // clear sorting properties
        for (final SortingProperty sortingProperty: updater.getSortingProperties()) {
            sortingProperty.setSorting(null);
            sortingProperty.setSortingNumber(-1);
        }
        
        final Map<String, SortingProperty> sortingPropsByKeys = updater.getSortingProperties().stream().collect(ValueCollectors.toLinkedHashMap(sp -> sp.getKey(), sp -> sp));
        int currentSortingNumber = 0;
        // updated SortingProperty instances in correct order, defined in sortingVals
        for (final String sortingVal: sortingVals) {
            final String[] splitted = sortingVal.split(":");
            final String name = splitted[0];
            final String ascOrDesc = splitted[1];
            final int sortingNumber = currentSortingNumber;
            
            final SortingProperty sortingProperty = sortingPropsByKeys.get(name);
            sortingProperty.setSorting("asc".equals(ascOrDesc)); // true or false (can not be null)
            sortingProperty.setSortingNumber(sortingNumber);
            
            currentSortingNumber++;
        }
    }

}
