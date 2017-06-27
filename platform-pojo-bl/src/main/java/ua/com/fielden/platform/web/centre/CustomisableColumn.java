package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Property Name", desc = "Property name of this customisable column property.")
@CompanionObject(ICustomisableColumn.class)
@DescTitle(value = "Description", desc = "Description of this customisable column property.")
public class CustomisableColumn extends AbstractEntity<String> {
    @IsProperty
    @Title(value = "Title", desc = "Title of this customisable column")
    private String title;
    
    @IsProperty
    @Title("Sorting")
    private Boolean sorting = null;
    
    @IsProperty
    @Title("Sorting Number")
    private Integer sortingNumber = -1;
    
    @IsProperty
    @Title("Sortable")
    private boolean sortable = false;

    @Observable
    public CustomisableColumn setSortable(final boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    public boolean getSortable() {
        return sortable;
    }

    @Observable
    public CustomisableColumn setSortingNumber(final Integer sortingNumber) {
        this.sortingNumber = sortingNumber;
        return this;
    }

    public Integer getSortingNumber() {
        return sortingNumber;
    }

    @Observable
    public CustomisableColumn setSorting(final Boolean sorting) {
        this.sorting = sorting;
        return this;
    }

    public Boolean getSorting() {
        return sorting;
    }

    @Observable
    public CustomisableColumn setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}