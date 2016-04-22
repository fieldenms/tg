package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Property Name", desc = "The Property Name of this sorting property.")
@CompanionObject(ISortingProperty.class)
@DescTitle(value = "Description", desc = "Description of this sorting property")
public class SortingProperty extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty
    @Title(value = "Title", desc = "Title of this sorting property")
    private String title;
    
    @IsProperty
    @Title(value = "Sorting", desc = "Sorting")
    private Boolean sorting = null;
    
    @IsProperty
    @Title(value = "Sorting Number", desc = "Sorting Number")
    private Integer sortingNumber = -1;

    @Observable
    public SortingProperty setSortingNumber(final Integer sortingNumber) {
        this.sortingNumber = sortingNumber;
        return this;
    }

    public Integer getSortingNumber() {
        return sortingNumber;
    }

    @Observable
    public SortingProperty setSorting(final Boolean sorting) {
        this.sorting = sorting;
        return this;
    }

    public Boolean getSorting() {
        return sorting;
    }

    @Observable
    public SortingProperty setTitle(final String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }
}