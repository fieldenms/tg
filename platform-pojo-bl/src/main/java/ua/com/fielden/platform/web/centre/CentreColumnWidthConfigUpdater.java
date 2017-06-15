package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
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
@CompanionObject(ICentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdater extends AbstractFunctionalEntityWithCentreContext<String> {
    
    @IsProperty
    @Title(value = "Column Property", desc = "Column property name")
    private String propName;
    
    @IsProperty
    @Title(value = "New Width", desc = "New width of property column")
    private Integer newWidth;

    @Observable
    public CentreColumnWidthConfigUpdater setNewWidth(final Integer newWidth) {
        this.newWidth = newWidth;
        return this;
    }

    public Integer getNewWidth() {
        return newWidth;
    }
    
    @Observable
    public CentreColumnWidthConfigUpdater setPropName(final String propName) {
        this.propName = propName;
        return this;
    }

    public String getPropName() {
        return propName;
    }
    
}
