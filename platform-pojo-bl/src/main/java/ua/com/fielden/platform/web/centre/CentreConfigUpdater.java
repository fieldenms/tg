package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;

/** 
 * Functional entity for updating centre configuration: centre's column visibility / order and centre's sorting.
 * 
 * @author TG Team
 *
 */
@CompanionObject(ICentreConfigUpdater.class)
// !@MapEntityTo -- here the entity is not persistent intentionally
public class CentreConfigUpdater extends AbstractFunctionalEntityForCollectionModification<String> {
    @IsProperty(CustomisableColumn.class)
    @Title("Customisable Columns")
    private Set<CustomisableColumn> customisableColumns = new LinkedHashSet<>();
    
    @IsProperty(value = String.class) 
    @Title(value = "Sorting values", desc = "Values of sorting properties -- 'asc', 'desc' or 'none' (the order is important and should be strictly the same as in 'sortingIds' property)")
    @AfterChange(CentreConfigUpdaterSortingValsDefiner.class)
    private List<String> sortingVals = new ArrayList<>(); // this list should not contain duplicates, please ensure that when setSortingVals invocation is performing
    
    @IsProperty
    @Title(value = "Sorting Changed", desc = "Indicates whether successful saving of this entity actually changed centre sorting")
    private boolean sortingChanged;
    
    
    @IsProperty
    @Title("Master Entity Holder")
    private CentreContextHolder masterEntityHolder;
    
    @Observable
    public CentreConfigUpdater setMasterEntityHolder(final CentreContextHolder masterEntityHolder) {
        this.masterEntityHolder = masterEntityHolder;
        return this;
    }
    
    public CentreContextHolder getMasterEntityHolder() {
        return masterEntityHolder;
    }
    
    @Observable
    public CentreConfigUpdater setSortingChanged(final boolean sortingChanged) {
        this.sortingChanged = sortingChanged;
        return this;
    }

    public boolean isSortingChanged() {
        return sortingChanged;
    }

    @Observable
    public CentreConfigUpdater setSortingVals(final List<String> sortingVals) {
        this.sortingVals.clear();
        this.sortingVals.addAll(sortingVals);
        return this;
    }

    public List<String> getSortingVals() {
        return Collections.unmodifiableList(sortingVals);
    }

    @Observable
    protected CentreConfigUpdater setCustomisableColumns(final Set<CustomisableColumn> customisableColumns) {
        this.customisableColumns.clear();
        this.customisableColumns.addAll(customisableColumns);
        return this;
    }

    public Set<CustomisableColumn> getCustomisableColumns() {
        return Collections.unmodifiableSet(customisableColumns);
    }
}
