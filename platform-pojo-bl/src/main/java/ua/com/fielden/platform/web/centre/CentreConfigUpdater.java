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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.validation.GreaterOrEqualValidator;
import ua.com.fielden.platform.entity.validation.GreaterValidator;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;

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
    private final Set<CustomisableColumn> customisableColumns = new LinkedHashSet<>();

    @IsProperty(value = String.class)
    @Title(value = "Sorting values", desc = "Values of sorting properties -- 'asc', 'desc' or 'none' (the order is important and should be strictly the same as in 'sortingIds' property)")
    @AfterChange(CentreConfigUpdaterSortingValsDefiner.class)
    private final List<String> sortingVals = new ArrayList<>(); // this list should not contain duplicates, please ensure that when setSortingVals invocation is performing

    @IsProperty
    @Title(value = "Trigger Re-run", desc = "Indicates whether successful saving of this entity should trigger re-run.")
    private boolean triggerRerun;

    @IsProperty
    @Title(value = "Master Entity Holder", desc = "Master entity's holder that is set during producing of this functional action and is used to restore master entity in companion object.")
    private CentreContextHolder masterEntityHolder;

    @IsProperty
    @Title(value = "Centre Changed", desc = "Indicates whether successful saving of this entity actually changed centre. Only populated when centre sorting wasn't changed.")
    private boolean centreChanged;

    @IsProperty
    @Title(value = "Page Capacity", desc = "The maximum number of entities retrieved.")
    @Required
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "300")})})
    private Integer pageCapacity;
    
    @IsProperty
    @Title(value = "Max Page Capacity", desc = "The maximum possible value for page capacity.")
    @Required
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0")})})
    private Integer maxPageCapacity;

    @IsProperty
    @Title(value = "Visible Rows", desc = "The number of visible rows. Value 0 (zero) stands for \"display all data retrieved\".")
    @Required
    @BeforeChange({@Handler(value = GreaterOrEqualValidator.class, str = {@StrParam(name = "limit", value = "0")})})
    private Integer visibleRowsCount;

    @IsProperty
    @Title(value = "Number of Header Lines", desc = "The maximum number of wrapped lines in table header. Minumum is 1 and maximum is 3.")
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "3")})})
    private Integer numberOfHeaderLines;

    @Observable
    public CentreConfigUpdater setNumberOfHeaderLines(final Integer numberOfHeaderLines) {
        this.numberOfHeaderLines = numberOfHeaderLines;
        return this;
    }

    public Integer getNumberOfHeaderLines() {
        return numberOfHeaderLines;
    }

    @Observable
    public CentreConfigUpdater setVisibleRowsCount(final Integer visibleRowsCount) {
        this.visibleRowsCount = visibleRowsCount;
        return this;
    }

    public Integer getVisibleRowsCount() {
        return visibleRowsCount;
    }

    @Observable
    public CentreConfigUpdater setMaxPageCapacity(final Integer maxPageCapacity) {
        this.maxPageCapacity = maxPageCapacity;
        return this;
    }

    public Integer getMaxPageCapacity() {
        return maxPageCapacity;
    }

    @Observable
    public CentreConfigUpdater setPageCapacity(final Integer pageCapacity) {
        this.pageCapacity = pageCapacity;
        return this;
    }

    public Integer getPageCapacity() {
        return pageCapacity;
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
    public CentreConfigUpdater setTriggerRerun(final boolean triggerRerun) {
        this.triggerRerun = triggerRerun;
        return this;
    }

    public boolean isTriggerRerun() {
        return triggerRerun;
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
