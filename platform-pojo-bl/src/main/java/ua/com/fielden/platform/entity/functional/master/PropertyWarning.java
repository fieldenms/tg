package ua.com.fielden.platform.entity.functional.master;

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
@KeyTitle(value = "Property", desc = "Property name with which this warning is associated.")
@CompanionObject(IPropertyWarning.class)
@DescTitle(value = "Warning", desc = "Warning")
public class PropertyWarning extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
    
//    @IsProperty
//    @Title(value = "Title", desc = "Title of this security token")
//    private String title;
//
//    @Observable
//    public PropertyWarning setTitle(final String title) {
//        this.title = title;
//        return this;
//    }
//
//    public String getTitle() {
//        return title;
//    }
}