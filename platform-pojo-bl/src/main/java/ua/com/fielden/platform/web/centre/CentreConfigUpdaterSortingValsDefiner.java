package ua.com.fielden.platform.web.centre;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.streaming.ValueCollectors;

/**
 * During validation cycles for centre configuration dialog there is a need to migrate sorting information from 'sortingVals' (serialisable form of information
 * from the client side) to fully-fledged 'customisableColumns'.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterSortingValsDefiner implements IAfterChangeEventHandler<List<String>> {

    @Override
    public void handle(final MetaProperty<List<String>> property, final List<String> sortingVals) {
        if (new HashSet<>(sortingVals).size() < sortingVals.size()) { // if sortingVals contain duplicates then sortingVals was not formed correctly in tg-collectional-editor (programming error, please correct tg-collectional-editor implementation in such case) 
            throw new IllegalArgumentException(String.format("SortingVals [%s] contains duplicates, please correct implementation in tg-collectional-editor.", sortingVals));
        }
        final CentreConfigUpdater updater = (CentreConfigUpdater) property.getEntity();
        
        // clear sorting properties
        for (final CustomisableColumn customisableColumn: updater.getCustomisableColumns()) {
            customisableColumn.setSorting(null);
            customisableColumn.setSortingNumber(-1);
        }
        
        final Map<String, CustomisableColumn> customisableColumnsByKeys = updater.getCustomisableColumns().stream().collect(ValueCollectors.toLinkedHashMap(sp -> sp.getKey(), sp -> sp));
        int currentSortingNumber = 0;
        // update CustomisableColumn instances in correct order, defined in sortingVals
        for (final String sortingVal: sortingVals) {
            final String[] splitted = sortingVal.split(":");
            final String name = splitted[0];
            final String ascOrDesc = splitted[1];
            final int sortingNumber = currentSortingNumber;
            
            final CustomisableColumn customisableColumn = customisableColumnsByKeys.get(name);
            customisableColumn.setSorting("asc".equals(ascOrDesc)); // true or false (can not be null)
            customisableColumn.setSortingNumber(sortingNumber);
            
            currentSortingNumber++;
        }
    }

}
