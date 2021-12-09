package ua.com.fielden.platform.entity.functional.master;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Property", desc = "Property name with which this warning is associated.")
@CompanionObject(PropertyWarningCo.class)
@DescTitle(value = "Warning", desc = "Warning")
public class PropertyWarning extends AbstractEntity<String> {

}