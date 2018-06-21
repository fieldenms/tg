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
    @Title(value = "Master Entity Holder", desc = "Master entity's holder that is set during producing of this functional action and is used to restore master entity in companion object.")
    private CentreContextHolder masterEntityHolder;
    
    @IsProperty
    @Title(value = "Centre Changed", desc = "Indicates whether successful saving of this entity actually changed centre. Only populated when centre sorting wasn't changed.")
    private boolean centreChanged;
    
    @IsProperty
    @Title(value = "Stale Criteria Message", desc = "Indicates whether successful saving of this entity actually changed whether previously run results do not match fresh criteria. Only populated when centre sorting wasn't changed.")
    private String staleCriteriaMessage;
    
    @Observable
    public CentreConfigUpdater setStaleCriteriaMessage(final String staleCriteriaMessage) {
        this.staleCriteriaMessage = staleCriteriaMessage;
        return this;
    }
    
    public String getStaleCriteriaMessage() {
        return staleCriteriaMessage;
    }
    
    @Observable
    public CentreConfigUpdater setCentreChanged(final boolean centreChanged) {
        this.centreChanged = centreChanged;
        return this;
    }
    
    public boolean isCentreChanged() {
        return centreChanged;
    }
    
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
