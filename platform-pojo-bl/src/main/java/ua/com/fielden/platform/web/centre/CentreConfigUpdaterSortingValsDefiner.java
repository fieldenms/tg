package ua.com.fielden.platform.web.centre;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;

public class CentreConfigUpdaterSortingValsDefiner implements IAfterChangeEventHandler<Set<String>> {

    @Inject
    public CentreConfigUpdaterSortingValsDefiner() {
        super();
    }

    @Override
    public void handle(final MetaProperty<Set<String>> property, final Set<String> sortingVals) {
        final CentreConfigUpdater updater = (CentreConfigUpdater) property.getEntity();
        
        for (final SortingProperty sortingProperty: updater.getSortingProperties()) {
            sortingProperty.setSorting(null);
            sortingProperty.setSortingNumber(-1);
        }
        
        int i = 0;
        for (final String sortingVal: sortingVals) {
            final String[] splitted = sortingVal.split(":");
            final String name = splitted[0];
            final String ascOrDesc = splitted[1];
            final int sortingNumber = i;
            updater.getSortingProperties().stream().filter(item -> item.getKey().equals(name)).findFirst().map(f -> {
                f.setSorting("asc".equals(ascOrDesc)); // true or false (can not be null)
                f.setSortingNumber(sortingNumber);
                return f;
            });
            
            i++;
        }
    }

}
