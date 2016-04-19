package ua.com.fielden.platform.web.centre;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ICentreConfigUpdater.class)
@KeyType(EnhancedCentreEntityQueryCriteria.class)
// @MapEntityTo
@KeyTitle(value = "Criteria Entity", desc = "Criteria entity, whose 'sortingProperties' collection modifies by this functional action.")
public class CentreConfigUpdater extends AbstractFunctionalEntityForCollectionModification<EnhancedCentreEntityQueryCriteria, String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(SortingProperty.class)
    @Title(value = "Sorting Properties", desc = "A list of sorting properties")
    private Set<SortingProperty> sortingProperties = new LinkedHashSet<SortingProperty>();
    
    @IsProperty(value = String.class) 
    @Title(value = "Sorting values", desc = "Values of sorting properties -- 'asc', 'desc' or 'none' (the order is important and should be strictly the same as in 'sortingIds' property)")
    private Set<String> sortingVals = new LinkedHashSet<>();
    
    @Observable
    public CentreConfigUpdater setSortingVals(final Set<String> sortingVals) {
        this.sortingVals.clear();
        this.sortingVals.addAll(sortingVals);
        return this;
    }

    public Set<String> getSortingVals() {
        return Collections.unmodifiableSet(sortingVals);
    }

    @Observable
    protected CentreConfigUpdater setSortingProperties(final Set<SortingProperty> sortingProperties) {
        this.sortingProperties.clear();
        this.sortingProperties.addAll(sortingProperties);
        return this;
    }

    public Set<SortingProperty> getSortingProperties() {
        return Collections.unmodifiableSet(sortingProperties);
    }
}
