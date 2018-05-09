package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/** 
 * Entity that represents collectional item in {@link CentreConfigLoadAction}.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Save As Name", desc = "Save As Name of loadable centre configuration.")
@CompanionObject(ILoadableCentreConfig.class)
@DescTitle(value = "Description", desc = "Description of loadable centre configuration.")
public class LoadableCentreConfig extends AbstractEntity<String> {
    
}